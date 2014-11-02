package com.rolfwang.mobilesafe.receiver;

import com.rolfwang.mobilesafe.service.WidgestService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MyAppWidgestProvider extends AppWidgetProvider {
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		//Widgest的增，删，更新，都会调用此方法
	}
	
	//第一个Widgest创建时会执行此方法
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		//第一个Widgest创建时，开启服务
		Intent intent = new Intent(context,WidgestService.class);
		context.startService(intent);		
	}

	//最后一个Widgest被删除是调用此方法
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		//最后一个Widgest删除时，停止服务
		Intent intent = new Intent(context,WidgestService.class);
		context.stopService(intent);
	}

	//当删除Widgest时，会调用此方法
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	//每一个Widgest创建时都会调用更新方法，并且默认更新时间到了的时候也会调用此方法
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
