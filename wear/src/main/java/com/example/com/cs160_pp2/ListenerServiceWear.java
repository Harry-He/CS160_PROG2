package com.example.com.cs160_pp2;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Wearable listener service for data layer messages
 * https://github.com/LarkspurCA/WearableMessage/blob/master/wear/src/main/java/com/androidweardocs/wearablemessage/ListenerService.java
 * Created by michaelHahn on 1/11/15.
 */
public class ListenerServiceWear extends WearableListenerService {

    public static final String BROADCAST = "ListenerServiceBroadcastWear";
    public static final String AMPTITUDE_BROADCAST = "ListenerAmptitudeBroadcast";
    private static final String MESSAGE_PATH = "/message_mobile_to_wear";
    private static final String AMPTITUDE_PATH = "amptitude_mobile_to_wear";
    private static final String TAG = "ListenerServiceWear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            //final String message = new String(messageEvent.getData());
            Log.d(TAG, "Wear receives the data");
            //Log.d(TAG, "Message path received on wear is: " + messageEvent.getPath());
            //Log.d(TAG, "Message received on wear is: " + message);
            Intent intent = new Intent(BROADCAST);
            byte[] dataByte = messageEvent.getData();
            Log.d(TAG, Integer.toString(dataByte.length));
            intent.putExtra("data", messageEvent.getData());
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        } else if (messageEvent.getPath().equals(AMPTITUDE_PATH)) {
            Log.d(TAG, "Wear receives the amptitude data");
            Intent intent = new Intent(AMPTITUDE_BROADCAST);
            byte[] dataByte = messageEvent.getData();
            Log.d(TAG, Integer.toString(dataByte.length));
            intent.putExtra("amptitude", messageEvent.getData());
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
