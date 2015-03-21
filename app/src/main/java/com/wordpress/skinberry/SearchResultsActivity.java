package com.wordpress.skinberry;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.*;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.app.Post;
import com.wordpress.skinberry.utils.ConnectionDetector;
import com.wordpress.skinberry.app.PostListAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SearchResultsActivity extends Activity {
	private static final String TAG = SearchResultsActivity.class.getSimpleName();
	private ListView listView;
	private PostListAdapter listAdapter;
	private List<Post> feedItems;
	private ProgressBar pbLoader;
	private TextView pbNoInternet;
	private TextView pbNoResult;
	private int pageNum;
	private String query;
	private int totalItems;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    Boolean isLoadingProgress = true;
    View footerView;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_grid);
        
        
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
	    TextView yourTextView = (TextView) findViewById(titleId);
	    yourTextView.setTextColor(getResources().getColor(R.color.white));
	    getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
	    getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_bg));
        
        footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loadmore, null, false);
        
        /*AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build(); 
        adView.loadAd(adRequest);*/
        
        pageNum = 1;
        
        listView = (ListView) findViewById(R.id.list_view);
		listView.setVisibility(View.GONE);
		listView.addFooterView(footerView);
		
		pbLoader = (ProgressBar) findViewById(R.id.pbLoader);
		pbLoader.setVisibility(View.VISIBLE);
		pbNoInternet = (TextView) findViewById(R.id.pbNoInternet);
		pbNoInternet.setVisibility(View.GONE);
		pbNoResult   = (TextView) findViewById(R.id.pbNoResult);
		pbNoResult.setVisibility(View.GONE);
		
		feedItems = new ArrayList<Post>();
		listAdapter = new PostListAdapter(SearchResultsActivity.this, feedItems);
		listView.setAdapter(listAdapter);
        
        // get the action bar
        ActionBar actionBar = getActionBar();
 
        // Enabling Back navigation on Action Bar icon
        actionBar.setDisplayHomeAsUpEnabled(true);
 
        handleIntent(getIntent());
        
        //Loading Bottom Ad
        final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //String deviceid = tm.getDeviceId();
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
		
		// Grid item select listener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				// On selecting the grid image, we launch fullscreen activity
				Intent i = new Intent(SearchResultsActivity.this, PostViewActivity.class);

				// Passing selected image to fullscreen activity
				Post photo = feedItems.get(position);
				i.putExtra(PostViewActivity.TAG_SEL_POST_ID, "P"+photo.getId());
				i.putExtra(PostViewActivity.TAG_SEL_POST_TITLE, photo.getName());
				startActivity(i);
			}
		});

        //On Scoll Event
		listView.setOnScrollListener(new OnScrollListener() {
			@Override
		    public void onScrollStateChanged(AbsListView view, int scrollState) {
		    }

		    @Override
		    public void onScroll(AbsListView view, int firstVisibleItem,
		                int visibleItemCount, int totalItemCount) {

		       final int lastItem = firstVisibleItem + visibleItemCount;
		       if (isLoadingProgress == false){
			       if (totalItemCount-1>=totalItems){
			    	   listView.removeFooterView(footerView);
			    	   return;
			       }
			       
			       if(lastItem == totalItemCount) {
			           //load more data
			    	   pageNum++;
			    	   loadSearchData(pageNum);
			       }
		       }
		    }
		});
    }
 
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
 
    /**
     * Handling intent data
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
    		getActionBar().setSubtitle(query);
            checkInternetConnection();
        }
 
    }
    
    
    
    public void checkInternetConnection (){
		// creating connection detector class instance
        cd = new ConnectionDetector(SearchResultsActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
        	loadSearchData(pageNum);
        } else {
        	// Hide the loader, make grid visible
			pbLoader.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			pbNoInternet.setVisibility(View.VISIBLE);
        }
	}
	
	public void loadSearchData(int pageNum){
		String url = null;
		isLoadingProgress = true;
		url = Const.URL_SEARCH_RESULT.replace("_SEARCH_KEYWORD_", query);
		url = url.replace("_PAGE_NO_", ""+pageNum);
		// making fresh volley request and getting json
		JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				VolleyLog.d(TAG, "Response: " + response.toString());
				if (response != null) {
					isLoadingProgress=false;
					parseJsonFeed(response);
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				VolleyLog.d(TAG, "Error: " + error.getMessage());
			}
		});

		// Adding request to volley request queue
		AppController.getInstance().addToRequestQueue(jsonReq);
		
	}
	/**
	 * Parsing json reponse and passing the data to feed view list adapter
	 * */
	private void parseJsonFeed(JSONObject response) {
        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG);
        try {
			totalItems = response.getInt("total_items");
			if (totalItems>0){
				JSONArray feedArray = response.getJSONArray("feed");

				for (int i = 0; i < feedArray.length(); i++) {
					JSONObject feedObj = (JSONObject) feedArray.get(i);

					Post item = new Post();
					item.setId(feedObj.getInt("id"));
					item.setName(feedObj.getString("name"));

					// Image might be null sometimes
					String image = feedObj.isNull("image") ? null : feedObj.getString("image");
					item.setImge(image);
					item.setStatus(feedObj.getString("status"));
					item.setProfilePic(feedObj.getString("profilePic"));
					item.setTimeStamp(feedObj.getString("timeStamp"));

					// url might be null sometimes
					String feedUrl = feedObj.isNull("url") ? null : feedObj.getString("url");
					item.setUrl(feedUrl);

					feedItems.add(item);
				}
	
				// notify data changes to list adapater
				listAdapter.notifyDataSetChanged();
				
				
				// Hide the loader, make grid visible
				pbLoader.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}else{
				if (pageNum==1){
					pbLoader.setVisibility(View.GONE);
					listView.setVisibility(View.GONE);
					pbNoResult.setVisibility(View.VISIBLE);
				}
			}
		} catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
            e.printStackTrace();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		getActionBar().setSubtitle(query);
	}
}