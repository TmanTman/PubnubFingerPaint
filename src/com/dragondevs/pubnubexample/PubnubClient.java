package com.dragondevs.pubnubexample;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dragondevs.pubnubexample.DrawingActivity.MyView;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class PubnubClient {
	
	Pubnub pubnub;
	//Only one call at a time is permitted
	String currentChannelName;
	//Handle to the current DrawArea to pass it the point
	MyView myView;
	//To make sure you don't receive your own doodles
	String username;
	//A general call back that will handle any published message
	Callback publishCallback;
	//To make calls to the UI from another thread
	Handler mHandler;
	
	private final String TAG = "PubnubFingerpaint";
	
	public PubnubClient(String channelName, MyView myView, Handler h) {
		
		final String subscribe_key = "***INSERT YOUR SUBSCRIBE KEY HERE***";
	    final String publish_key = "***INSERT YOUR PUBLISH KEY HERE***";
		
		//Initialize
        this.pubnub = new Pubnub(publish_key, subscribe_key);  
        Log.d(TAG, "Logging Pubnub in with channel name: " + channelName);
        subscribe(channelName, myView);
        this.mHandler = h;
        
        publishCallback = new Callback() {
 		   public void successCallback(String channel, Object response) {
 			   Log.d("PUBLISH",response.toString());
 		   }
 		   public void errorCallback(String channel, PubnubError error) {
 			   Log.d("PUBNUB",error.toString());
 		   }
        };
	}
	
	//Subscribe to the general broadcast channel
    public void subscribe (String channelName, MyView d) {
    	if (pubnub != null) {
    		currentChannelName = channelName;
    		myView = d;
    		username = RandomStringUtils.random(12, String.valueOf(System.currentTimeMillis()));
    	try {
    		   pubnub.subscribe(currentChannelName, new Callback() {
    		 
    		       @Override
    		       public void connectCallback(String channel, Object message) {
//    		           Log.d("PUBNUB","SUBSCRIBE : CONNECT on channel:" + channel
//    		                      + " : " + message.getClass() + " : "
//    		                      + message.toString());
    		       }
    		 
    		       @Override
    		       public void disconnectCallback(String channel, Object message) {
//    		           Log.d("PUBNUB","SUBSCRIBE : DISCONNECT on channel:" + channel
//    		                      + " : " + message.getClass() + " : "
//    		                      + message.toString());
    		       }
    		 
    		       public void reconnectCallback(String channel, Object message) {
//    		           Log.d("PUBNUB","SUBSCRIBE : RECONNECT on channel:" + channel
//    		                      + " : " + message.getClass() + " : "
//    		                      + message.toString());
    		       }
    		 
    		       //This is what gets called when a message is received
    		       @Override
    		       public void successCallback(String channel, Object message) {
//    		           Log.d("PUBNUB","SUBSCRIBE RECEIVED : " + channel + " : "
//    		                      + message.getClass() + " : " + message.toString());
    		           JSONObject json = (JSONObject)message; 
    		           try {
    		        	   //Don't receive your own doodles 
	    		           if (!json.getString("TO").equals(username)) {	   		        	  
	        		           receive(json);
	    		           }
    		           } catch (JSONException e) {
    		        	   Log.d(TAG, "Exeption: " + e.toString());
    		           }
    		       }
    		 
    		       @Override
    		       public void errorCallback(String channel, PubnubError error) {
//    		           Log.d("PUBNUB","SUBSCRIBE : ERROR on channel " + channel
//    		                      + " : " + error.toString());
    		       }
    		     }
    		   );
    		 } catch (PubnubException e) {
    		   Log.d("PUBNUB",e.toString());
    		 }
    	}
    }

	public void send(float x, float y, int state) {
		JSONObject json = null;
		try {
			json = new JSONObject();
    		json.put("TO", username);
    		json.put("STATE", state);
    		json.put("X", x);
    		json.put("Y", y);
    	} catch (JSONException e) {
    		Log.d(TAG, "Error: " + e.toString());
    	}
		Log.d("PUBLISH", "Time Publish " + System.currentTimeMillis());
    	if (json != null) pubnub.publish(currentChannelName, json, publishCallback);
	}
	
	private void receive(JSONObject json) {
		if (json != null) {
			Message msg = null;
			try {
				msg = mHandler.obtainMessage();
			} catch (Exception e) {
				Log.d(TAG, "RECEIVED NULL FROM HANDLER");
			}
			if (msg != null) {
				msg.obj = json;
				mHandler.sendMessage(msg);
			} else Log.d(TAG, "RECEIVED NULL MESSAGE");
		} else Log.d(TAG, "RECEIVED NULL JSON");
	}
    	
}
