package com.wordpress.skinberry.Services;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.wordpress.skinberry.MainActivity;
import com.wordpress.skinberry.R;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.utils.ConnectionDetector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BroadcastService extends Service {
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onCreate() {

        // TODO Auto-generated method stub
        super.onCreate();
            ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
            Boolean isInternetPresent = false;
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {

                // cancel if already existed
                if(mTimer != null) {
                    mTimer.cancel();
                } else {
                    // recreate new
                    mTimer = new Timer();
                }
                // schedule task
                mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, Const.UpdateCheckIn);

            } else {
                stopSelf();
            }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }


    @SuppressWarnings("deprecation")
    protected void PushNotification(String title, String message) {
        String ns = Context.NOTIFICATION_SERVICE;
        int NOTIFICATION_ID = 1;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.ic_launcher;
        CharSequence tickerText = message;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        Context context = this;
        CharSequence contentTitle = title;
        CharSequence contentText = message;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);
        notification.defaults |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void sendBroadcastMsg(boolean Msg) {
        Intent i = new Intent();
        i.setAction(Const.PACKAGE_INTENT);
        i.putExtra("status", Msg);
        this.sendBroadcast(i);
    }



    class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    String url = Const.URL_RECENTLY_ADDED;
                    // making fresh volley request and getting json
                    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                parseJsonFeed(response);
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                        }
                    }) {

                        /**
                         * Passing some request headers
                         * */
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json");
                            headers.put("ApiKey", Const.AuthenticationKey);
                            return headers;
                        }

                    };
                    // Adding request to volley request queue
                    AppController.getInstance().addToRequestQueue(jsonReq);
                }
            });
        }



        private void parseJsonFeed(JSONObject response) {
            Integer lastId = AppController.getInstance().getPrefManger().getLastID();
            Boolean hasNewUpdate = false;
            Integer mostRecentUpdate = 0;
            Integer numOfUpdate = 0;
            //Toast.makeText(getApplicationContext(),"Requested",Toast.LENGTH_LONG).show();
            try {
                if (response.has("error")) {
                    String error = response.getString("error");
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                }else {
                    JSONArray feedArray = response.getJSONArray("feed");
                    for (int i = 0; i < feedArray.length(); i++) {
                        JSONObject feedObj = (JSONObject) feedArray.get(i);
                        Integer feedUpdate = feedObj.getInt("id");
                        if (feedUpdate > lastId) {
                            numOfUpdate++;
                            if (hasNewUpdate == false) hasNewUpdate = true;
                            if (mostRecentUpdate < feedUpdate) mostRecentUpdate = feedUpdate;
                        }
                    }

                    if (hasNewUpdate) {
                        AppController.getInstance().getPrefManger().setLastID(mostRecentUpdate);
                        if (AppController.getInstance().getPrefManger().notificationEnabled()) {
                            PushNotification(getString(R.string.notification_title), String.format(getString(R.string.notification_msg), numOfUpdate));
                            sendBroadcastMsg(true);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //stopSelf();
        }


        private String getDateTime() {
            // get date time in custom format
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
            return sdf.format(new Date());
        }

    }
}
