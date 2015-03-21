package com.wordpress.skinberry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.wordpress.skinberry.app.AppController;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class SettingsActivity extends Activity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())

                .commit();
        //Get a Tracker (should auto-report)
        Tracker t = ((AppController)this.getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    public static class SettingsFragment extends PreferenceFragment {
        Preference notify_me, time, background, facebook_login, facebook_logout,
                twitter_login, twitter_logout, about, check;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.activity_settings);

            addPreferencesAction();
        }

        public void addPreferencesAction(){
            /////////////////Status Notification Preference
            notify_me = (Preference) findPreference("statusNotifications");
            notify_me.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)) {
                        AppController.getInstance().getPrefManger().notificationStatus(true);
                    } else {
                        AppController.getInstance().getPrefManger().notificationStatus(false);
                    }
                    return true;
                }
            });

            /////////////////About Preference
            about = (Preference) findPreference("about_app");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), AboutActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }

    }
}