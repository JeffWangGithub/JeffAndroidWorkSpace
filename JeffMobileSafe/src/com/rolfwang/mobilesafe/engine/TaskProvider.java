package com.rolfwang.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import com.rolfwang.mobilesafe.domain.TaskInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

public class TaskProvider {
	
	
	/**
	 * 获取所有运行进程的进程信息列表
	 * @param context
	 * @return
	 */
	public static List<TaskInfo> getRunningTaskInfos(Context context){
		List<TaskInfo> allTask = new ArrayList<TaskInfo>();
		
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager packageManager = context.getPackageManager();
		
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
			TaskInfo taskInfo = new TaskInfo();
			
			String packageName = appProcessInfo.processName;//获取应用程序的进程名，进程名就是应用的包名
			//1，进程包名
			taskInfo.setPackageName(packageName);
			
			int pid = appProcessInfo.pid;//获取进程占用的内存
			MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(new int[]{pid});//API******
			long memory = memoryInfos[0].getTotalPss()*1024;//API *************
			//2，进程内存占用
			taskInfo.setMem(memory);
			
			try {
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);//参数0，表示获取所有的标签
				Drawable icon = applicationInfo.loadIcon(packageManager);//获取应用的图标，API*****
				String label = applicationInfo.loadLabel(packageManager).toString();//获取应用的名称  API***
				
				//3，进程图标
				taskInfo.setIcon(icon);
				//4，进程名称
				taskInfo.setName(label);
				
				int flags = applicationInfo.flags;
				//5，进程是否是用户进程
				if((flags&ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
					taskInfo.setUser(false);//系统进程
				}else{
					taskInfo.setUser(true);//用户进程
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			allTask.add(taskInfo);			
		}
		return allTask;
	}
}
