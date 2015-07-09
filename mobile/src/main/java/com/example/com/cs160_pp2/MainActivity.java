package com.example.com.cs160_pp2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import io.fabric.sdk.android.Fabric;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_CONSUMER_KEY = "3rRvwpDgzP5nONzf4TEzMEM1p";
    private static final String TWITTER_CONSUMER_SECRET = "3cIiSefpTIuUxxGYBqVYWHFsO4Rwwihynvom5XRMOERiH4AoaB";
    private static final String TWITTER_ACCESS_TOKEN = "3270142999-NK2v4LW8CckXBTaOPdg5JEU2DqEaXo37RKONPNL";
    private static final String TWITTER_ACCESS_TOKEN_SECRET = "kQvgA8k4NPow2X33hM8O9qcQntbEU4MtwVmKhzXySEbs7";
    private static final String TAG = "MainActivityMobile";
    /* http://stackoverflow.com/questions/20594936/communication-between-activity-and-service */
    // handler for received data from service
    private static final int REQUEST_TWITTER_COMPOSER = 100;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ListenerServiceMobile.BROADCAST)) {
                Log.d(TAG, "Start camera intent");
                final Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent2, REQUEST_IMAGE_CAPTURE);
            }  else if (intent.getAction().equals(SendMessageMobile.BROADCAST)) {
                /*
                    THIS PART NOT WORKING BUT NOT A IMPORTANT PART
                 */

                Log.d(TAG, "received message to show error notification");
                NotificationManager notificationManager = (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder notificationBuilder =
                        new Notification.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.ic_error_black_36dp)
                                .setContentTitle("Connection Error")
                                .setContentText("Cannot connect to watch")
                                .setPriority(Notification.PRIORITY_MAX);
                notificationManager.notify(1, notificationBuilder.build());
                //delayCancelNotification(NOTIFY_DELAY_ERROR, NOTIFY_ID_ERROR);
            }
        }
    };
    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new TweetComposer(), new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitter success");
                // Do something with result, which provides a TwitterSession for making API calls
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "twitter fail");
                exception.printStackTrace();
                // Do something on failure
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(ListenerServiceMobile.BROADCAST);
        filter.addAction(SendMessageMobile.BROADCAST);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        //searchTweet();
    }

    public void composeTweet(Uri image) {
        Intent intent = new TweetComposer.Builder(this)
                .text("#cs160excited").image(image)
                .createIntent();
        startActivityForResult(intent, REQUEST_TWITTER_COMPOSER);
    }

    /* https://colinyeoh.wordpress.com/2012/05/18/android-getting-image-uri-from-bitmap/ */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        Uri uri = Uri.parse(path);
        Log.d(TAG, uri.toString());
        return uri;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Very import line
        loginButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Log.d(TAG, "hasExtras");
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            composeTweet(getImageUri(getApplicationContext(), imageBitmap));
        } else if (requestCode == REQUEST_TWITTER_COMPOSER && resultCode == RESULT_OK) {
            Log.d(TAG, "twitter compose done");
            // Send new twitter with $cs160excited to watch.
            searchTweet();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


    /* http://stackoverflow.com/questions/8992964/android-load-from-url-to-bitmap */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    void searchTweet() {
        new Thread() {
            @Override
            public void run() {
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true).setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                        .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET)
                        .setOAuthAccessToken(TWITTER_ACCESS_TOKEN)
                        .setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);
                TwitterFactory tf = new TwitterFactory(cb.build());
                twitter4j.Twitter twitter = tf.getInstance();
                twitter4j.Query query = new twitter4j.Query("#cs160excited");
                QueryResult result = null;
                query.setResultType(Query.RECENT);
                try
                {
                    result = twitter.search(query);
                } catch (twitter4j.TwitterException e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "twitter search exception");
                }
                if (result == null) {
                    Log.d(TAG, "null search result");
                    return;
                }

                // Get the lastest tweet of #cs160excited with picture.
                Status tweetToBeShow = null;
                for (Status tweet : result.getTweets()) {
                    if (tweet.getMediaEntities().length > 0 && tweetToBeShow == null)
                        tweetToBeShow = tweet;
                    else if (tweet.getMediaEntities().length > 0
                            && tweetToBeShow.getCreatedAt().compareTo(tweet.getCreatedAt()) <= 0) {
                        tweetToBeShow = tweet;
                    }
                    if (tweetToBeShow != null) {
                        Log.d(TAG, Integer.toString(tweetToBeShow.getMediaEntities().length));
                        Log.d(TAG, "tweetfectch   " + tweetToBeShow.getText());
                    }
                }

                if (tweetToBeShow == null) {
                    Log.d(TAG, "null tweetToBeShow");
                    return;
                }
                String text = tweetToBeShow.getUser().getName();
                Log.d(TAG, "tweet   " + text + ":" + tweetToBeShow.getText());
                twitter4j.MediaEntity[] entity = tweetToBeShow.getMediaEntities();
                if (entity.length != 0) {
                    String url = tweetToBeShow.getMediaEntities()[0].getMediaURL();
                    Log.d(TAG, "tweet picture     " + url);
                    Bitmap bitmap = getBitmapFromURL(url);

                    // Send text and bitmap data to wear
                    Intent intent = new Intent(MainActivity.this, SendMessageMobile.class);

                    int sizeOfText = text.length();
                    Log.d(TAG, Integer.toString(sizeOfText));
                    byte[] sizeOfTextByte = ByteBuffer.allocate(4).putInt(sizeOfText).array();
                    byte[] textByte = text.getBytes();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bitmapByte = stream.toByteArray();

                    byte[] dataByte = new byte[sizeOfTextByte.length+textByte.length+bitmapByte.length];
                    for (int i = 0; i < 4; i++) {
                        dataByte[i] = sizeOfTextByte[i];
                    }
                    for (int i = 0; i < textByte.length; i++) {
                        dataByte[i+4] = textByte[i];
                    }
                    for (int i = 0; i < bitmapByte.length; i++) {
                        dataByte[i+4+textByte.length] = bitmapByte[i];
                    }
                    Log.d(TAG, Integer.toString(dataByte.length));
                    intent.putExtra("data", dataByte);
                    startService(intent);
                }
            }
        }.start();
    }
}
