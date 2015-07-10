package com.example.com.cs160_pp2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivityWear";
    private static final double SENSOR_DELTA_TOLERATE = 100.0;
    private static final int NOTIFY_DELAY_EXCITED = 10000;
    private static final int NOTIFY_DELAY_TWITTER = 10000;
    private static final int NOTIFY_DELAY_ERROR = 10000;
    private static final int NOTIFY_ID_EXCITED = 1;
    private static final int NOTIFY_ID_TWITTER = 2;
    private static final int NOTIFY_ID_ERROR = 3;
    private SensorManager mSensorManager;
    //private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private boolean sensorInitialized = false;
    private NotificationManager notificationManager;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ListenerServiceWear.BROADCAST)) {

                notificationManager.cancel(NOTIFY_ID_EXCITED);
                Log.d(TAG, "Start tweet searching");
                byte[] dataByte = (byte[])intent.getExtras().get("data");
                Log.d(TAG, dataByte[0] + " " + dataByte[1] + " " + dataByte[2] + " " + dataByte[3]);
                Log.d(TAG, Integer.toString(dataByte.length));
                int sizeOfText= ((dataByte[0] & 0xFF) << 24) | ((dataByte[1] & 0xFF) << 16)
                        | ((dataByte[2] & 0xFF) << 8) | (dataByte[3] & 0xFF);
                Log.d(TAG, Integer.toString(sizeOfText));

                byte[] textByte = new byte[sizeOfText];
                for (int i = 0; i < sizeOfText; i++) {
                    textByte[i] = dataByte[i+4];
                }
                String text = new String(textByte);

                byte[] bitmapByte = new byte[dataByte.length - 4 - sizeOfText];
                for (int i = 0; i < dataByte.length - 4 - sizeOfText; i++) {
                    bitmapByte[i] = dataByte[4+sizeOfText+i];
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);


                Log.d(TAG, "text to be notifed  " + text);

                Notification.Builder notificationBuilder =
                        new Notification.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.twitter128)
                                .setContentTitle(text)
                                .setContentText("IS ALSO EXCITED")
                                .setPriority(Notification.PRIORITY_MAX)
                                .setLargeIcon(bitmap);
                notificationManager.notify(NOTIFY_ID_TWITTER, notificationBuilder.build());

                delayCancelNotification(NOTIFY_DELAY_TWITTER, NOTIFY_ID_TWITTER);
            } else if (intent.getAction().equals(SendMessageWear.BROADCAST)) {
                Log.d(TAG, "received message to show error notification");
                notificationManager.cancel(NOTIFY_ID_EXCITED);
                Notification.Builder notificationBuilder =
                        new Notification.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.ic_error_black_36dp)
                                .setContentTitle("Connection Error")
                                .setContentText("Cannot connect to phone")
                                .setPriority(Notification.PRIORITY_MAX);
                notificationManager.notify(NOTIFY_ID_ERROR, notificationBuilder.build());
                delayCancelNotification(NOTIFY_DELAY_ERROR, NOTIFY_ID_ERROR);
            } else if (intent.getAction().equals(ListenerServiceWear.AMPTITUDE_BROADCAST)) {
                Log.d(TAG, "received amptitude to error notification");
                byte[] ampByte = (byte[])intent.getExtras().get("amptitude");
                int amp= ((ampByte[0] & 0xFF) << 24) | ((ampByte[1] & 0xFF) << 16)
                        | ((ampByte[2] & 0xFF) << 8) | (ampByte[3] & 0xFF);
                Log.d(TAG, String.valueOf(amp));
                excitingNotification();
                delayCancelNotification(NOTIFY_DELAY_EXCITED, NOTIFY_ID_EXCITED);
            }
        }
    };

    /*
        Code of sensor comes from
        http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
     */
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = Math.abs(mAccelCurrent - mAccelLast);
            //mAccel = mAccel * 0.9f + delta; // perform low-cut filter
            if (sensorInitialized && delta > SENSOR_DELTA_TOLERATE) {
                Log.d(TAG, "Sensor Changes" + x + " " + y + " " + z);
                excitingNotification();
                delayCancelNotification(NOTIFY_DELAY_EXCITED, NOTIFY_ID_EXCITED);
            } else {
                sensorInitialized = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Empty
        }
    };

    void delayCancelNotification(final int delay, final int id) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notificationManager.cancel(id);
                    }
                }, delay);

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ListenerServiceWear.BROADCAST);
        filter.addAction(SendMessageWear.BROADCAST);
        filter.addAction(ListenerServiceWear.AMPTITUDE_BROADCAST);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);
        finish();
    }

    void excitingNotification() {

        Intent viewIntent = new Intent(this, SendMessageWear.class);
        viewIntent.putExtra("send", "message");
        PendingIntent viewPendingIntent =
                PendingIntent.getService(this, 0, viewIntent, 0);
        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_photo_camera_black_36dp)
                        .setContentTitle("Excited?")
                        .setContentText("Take a picture")
                        .addAction(R.drawable.ic_photo_camera_black_36dp, "take pic", viewPendingIntent)
                        .setContentIntent(viewPendingIntent)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.drawable.excited));
        notificationManager.notify(NOTIFY_ID_EXCITED, notificationBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.
                getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        //bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


}
