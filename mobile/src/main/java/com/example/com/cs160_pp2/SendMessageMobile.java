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

public class SendMessageMobile extends IntentService {

    private static final String TAG = "SendMessageMobile";
    private static final String MESSAGE_PATH = "/message_mobile_to_wear";
    private static final String AMPTITUDE_PATH = "amptitude_mobile_to_wear";
    private static final String MESSAGE = "CS160_EXCITMENT_MOBILE_TO_WEAR";
    public static final String BROADCAST = "SendMessageBroadcastMobile";
    private GoogleApiClient mGoogleApiClient;
    public SendMessageMobile() {
        super("SendMessageMobile");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            googleApiInit();
            mGoogleApiClient.connect();
            byte[] data = (byte[])intent.getExtras().get("data");
            Log.d(TAG, "Twitter text and images are sending from phone to wear");
            if (data != null)
                sendMessage(MESSAGE_PATH, data);
            else {
                data = (byte[])intent.getExtras().get("amptitude");
                sendMessage(AMPTITUDE_PATH, data);
            }
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
    void sendMessage(String messagePath, byte[] message) {
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
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), messagePath,
                            message).await();
            if (result.getStatus().isSuccess()) {
                isConnectionGood = true;
                if (messagePath == AMPTITUDE_PATH)
                    Log.d(TAG, "amptitude sent");
                Log.d(TAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.d(TAG, "ERROR: mobile connected, but failed to send Message");
            }
        }

        if (!isConnectionGood) {
            Intent intent = new Intent(BROADCAST);
            Log.d(TAG, "Send ConnectionError from phone to wear to MainActivityMobile");
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }

    }
}
