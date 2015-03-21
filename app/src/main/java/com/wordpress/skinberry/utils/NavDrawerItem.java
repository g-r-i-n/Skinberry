package com.wordpress.skinberry.utils;

public class NavDrawerItem {

	private String categoryId, categoryTitle;
	// boolean flag to check for recent album
	private boolean isRecentCategory = false;

	public NavDrawerItem() {
	}

	public NavDrawerItem(String categoryId, String categoryTitle) {
		this.categoryId = categoryId;
		this.categoryTitle = categoryTitle;
	}

	public NavDrawerItem(String categoryId, String categoryTitle,
			boolean isRecentCategory) {
		this.categoryTitle = categoryTitle;
		this.isRecentCategory = isRecentCategory;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getTitle() {
		return this.categoryTitle;
	}

	public void setTitle(String title) {
		this.categoryTitle = title;
	}

	public boolean isRecentCategory() {
		return isRecentCategory;
	}

	public void setRecentCategory(boolean isRecentCategory) {
		this.isRecentCategory = isRecentCategory;
	}
}
