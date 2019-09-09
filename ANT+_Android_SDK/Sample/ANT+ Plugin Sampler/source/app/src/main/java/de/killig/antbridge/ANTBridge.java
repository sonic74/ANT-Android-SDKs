package de.killig.antbridge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.dsi.ant.antplus.pluginsampler.Activity_Dashboard;
import com.dsi.ant.antplus.pluginsampler.R;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static androidx.core.content.FileProvider.getUriForFile;

public class ANTBridge extends Service {
    private static final String LOG_TAG = ANTBridge.class.getSimpleName();

    static NanoHTTPD httpServer;
    public static final int HTTPD_PORT = 8081;
    static TextView mTv;
    public static void startRC(TextView tv) {
        Log.d(LOG_TAG,"startRC()");
        mTv=tv;
        if(httpServer!=null) {
            httpServer.stop();
            httpServer=null;
        }
        if(httpServer==null) httpServer = new NanoHTTPD(HTTPD_PORT) {

            @Override
            public Response serve(IHTTPSession session) {
                Log.d(LOG_TAG, "serve({" + session.getUri() + "; " + session.getQueryParameterString()+ "; " + session.getParms());
                Map<String, String> hm=session.getParms();
                String key=hm.get("key");
                Log.d(LOG_TAG, "key="+key);
                try {
                    Response r=newFixedLengthResponse(mTv.getText().toString());
                    // Access to XMLHttpRequest at 'http://localhost:8081/' from origin 'https://sven.killig.de' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
                    r.addHeader("Access-Control-Allow-Origin", "*");
                    r.setMimeType("text/plain");
                    return r;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        try {
            /*KeyStore ks = KeyStore.getInstance("PKCS12");

            // get user password and file input stream
            char[] password = "password".toCharArray();

            ks.load(getAssets().open("keystore.jks"), password);
            KeyManagerFactory tmf = KeyManagerFactory.getInstance("X509");
            tmf.init(ks, password);
            httpServer.makeSecure(NanoHTTPD.makeSSLSocketFactory(ks, tmf), null);*/
            httpServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";
    public static void showJoyStick(Context context, String contentText, String subText) {
        Intent notificationIntent = new Intent(context, context.getClass());
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, /*Intent.FLAG_ACTIVITY_CLEAR_TASK*/0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(),NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(context.getText(R.string.app_name));
        builder.setContentText(contentText);
        builder.setSubText(subText);
        builder.setNumber(101);//?
        builder.setContentIntent(pendingIntent);
        //builder.setTicker(contentText);
        builder.setSmallIcon(R.drawable.ic_launcher);
        //builder.setLargeIcon(bm);
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            /*notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);*/
            notificationChannel.setSound(null,null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public static void upload(Context context, String filename) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File f=context.getFileStreamPath(filename);
        Uri uri=getUriForFile(context, "de.killig.fileprovider", f);
        Log.d(LOG_TAG, "uri="+uri+"  length:"+f.length());
//                    intentShareFile.setDataAndType(uri, "text/csv");
//                    intentShareFile.setData(uri);
//                    intentShareFile.setClipData(new ClipData());
        intentShareFile.setType("text/csv");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);
        intentShareFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intentShareFile, context.getText(R.string.menu_share)));
    }

    // https://www.thisisant.com/forum/viewthread/3831/#3908
    // fit_profile.hpp: "seconds since UTC 00:00 Dec 31 1989"
    /*private static readonly DateTime RefDateTime = new DateTime(1989, 12, 31, 0, 0, 0, DateTimeKind.Utc);
    protected static Nullable<DateTime> ToDateTime(UInt32 dateTimeValue)
    {
        if (dateTimeValue != FIT_UINT32_INVALID)
            return RefDateTime.AddSeconds(dateTimeValue);
        else
            return null;
    }*/


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand("+intent+","+flags+","+startId);
        String input = intent.getStringExtra("inputExtra");
        //createNotificationChannel();
        Intent notificationIntent = new Intent(this, Activity_Dashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        //do heavy work on a background thread

        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind("+intent);
        return null;
    }
}
