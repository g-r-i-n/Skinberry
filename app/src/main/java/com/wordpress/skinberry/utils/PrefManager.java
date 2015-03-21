package com.wordpress.skinberry.utils;

import com.wordpress.skinberry.app.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

public class PrefManager {
	private static final String TAG = PrefManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "BlogPress";

	// Gallery directory name
	private static final String KEY_CATEGORIES_NAME = "categories";


	public PrefManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

	}

    /**
     * Storing lastUpdated Feed
     * */
    public void setFirstLaunched(boolean launched) {
        editor = pref.edit();

        editor.putBoolean("firstLaunched", launched);

        // commit changes
        editor.commit();
    }

    public Boolean getFirstLaunched() {
        return pref.getBoolean("firstLaunched", true);
    }

    /**
     * Storing lastUpdated Feed
     * */
    public void setLastID(Integer created) {
        editor = pref.edit();

        editor.putInt("lastUpdated", created);

        // commit changes
        editor.commit();
    }

    public Integer getLastID() {
        return pref.getInt("lastUpdated", 0);
    }


    ///////////////////////////////////////

    /**
     * Storing notification message status
     * */
    public void notificationStatus(Boolean status) {
        editor = pref.edit();

        editor.putBoolean("show_notification", status);

        // commit changes
        editor.commit();
    }

    public Boolean notificationEnabled() {
        return pref.getBoolean("show_notification", true);
    }


	/**
	 * Storing albums in shared preferences
	 * */
	public void storeCategories(List<Category> categories) {
		editor = pref.edit();
		Gson gson = new Gson();

		//Log.d(TAG, "Albums: " + gson.toJson(albums));

		editor.putString(KEY_CATEGORIES_NAME, gson.toJson(categories));

		// save changes
		editor.commit();
	}

	/**
	 * Fetching albums from shared preferences. Albums will be sorted before
	 * returning in alphabetical order
	 * */
	public List<Category> getCategories() {
		List<Category> albums = new ArrayList<Category>();

		if (pref.contains(KEY_CATEGORIES_NAME)) {
			String json = pref.getString(KEY_CATEGORIES_NAME, null);
			Gson gson = new Gson();
			Category[] albumArry = gson.fromJson(json, Category[].class);

			albums = Arrays.asList(albumArry);
			albums = new ArrayList<Category>(albums);
		} else
			return null;

		List<Category> allAlbums = albums;

		// Sort the albums in alphabetical order
		Collections.sort(allAlbums, new Comparator<Category>() {
			public int compare(Category a1, Category a2) {
				return a1.getTitle().compareToIgnoreCase(a2.getTitle());
			}
		});

		return allAlbums;

	}

	/**
	 * Comparing albums titles for sorting
	 * */
	public class CustomComparator implements Comparator<Category> {
		@Override
		public int compare(Category c1, Category c2) {
			return c1.getTitle().compareTo(c2.getTitle());
		}
	}
}
