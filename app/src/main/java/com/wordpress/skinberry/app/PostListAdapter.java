package com.wordpress.skinberry.app;

import com.wordpress.skinberry.utils.PostImageView;
import com.wordpress.skinberry.R;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;


public class PostListAdapter extends BaseAdapter {	
	private Activity activity;
	private LayoutInflater inflater;
	private List<Post> postItems = new ArrayList<Post>();
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();
	Boolean isAdLoaded = false;
    private int lastPosition = -1;
	
	public PostListAdapter(Activity activity, List<Post> postItems) {
		this.activity = activity;
		this.postItems = postItems;
	}

	@Override
	public int getCount() {
		return postItems.size();
	}

	@Override
	public Object getItem(int location) {
		return postItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (inflater == null)
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.listview_item, null);

		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();

		TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView category = (TextView) convertView.findViewById(R.id.category);
        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        PostImageView postImageView = (PostImageView) convertView.findViewById(R.id.feedImage1);


		Post item = postItems.get(position);
		name.setText(Html.fromHtml(item.getName()));
        category.setText(item.getCategory());

        // Converting timestamp into x ago format

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(item.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        timestamp.setText(timeAgo);

		// Feed image
		if (item.getImge() != null) {
			postImageView.setImageUrl(item.getImge(), imageLoader);
			postImageView.setVisibility(View.VISIBLE);
			postImageView
					.setResponseObserver(new PostImageView.ResponseObserver() {
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


        lastPosition = position;

		return convertView;
	}

}