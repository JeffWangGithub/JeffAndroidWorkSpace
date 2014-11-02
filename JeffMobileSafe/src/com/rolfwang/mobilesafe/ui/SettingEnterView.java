package com.rolfwang.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rolfwang.mobilesafe.R;

/**
 *自定义控件类，设置界面的点击记入控件
 *
 */
public class SettingEnterView extends RelativeLayout {

	private View view;
	private TextView tv_title;
	private TextView tv_des;

	public SettingEnterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SettingEnterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		//拿到自定义属性的值
		String title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.rolfwang.mobilesafe", "title");
		String des = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.rolfwang.mobilesafe", "des");
		//设置默认值
		tv_title.setText(title);
		tv_des.setText(des);
		
	}

	public SettingEnterView(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		view = View.inflate(getContext(), R.layout.setting_enter_view, this);
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		tv_des = (TextView) view.findViewById(R.id.tv_des);
	}
	
	
	//暴露一些方法
	
	public void setTitle(String title){
		tv_title.setText(title);		
	}
	
	public void setDes(String des){
		tv_des.setText(des);		
	}

}
