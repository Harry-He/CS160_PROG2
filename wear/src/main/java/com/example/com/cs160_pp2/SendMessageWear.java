package com.example.com.cs160_pp2;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SendMessageWear extends IntentService {

    private static final String TAG = "SendMessageWear";
    private static final String MESSAGE_PATH = "/message_wear_to_mobile";
    private static final String MESSAGE = "CS160_EXCITMENT_WEAR_TO_MOBILE";
    public static final String BROADCAST = "SendMessageBroadcastWear";
    private GoogleApiClient mGoogleApiClient;

    public SendMessageWear() {
        super("SendMessageWear");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "service has run");
        if (intent != null) {
            Log.d(TAG, "service has run run");
            googleApiInit();
            mGoogleApiClient.connect();
            sendMessage(MESSAGE);
        }
    }

    void googleApiInit() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
    }


    /*
    https://github.com/LarkspurCA/WearableMessage/blob/master/mobile/src/main/java/com/androidweardocs/wearablemessage/MessageActivity.java
 */
    void sendMessage(String message) {
        Log.d(TAG, "message has started to send");
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(mGoogleApiClient).await();
        if (nodes.getNodes().isEmpty()) {
            Log.d(TAG, "Cannot connect to mobile");
        } else {
            Log.d(TAG, "mobile connected");
        }
        boolean isConnectionGood = false;
        for (Node node : nodes.getNodes()) {
            Log.d(TAG, "sending to one node");
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), MESSAGE_PATH,
                            message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                isConnectionGood = true;
                Log.d(TAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.d(TAG, "ERROR: mobile connected, but failed to send Message");
            }
        }

        if (!isConnectionGood) {
            Intent intent = new Intent(BROADCAST);
            Log.d(TAG, "Send ConnectionError from wear to phone to MainActivityWear");
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }
    }


}
