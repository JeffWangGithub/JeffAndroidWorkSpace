package com.rolfwang.mobilesafe.domain;

public class ContactInfo {

	// 电话号码
	private String num;
	private String name;
	private String email;
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public ContactInfo(String num, String name, String email) {
		super();
		this.num = num;
		this.name = name;
		this.email = email;
	}
	@Override
	public String toString() {
		return "ContactInfo [num=" + num + ", name=" + name + ", email="
				+ email + "]";
	}
	public ContactInfo() {
		super();
	}
	
	

}
