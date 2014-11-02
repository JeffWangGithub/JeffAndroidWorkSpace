package com.rolfwang.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.text.TextUtils;

public class ServiceUtils {
	
	public static boolean isServiceRunning(Context context,String serviceClassName){
		
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		//获取当前正在运行的所有的服务，参数表示获取的最大个数，当运行个数小于最大个数时，按照实际个数返回
		List<RunningServiceInfo> runningServices = activityManager.getRunningServices(1000);
		//非空判断
		if(TextUtils.isEmpty(serviceClassName)){
			throw new RuntimeException("serviceName不能为空");
		}
		for (RunningServiceInfo serviceInfo : runningServices) {
			String runningServiceName = serviceInfo.service.getClassName();
			if(serviceClassName.equals(runningServiceName)){
				return true;
			}
		}
		return false;
	}

}
