package com.wordpress.skinberry;

import com.android.volley.AuthFailureError;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.app.PostListAdapter;
import com.wordpress.skinberry.utils.ConnectionDetector;
import com.wordpress.skinberry.app.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class GridFragment extends Fragment {
	private static final String TAG = GridFragment.class.getSimpleName();
	private ListView listView;
	private PostListAdapter listAdapter;
	private List<Post> feedItems;
	private static final String bundleCategoryId = "categoryId";
    private static final String bundleCategoryName = "categoryName";
	private String selectedCategoryId, selectedCategoryName;
	private ProgressBar pbLoader;
	private TextView pbNoInternet;
    private TextView pbNoResult;

    private AdView mAdView;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;
	
	public GridFragment() {
	}

	public static GridFragment newInstance(String categoryId, String categoryName) {
		GridFragment f = new GridFragment();
		Bundle args = new Bundle();
		args.putString(bundleCategoryId, categoryId);
        args.putString(bundleCategoryName, categoryName);
        f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

		listView = (ListView) rootView.findViewById(R.id.list_view);
		listView.setVisibility(View.GONE);
		pbLoader = (ProgressBar) rootView.findViewById(R.id.pbLoader);
		pbLoader.setVisibility(View.VISIBLE);
		pbNoInternet = (TextView) rootView.findViewById(R.id.pbNoInternet);
		pbNoInternet.setVisibility(View.GONE);
        pbNoResult   = (TextView) rootView.findViewById(R.id.pbNoResult);
        pbNoResult.setVisibility(View.GONE);


        if (Const.ADMOBService_ACTIVE) {
            mAdView = (AdView) rootView.findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
        }

        //Feed Items
		feedItems = new ArrayList<Post>();
		listAdapter = new PostListAdapter(getActivity(), feedItems);
		listView.setAdapter(listAdapter);
		

		if (getArguments().getString(bundleCategoryId) != null) {
			selectedCategoryId = getArguments().getString(bundleCategoryId);
		} else {
			selectedCategoryId = null;
		}

        if (getArguments().getString(bundleCategoryName) != null) {
            selectedCategoryName = getArguments().getString(bundleCategoryName);
        } else {
            selectedCategoryName = null;
        }

		checkInternetConnection();

		return rootView;
	}
	
	public void checkInternetConnection (){
		// creating connection detector class instance
        cd = new ConnectionDetector(this.getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
        	loadCategoriesData();
        } else {
        	// Hide the loader, make grid visible
			pbLoader.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
			pbNoInternet.setVisibility(View.VISIBLE);
        }
	}
	
	public void loadCategoriesData(){
		String url = null;
        Tracker t = AppController.getInstance().getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
		if (selectedCategoryId == null) {
			url = Const.URL_RECENTLY_ADDED;
            // Build and Send the Analytics Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.recently_added))
                    .setAction("View")
                    .build());
		} else {
			url = Const.URL_CATEGORY_POST.replace("_CAT_ID_", selectedCategoryId);
            // Build and Send the Analytics Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(selectedCategoryName)
                    .setAction("View")
                    .build());
		}

        //Toast.makeText(getActivity(),url,Toast.LENGTH_LONG).show();

        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        }else {
                            parseJsonFeed(response);
                        }
                    }catch (JSONException es) {
                        es.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    }
                }else{
                    pbLoader.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    pbNoResult.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
            /** Passing some request headers **/
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

		// Grid item select listener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				// On selecting the grid image, we launch fullscreen activity
				Intent i = new Intent(getActivity(), PostViewActivity.class);

				// Passing selected image to fullscreen activity
				Post photo = feedItems.get(position);
				i.putExtra(PostViewActivity.TAG_SEL_POST_ID, "P"+photo.getId());
				i.putExtra(PostViewActivity.TAG_SEL_POST_TITLE, photo.getName());
				startActivity(i);
			}
		});
	}
	/**
	 * Parsing json reponse and passing the data to feed view list adapter
	 * */
	private void parseJsonFeed(JSONObject response) {
		try {
			JSONArray feedArray = response.getJSONArray("feed");

			for (int i = 0; i < feedArray.length(); i++) {
				JSONObject feedObj = (JSONObject) feedArray.get(i);

				Post item = new Post();
				item.setId(feedObj.getInt("id"));
				Log.d(TAG, "ID: " + feedObj.getInt("id"));
				item.setName(feedObj.getString("name"));
                item.setCategory(feedObj.getString("category"));

				// Image might be null sometimes
				String image = feedObj.isNull("image") ? null : feedObj
						.getString("image");
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
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}