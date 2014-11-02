package com.rolfwang.mobilesafe.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.domain.TaskInfo;
import com.rolfwang.mobilesafe.engine.TaskProvider;
import com.rolfwang.mobilesafe.receiver.MyAppWidgestProvider;
import com.rolfwang.mobilesafe.utils.MemoryUtils;
import com.rolfwang.mobilesafe.utils.TaskUtils;

public class WidgestService extends Service {

	private AppWidgetManager appWidgetManager;
	private ComponentName componentName;
	private RemoteViews remoteView;
	private ClearReceiver clearReceiver;
	private Timer timer;
	private ScreenOnOffReceiver screenOnOffReceiver;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * 接收桌面发送的清理进程的广播，进行进程的清理
	 *
	 */
	class ClearReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收广播，进行清理
//			System.out.println("Widgest service 接收到桌面发来的广播");
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<TaskInfo> runningTaskInfos = TaskProvider.getRunningTaskInfos(getApplicationContext());
			for (TaskInfo taskInfo : runningTaskInfos) {
				if(getApplicationContext().getPackageName().equals(taskInfo.getPackageName())){
					//如果是当前应用，则不进行清理
					continue;
				}
				activityManager.killBackgroundProcesses(taskInfo.getPackageName());				
			}
			updateWidgetsText();
			
		}
	}
	
	class ScreenOnOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			System.out.println("屏幕关闭了.............");
			String action = intent.getAction();
			if("android.intent.action.SCREEN_OFF".equals(action)){
				if(timer != null){
					timer.cancel();
					timer = null;
				System.out.println("timer取消了.............");
				}				
			}else if("android.intent.action.SCREEN_ON".equals(action)){
				updateWidgetsText();
				System.out.println("开始更新..............");
				
			}
		}
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
//		System.out.println("widgets 服务开启了");
		clearReceiver = new ClearReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.rolfwang.mobilesafe.CLEAR");

		//注册广播接收者，接收桌面发来的更新Widgets的广播
		this.registerReceiver(clearReceiver, filter);
		
		screenOnOffReceiver=new ScreenOnOffReceiver();
		IntentFilter filter2=new IntentFilter();
		filter2.addAction("android.intent.action.SCREEN_OFF");
		filter2.addAction("android.intent.action.SCREEN_ON");
		registerReceiver(screenOnOffReceiver, filter2);
		
		
		appWidgetManager = AppWidgetManager.getInstance(this);

		componentName = new ComponentName(this,
				MyAppWidgestProvider.class);
		remoteView = new RemoteViews(getPackageName(),
				R.layout.my_appwidget);
		
		updateWidgetsText();//更新Widgets中的文本内容
		
		//设置Widgets中意见清理按钮的单击事件
		setWidgetButtonClick();
	}
	/**
	 * 给Widgets中的一键清理按钮设置单击事件
	 */
	private void setWidgetButtonClick() {
		
		Intent intent = new Intent();
		intent.setAction("com.rolfwang.mobilesafe.CLEAR");
		//创建一个延期意图，进行发送广播
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//设置Widgets中按钮的点击事件,当点击清理按钮时，桌面程序会执行这个延期意图(此延期意图会发送一个广播)
		remoteView.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);
		//更新Widget，设置一键清理按钮的单击事件
		appWidgetManager.updateAppWidget(componentName, remoteView);
	}

	/**
	 * 更新Widgets中Text的内容
	 */
	private void updateWidgetsText() {

		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				remoteView.setTextViewText(R.id.process_count,
						"正在进程数:" + TaskUtils.getRunningTaskCount(getApplicationContext()));

				long ramAviSize = MemoryUtils.getRAMAviSize(getApplicationContext());
				remoteView.setTextViewText(R.id.process_memory,
						"可用内存:" + Formatter.formatFileSize(getApplicationContext(), ramAviSize));
				// 更新Widgets的方法，参数1：ComponentName Widgets组件标识符；参数2：远程View对象
				appWidgetManager.updateAppWidget(componentName, remoteView);
			}
		};
		timer.schedule(timerTask, 1000, 2000);//1秒后开始执行定时任务，每2s执行一次定时任务
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// System.out.println("WidgesService服务停止了");
		if(timer != null){
			timer.cancel();	//取消定时任务
			timer = null;
		}
		
		if(clearReceiver != null){
			unregisterReceiver(clearReceiver);//服务停止时，取消广播
		}
		
		if(screenOnOffReceiver != null){
			unregisterReceiver(screenOnOffReceiver);
		}
		
	}
}
