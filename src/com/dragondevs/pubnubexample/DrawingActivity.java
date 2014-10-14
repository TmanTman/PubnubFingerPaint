/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dragondevs.pubnubexample;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class DrawingActivity extends Activity
        implements ColorPickerDialog.OnColorChangedListener {
	
	private String channelName;
	private Handler mHandler;
	private final String TAG = "DrawingActivity";
	MyView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        channelName = intent.getStringExtra("CHANNEL");
        
      //Create Handler to update screen from PubnubClient
        mHandler = new Handler(Looper.getMainLooper()){
        	@Override 
        	public void handleMessage(Message inputMessage){
        		if (inputMessage.obj instanceof JSONObject) {
        			JSONObject json = (JSONObject)inputMessage.obj;
	        		int x = -1;
	        		int y = -1;
	        		int state = -1;
	        		try {
	        			x = json.getInt("X");
	        			y = json.getInt("Y");
	        			state = json.getInt("STATE");
	        		} catch (JSONException e) {
	        			Log.d(TAG, "Error: " + e.toString());
	        		}
	        		Log.d(TAG, "Drawing coord at x, y, state: " + x + " " + y + " " + state);
	        		if (x>=0 && y>=0 && state>=0) myView.drawLine(x, y, state);
	        		else Log.d(TAG, "Json values read was not valid");
        		}
        	}
        };
        
        myView = new MyView(this, mHandler);
        
        setContentView(myView);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
     
    }

    private Paint       mPaint;
    
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class MyView extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;

        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Paint   mBitmapPaint;        
        private PubnubClient mPubnubClient;
        private DrawnLine mLine;
        private DrawnLine receiveLine;
        
    	public static final int TOUCH_DOWN = 0;
    	public static final int TOUCH_MOVE = 1;
    	public static final int TOUCH_UP = 2;
        
        public class DrawnLine {
        	Path mPath = new Path();
        	Paint mPaint = new Paint();
            float mX = 0;
            float mY = 0;
        }
              
        public MyView(Context c, Handler h) {
            super(c);
            //For Pubnub
            mPubnubClient = new PubnubClient(channelName, this, h);
            
            //Drawing mechanisms
            mLine = new DrawnLine();
            receiveLine = new DrawnLine();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

		@Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mLine.mPath, mPaint);
            
            canvas.drawPath(receiveLine.mPath, mPaint);
        }
        
        //Method called when line was received from PubnubClient
        public void drawLine(int x, int y, int state) {
			switch (state) {
			case TOUCH_DOWN: touch_start(x, y, receiveLine); invalidate(); return;
			case TOUCH_MOVE: touch_move(x, y, receiveLine); invalidate(); return;
			case TOUCH_UP: touch_up(receiveLine); invalidate(); return;
			}
			
		}

        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y, DrawnLine l) {
        	l.mPath.reset();
        	l.mPath.moveTo(x, y);
            l.mX = x;
            l.mY = y;
        }
        private void touch_move(float x, float y, DrawnLine l) {
            float dx = Math.abs(x - l.mX);
            float dy = Math.abs(y - l.mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            	l.mPath.quadTo(l.mX, l.mY, (x + l.mX)/2, (y + l.mY)/2);
                l.mX = x;
                l.mY = y;
            }
        }
        private void touch_up(DrawnLine l) {
        	l.mPath.lineTo(l.mX, l.mY);
            // commit the path to our offscreen
            mCanvas.drawPath(l.mPath, mPaint);
            // kill this so we don't double draw
            l.mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y, mLine);
                    mPubnubClient.send(x, y, TOUCH_DOWN);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y, mLine);
                    mPubnubClient.send(x, y, TOUCH_MOVE);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up(mLine);
                    mPubnubClient.send(0, 0, TOUCH_UP);
                    invalidate();
                    break;
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
