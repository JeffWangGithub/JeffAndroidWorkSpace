package com.rolfwang.mobilesafe.domain;

import android.graphics.drawable.Drawable;

public class TaskInfo {
	
	private String name;
	private String packageName;
	private Drawable icon;
	private boolean isUser;
	private long mem;
	private boolean isChecked;
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public boolean isUser() {
		return isUser;
	}
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	public long getMem() {
		return mem;
	}
	public void setMem(long mem) {
		this.mem = mem;
	}
	@Override
	public String toString() {
		return "TaskInfo [name=" + name + ", packageName=" + packageName
				+ ", isUser=" + isUser + ", mem=" + mem
				+ "]";
	}
	
	
	

}
