package com.wordpress.skinberry;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.wordpress.skinberry.app.AppController;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_about);




        TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionTxt.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Best effort
        }

        //Get a Tracker (should auto-report)
        Tracker t = ((AppController)this.getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
