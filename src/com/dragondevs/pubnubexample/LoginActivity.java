package com.dragondevs.pubnubexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
    }
    
    public void startFingerPaint (View v){   
    	//Read channel name
    	EditText channelNameEditText = (EditText)findViewById(R.id.editText2);
    	String channelName = channelNameEditText.getText().toString();
    	//Start intent
    	Intent intent = new Intent(this, DrawingActivity.class);
    	intent.putExtra("CHANNEL", channelName);
		startActivity(intent);
    }
}
