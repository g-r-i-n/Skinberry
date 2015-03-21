package com.wordpress.skinberry;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.wordpress.skinberry.app.AppController;
import com.wordpress.skinberry.app.Const;
import com.wordpress.skinberry.utils.PostImageView;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.ads.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/////////////////////////////////////////////

public class PostViewActivity extends Activity {
	private static final String TAG = PostViewActivity.class.getSimpleName();
	public static final String TAG_SEL_POST_ID = "post_id";
	public static final String TAG_SEL_POST_TITLE = "post_title";
	private String selectedPostID, selectedPostTitle;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	private TextView post_name;
	private TextView post_content;
    private TextView post_author;
	private PostImageView postImageView;
	private String post_image, objURL;
	private TextView timestamp;
	private NetworkImageView profilePic;
    private ShareActionProvider mShareActionProvider;
    private WebView post_contentHTML;

    private Spanned spannedContent;
    private ProgressBar pbLoader;
    private LinearLayout llayout;

    private Integer OC = 0;
    private Tracker t;

    //----------Exit and Banner Ad----------------------------------
    private InterstitialAd interstitial;
    private AdView mAdView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_view);

        //Get a Tracker (should auto-report)
        t = ((AppController)this.getApplication()).getTracker(AppController.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());

        if (Const.ADMOBService_ACTIVE) {

            //----------Exit Ad----------------------------------
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId(getString(R.string.unit_id_interstitial));
            AdRequest adRequestInterstitial = new AdRequest.Builder().build();
            interstitial.loadAd(adRequestInterstitial);
            //----------Exit Ad----------------------------------


            ////Standard Banner
            mAdView = (AdView) findViewById(R.id.adView);
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

        pbLoader = (ProgressBar) findViewById(R.id.pbLoader);
        pbLoader.setVisibility(View.VISIBLE);

        llayout = (LinearLayout) findViewById(R.id.LoaderGroup);
        llayout.setVisibility(View.GONE);

		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();
		
		post_name = (TextView) findViewById(R.id.name);
		timestamp = (TextView) findViewById(R.id.timestamp);

		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/GenR102.ttf");
		post_name.setTypeface(font);

        post_content = (TextView) findViewById(R.id.txtStatusMsg);
        post_contentHTML = (WebView) findViewById(R.id.txtStatusMsgHTML);
        if (!Const.ShowPostAsWebView) {
            post_content.setMovementMethod(LinkMovementMethod.getInstance());
            Typeface font_postcontent = Typeface.createFromAsset(getAssets(), "fonts/OSRegular.ttf");
            post_content.setTypeface(font_postcontent);
        }else{
            post_contentHTML.setVisibility(View.VISIBLE);
            WebSettings webSettings = post_contentHTML.getSettings();
            post_contentHTML.getSettings().setJavaScriptEnabled(true);
            post_contentHTML.getSettings().setAllowContentAccess(true);
            post_contentHTML.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            post_contentHTML.getSettings().setLoadsImagesAutomatically(true);
            post_contentHTML.getSettings().setDefaultTextEncodingName("utf-8");
            post_contentHTML.getSettings().setUseWideViewPort(true);
            post_contentHTML.getSettings().setLoadWithOverviewMode(true);
            post_contentHTML.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            post_contentHTML.setWebChromeClient(new WebChromeClient());
        }

        post_author = (TextView) findViewById(R.id.post_author);


        postImageView = (PostImageView) findViewById(R.id.feedImage1);
		profilePic = (NetworkImageView) findViewById(R.id.profilePic);
		
		if (savedInstanceState == null) {
		    if(getIntent().getExtras() == null) {
		    	selectedPostID = null;
		    	selectedPostTitle = "Unknown Error";
		    } else {
		    	selectedPostID = getIntent().getExtras().getString(TAG_SEL_POST_ID);
		    	selectedPostTitle = getIntent().getExtras().getString(TAG_SEL_POST_TITLE);
		    }
		} else {
			selectedPostID = (String) savedInstanceState.getSerializable(TAG_SEL_POST_ID);
			selectedPostTitle = (String) savedInstanceState.getSerializable(TAG_SEL_POST_TITLE);
		}
		
		setTitle(selectedPostTitle);
		// Enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
	    TextView yourTextView = (TextView) findViewById(titleId);
	    yourTextView.setTextColor(getResources().getColor(R.color.white));
	    
	    
	    
	    //Requesting The Story
	    String url = null;
		url = Const.URL_STORY_PAGE.replace("_STORY_ID_", selectedPostID.replace("P", ""));
		
		// making fresh volley request and getting json
		JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET, url, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				VolleyLog.d(TAG, "Response: " + response.toString());
				if (response != null) {
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        }else {
                            parseJsonFeed(response);
                        }
                    }catch (JSONException es) {
                        es.printStackTrace();
                        Toast.makeText(getApplicationContext(), es.getMessage(), Toast.LENGTH_LONG).show();
                    }
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
		
	}


	/**
	 * Parsing json reponse and passing the data to feed view list adapter
	 * */
	private void parseJsonFeed(JSONObject feedObj) {
		try {
			post_name.setText(feedObj.getString("name"));

            if (!Const.ShowPostAsWebView) {
                post_contentHTML.setVisibility(View.GONE);
                URLImageParser p = new URLImageParser(post_content, this);
                spannedContent = Html.fromHtml(feedObj.getString("story_content"), p, null);
                post_content.setText(trimTrailingWhitespace(spannedContent));
            }else{
                post_content.setVisibility(View.GONE);
                post_contentHTML.setVisibility(View.VISIBLE);

                String post_con = "<!DOCTYPE html>" +
                        "<html lang=\"en\">" +
                        "  <head>" +
                        "    <meta charset=\"utf-8\">" +
                        "  </head>" +
                        "  <body>" +
                        "    #content# " +
                        "  </body>" +
                        "</html>";
                try {
                    InputStream in_s = getResources().openRawResource(R.raw.post_format);
                    byte[] b = new byte[in_s.available()];
                    in_s.read(b);
                    post_con = new String(b);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                post_con = post_con.replace("#title#", feedObj.getString("name"));
                post_con = post_con.replace("#content#", feedObj.getString("story_content"));
                //post_contentHTML.loadData(post_con, "text/html; charset=utf-8", "utf-8");
                post_contentHTML.loadDataWithBaseURL( null,
                        post_con,
                        "text/html",
                        "UTF-8",
                        null);
            }

            post_author.setText(feedObj.getString("author"));
			getActionBar().setSubtitle("By " + feedObj.getString("author"));

			post_image = feedObj.getString("image");
            objURL = feedObj.getString("url");

            //Setting Up a Share Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, feedObj.getString("name") + " - " + feedObj.getString("url"));
            setShareIntent(shareIntent);

            //Button Click
            Button viewWeb = (Button) findViewById(R.id.btnViewWeb);
            viewWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToUrl (objURL);
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("External Browser")
                            .setAction("Post Name")
                            .setLabel(selectedPostTitle)
                            .build());
                }
            });


			// Converting timestamp into x ago format
			CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
					Long.parseLong(feedObj.getString("timeStamp")),
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
			timestamp.setText(timeAgo);
			
			profilePic.setImageUrl(feedObj.getString("profilePic"), imageLoader);


			loadConfig();
            pbLoader.setVisibility(View.GONE);
            llayout.setVisibility(View.VISIBLE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		getActionBar().setTitle(title);
	}

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }


    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.post_view, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	// Respond to the action bar's Up/Home button
		    case android.R.id.home:
		        NavUtils.navigateUpFromSameTask(this);
		        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int swidth = size.x;
        int height = size.y;

        int new_height = 0;
        new_height = swidth * postImageView.getLayoutParams().height / postImageView.getLayoutParams().width;


        postImageView.getLayoutParams().height = new_height;
        postImageView.getLayoutParams().width = swidth;

        if (!Const.ShowPostAsWebView) {
            //////////
            post_content.setMinimumHeight(0);
            post_content.requestLayout();
            post_content.invalidate();
            post_content.setText(post_content.getText());
        }else {

            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int hHTML = LinearLayout.LayoutParams.MATCH_PARENT;
            post_contentHTML.setLayoutParams(new LinearLayout.LayoutParams(width, hHTML));
        }

	}
	
	private void loadConfig(){
		if (post_image != null) {
			postImageView.setImageUrl(post_image, imageLoader);
			postImageView.setVisibility(View.VISIBLE);
			postImageView.setResponseObserver(new PostImageView.ResponseObserver() {
				@Override
				public void onError() {
				}

				@Override
				public void onSuccess() {
				}
			});
		} else {
			postImageView.setVisibility(View.GONE);
		}
	}

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }


	public class URLDrawable extends BitmapDrawable {
	    // the drawable that you need to set, you could set the initial drawing
	    // with the loading image if you need to
	    protected Drawable drawable;

	    @Override
	    public void draw(Canvas canvas) {
	        // override the draw to facilitate refresh function later
	        if(drawable != null) {
	            drawable.draw(canvas);
	        }
	    }
	}



	public class URLImageParser implements ImageGetter {
	    Context c;
	    View container;

	    /***
	     * Construct the URLImageParser which will execute AsyncTask and refresh the container
	     * @param t
	     * @param c
	     */
	    public URLImageParser(View t, Context c) {
	        this.c = c;
	        this.container = t;
	    }

	    public Drawable getDrawable(String source) {
	        URLDrawable urlDrawable = new URLDrawable();

	        // get the actual source
	        ImageGetterAsyncTask asyncTask =
	            new ImageGetterAsyncTask( urlDrawable);

	        asyncTask.execute(source);

	        // return reference to URLDrawable where I will change with actual image from
	        // the src tag
	        return urlDrawable;
	    }

	    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
	        URLDrawable urlDrawable;

	        public ImageGetterAsyncTask(URLDrawable d) {
	            this.urlDrawable = d;
	        }

	        @Override
	        protected Drawable doInBackground(String... params) {
	            String source = params[0];
	            return fetchDrawable(source);
	        }

	        @Override
	        protected void onPostExecute(Drawable result) {
                try {
                    // set the correct bound according to the result from HTTP call
                    urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
                            + result.getIntrinsicHeight());

                    // change the reference of the current drawable to the result
                    // from the HTTP call
                    urlDrawable.drawable = result;

                    // redraw the image by invalidating the container

                    URLImageParser.this.container.setMinimumHeight((URLImageParser.this.container.getHeight()+ result.getIntrinsicHeight()));
                    URLImageParser.this.container.requestLayout();
                    URLImageParser.this.container.invalidate();
                    post_content.setText(post_content.getText());
                } catch (NullPointerException ex){
                    urlDrawable.setBounds(0,0,0,0);
                    urlDrawable.drawable = result;

                }
	        }

	        /***
	         * Get the Drawable from URL
	         * @param urlString
	         * @return
	         */
	        public Drawable fetchDrawable(String urlString) {
	            try {
	                InputStream is = fetch(urlString);
	                Drawable drawable = Drawable.createFromStream(is, "src");
	                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
	                        + drawable.getIntrinsicHeight());
	                return drawable;
	            } catch (Exception e) {
	                return null;
	            }
	        }

	        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpGet request = new HttpGet(urlString);
	            HttpResponse response = httpClient.execute(request);
	            return response.getEntity().getContent();
	        }
	    }
	}

    @Override
    public void onBackPressed() {
        displayInterstitial();
        super.onBackPressed();
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }
}

