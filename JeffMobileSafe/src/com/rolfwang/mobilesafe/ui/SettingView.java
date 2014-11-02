package com.rolfwang.mobilesafe.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rolfwang.mobilesafe.R;

public class SettingView extends RelativeLayout {

	private TextView tv_title;
	private TextView tv_des;
	private CheckBox cb;
	private String titleValue;
	private String des_on;
	private String des_off;
	//使用自定义样式时，调用三个方法的构造方法
	public SettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	//在布局文件中使用自定义控件时，调用两个参数的构造方法
	public SettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//1,使用布局填充此View对象
		init();
		/*
		//2，获取自定义属性
		//获取自定义属性的  方式一
		titleValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.rolfwang.mobilesafe", "title");
		des_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.rolfwang.mobilesafe", "des_on");
		des_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.rolfwang.mobilesafe", "des_off");
		*/
		//获取自定义属性方式二：
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.com_rolfwang_mobilesafe_ui_SettingView);
		
		titleValue = typedArray.getString(R.styleable.com_rolfwang_mobilesafe_ui_SettingView_title);
		des_on = typedArray.getString(R.styleable.com_rolfwang_mobilesafe_ui_SettingView_des_on);
		des_off = typedArray.getString(R.styleable.com_rolfwang_mobilesafe_ui_SettingView_des_off);
		typedArray.recycle();//注意使用完typeArray之后，需要进行资源的释放
		
		//3，设置初始化属性数据，按照用户使用此自定义控件时填写的属性，
		setTitle(titleValue);
		if(cb.isChecked()){
			setDes(des_on);
		}else{
			setDes(des_off);
		}
	}

	//在java代码中使用自定义控件时，调用一个参数的构造方法
	public SettingView(Context context) {
		super(context);
		init();
	}
	/**
	 * 初始化的方法
	 */
	private void init() {
		/*
		//方式一：
		//1,使用布局文件填充一个view对象
		View view = View.inflate(getContext(), R.layout.setting_view, null);
		//2,将view对象添加到当前的布局文件中
		this.addView(view);*/
		
		//方式二
		View view = View.inflate(getContext(), R.layout.setting_view, this);//第三个参数指定，当前控件的父控件
		
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		tv_des = (TextView) view.findViewById(R.id.tv_des);
		cb = (CheckBox) view.findViewById(R.id.cb);
	}
	
	//向调用者暴露方法，一边调用者可以直接调用操作某些属性
	/**
	 * 设置条目的标题内容
	 * @param title
	 */
	public void setTitle(String title){
		tv_title.setText(title);		
	}
	
	/**
	 * 设置条目的描述内容
	 * @param des
	 */
	public void setDes(String des){
		tv_des.setText(des);
	}
	
	/**
	 * 返回选项是否被选中
	 * @return
	 */
	public boolean isChecked(){
		return cb.isChecked();
	}
	
	/**
	 * 设置选中状态
	 * @param isChecked
	 */
	public void setChecked(boolean isChecked){
		cb.setChecked(isChecked);
		if(isChecked){
			setDes(des_on);
		}else{
			setDes(des_off);
		}
	}
}
