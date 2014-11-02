package com.rolfwang.mobilesafe.domain;

public class BlackNumInfo {
	private int id;
	private String blackNum;
	private int mode;
	
	
	public BlackNumInfo() {
		super();
	}
	public BlackNumInfo(String balckNum, int mode) {
		super();
		this.blackNum = balckNum;
		this.mode = mode;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	@Override
	public String toString() {
		return "BlackNumInfo [balckNum=" + blackNum + ", mode=" + mode + "]";
	}
	public String getBlackNum() {
		return blackNum;
	}
	public void setBlackNum(String blackNum) {
		this.blackNum = blackNum;
	}
	
}
