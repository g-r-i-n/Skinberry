package com.wordpress.skinberry;

import com.android.volley.AuthFailureError;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.app.Category;
import com.wordpress.skinberry.utils.ConnectionDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final String TAG_CATEGORIES = "categories", TAG_TERM_ID = "term_id",
            TAG_TERM_NAME = "name";

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_splash);

        TextView versionTxt = (TextView) findViewById(R.id.AppVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionTxt.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Best effort
        }

        checkInternetConnection ();

        Tracker t = AppController.getInstance().getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    public void checkInternetConnection (){
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            loadCategoriesData();
        } else {
            showAlertDialog(SplashActivity.this, getString(R.string.no_internet), getString(R.string.no_internet_message), false);
        }
    }

    public void loadCategoriesData(){
        // Categories request to get list of featured categories
        String url = Const.URL_BLOG_CATEGORIES;


        // Preparing volley's json object request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                List<Category> categories = new ArrayList<Category>();

                try {
                    if (response.has("error")) {
                        String error = response.getString("error");
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }else {
                        // Parsing the json response
                        JSONArray entry = response.getJSONArray(TAG_CATEGORIES);

                        // loop through categories and add them to album
                        // list
                        for (int i = 0; i < entry.length(); i++) {
                            JSONObject catObj = (JSONObject) entry.get(i);
                            // album id
                            String catID = catObj.getString(TAG_TERM_ID);

                            // album title
                            String catTitle = catObj.getString(TAG_TERM_NAME);

                            Category category = new Category();
                            category.setId(catID);
                            category.setTitle(catTitle);

                            // add album to list
                            categories.add(category);
                        }

                        // Store categories in shared pref
                        AppController.getInstance().getPrefManger().storeCategories(categories);

                        // String the main activity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                    // closing spalsh activity
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "System Error: " + error.getMessage());

                // show error toast
                Toast.makeText(getApplicationContext(), getString(R.string.server_unavailable), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "System Error: " + error.getMessage(), Toast.LENGTH_LONG).show();

                // closing spalsh activity
                finish();
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

        // disable the cache for this request, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Making the request
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        //builder.setIcon(R.drawable.icon);
        builder.setMessage(message);
        builder.setNegativeButton("Re-check", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkInternetConnection();
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }


    private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(), SplashActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),R.drawable.ic_launcher));
        addIntent.putExtra("duplicate", false);

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }




}
