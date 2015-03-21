package com.wordpress.skinberry;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.wordpress.skinberry.Services.BroadcastService;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.app.Category;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.utils.NavDrawerItem;
import com.wordpress.skinberry.utils.NavDrawerListAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    SearchView searchView;

    // Navigation drawer title
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<Category> categoriesList;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private String CurrentOpen = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //registering the register to get the response of the service
        registerReceiver(this.myBroadCast, new IntentFilter(Const.PACKAGE_INTENT));

        //if (AppController.getInstance().getPrefManger().getFirstLaunched()){
            //Starting the Service for Push/Status Notification
            startService();
        //}

        //Get a Tracker (should auto-report)
        Tracker t = ((AppController)this.getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());


        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // Getting the albums from shared preferences
        categoriesList = AppController.getInstance().getPrefManger().getCategories();

        // Insert "Recently Added" in navigation drawer first position
        Category recentCategory = new Category(null,  getString(R.string.recently_added));

        categoriesList.add(0, recentCategory);


        // Loop through albums in add them to navigation drawer adapter
        for (Category a : categoriesList) {
            navDrawerItems.add(new NavDrawerItem(a.getId(), a.getTitle()));
        }

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // Setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);


        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) findViewById(titleId);
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // Enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }



    /**
     * Navigation drawer menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit (String query) {
                setSubtitle ("Search Results for: "+query);
                //Toast.makeText(getApplicationContext(), "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();
                menu.findItem(R.id.action_search).collapseActionView();

                CurrentOpen = "Search";
                Fragment fragment = SearchFragment.newInstance(query);
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                } else {
                    // error in creating fragment
                    Toast.makeText(getApplicationContext(),"Error in creating search fragment", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == R.id.menu_settings) {
            Intent settingsActivityIntent = new Intent();
            settingsActivityIntent.setClass(this, SettingsActivity.class);
            this.startActivityForResult(settingsActivityIntent, 111);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }





    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                // Recently added item selected
                // don't pass album id to home fragment
                CurrentOpen = "Recent";
                fragment = GridFragment.newInstance(null, getString(R.string.recently_added));
                break;

            default:
                // selected wallpaper category
                // send album id to home fragment to list all the wallpapers
                CurrentOpen = "Other";
                String categoryId = categoriesList.get(position).getId();
                String categoryName = categoriesList.get(position).getTitle();
                fragment = GridFragment.newInstance(categoryId, categoryName);
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setSubtitle(categoriesList.get(position).getTitle());
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Toast.makeText(getApplicationContext(),"Error in creating fragment", Toast.LENGTH_LONG).show();
        }
    }

    public void setSubtitle (CharSequence title) {
        mTitle = title;
        getActionBar().setSubtitle(mTitle);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(this.myBroadCast);
        super.onDestroy();
    }


    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    public void startService() {
        Intent start = new Intent(MainActivity.this, BroadcastService.class);
        if(startService(start)!=null){
            startService(start);
        }
    }

    BroadcastReceiver myBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(Const.PACKAGE_INTENT)) {
                if (CurrentOpen=="Recent") {
                    displayView(0);
                }
            }
        }
    };
}
