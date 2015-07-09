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
public class ListenerServiceMobile extends WearableListenerService {

    public static final String BROADCAST = "ListenerServiceBroadcastMobile";
    private static final String MESSAGE_PATH = "/message_wear_to_mobile";
    private static final String TAG = "ListenerServiceMobile";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on mobile is: " + message);
            Intent intent = new Intent(BROADCAST);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
