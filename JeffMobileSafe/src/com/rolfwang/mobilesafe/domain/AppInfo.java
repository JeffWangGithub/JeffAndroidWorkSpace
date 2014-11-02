package com.rolfwang.mobilesafe.domain;

import android.graphics.drawable.Drawable;

public class AppInfo {
	
	private String appPackageName;
	private String appVersion;
	private String appName;
	private Drawable appIcon;
	
	private boolean isSD;
	private boolean isUser;
	public String getAppPackageName() {
		return appPackageName;
	}
	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public boolean isSD() {
		return isSD;
	}
	public void setSD(boolean isSD) {
		this.isSD = isSD;
	}
	public boolean isUser() {
		return isUser;
	}
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	public Drawable getAppIcon() {
		return appIcon;
	}
	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
	@Override
	public String toString() {
		return "AppInfo [appPackageName=" + appPackageName + ", appVersion="
				+ appVersion + ", appName=" + appName + ", isSD=" + isSD
				+ ", isUser=" + isUser + "]";
	}

}
