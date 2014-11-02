package com.rolfwang.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class TaskUtils {
	
	/**
	 * 获取当前正在运行应用程序的总进程数
	 * @param context
	 * @return
	 */
	public static int getRunningTaskCount(Context context){
		
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
		
		return runningAppProcesses.size();
	}
	
	
	
	
}
