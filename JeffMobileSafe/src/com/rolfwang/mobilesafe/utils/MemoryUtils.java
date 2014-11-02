package com.rolfwang.mobilesafe.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

public class MemoryUtils {
	
	
	/**
	 * 获取当前可用的RAM大小
	 * @param context
	 * @return 返回结果未进行文件大小的格式化
	 */
	public static long getRAMAviSize(Context context){
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();//获取手机内存信息，并将信息封装到MemoryInfo对象中
		activityManager.getMemoryInfo(outInfo);//从MemoryInfo对象中获取总内存
		return outInfo.availMem;
	}
	
	/**
	 * 获取设备的RAM总大小,不兼容API14一下版本，
	 * 在低版本的设备上不能兼容，简易使用getRAMTotalSize2(Context context)方法
	 * @deprecated
	 * @param context
	 * @return
	 */
	public static long getRAMTotal(Context context){
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);		
		MemoryInfo outInfo = new MemoryInfo();
		//获取手机内存信息，并将信息封装到MemoryInfo对象中
		activityManager.getMemoryInfo(outInfo);
		long totalMem = outInfo.totalMem;//从MemoryInfo对象中获取总内存
		return totalMem;  //注意outInfo.availMem此字段是API14以上才有的字段，低版本可能会有问题
	}
	
	/**
	 * 获取设备的RAM总大小，兼容低版本，简易使用此方法
	 * @param context
	 * @return 返回long型数据，数据未进行格式化
	 */
	public static long getRAMTotalSize2(Context context){
		try {
			FileReader read  = new FileReader("/proc/meminfo");
			BufferedReader br = new BufferedReader(read);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line =br.readLine())!=null){
				if(line.contains("MemTotal")){
					char[] charArray = line.toCharArray();
					for (char c : charArray) {
						if(c>='0'&&c<='9'){
							sb.append(c);
						}
					}
					break;
				}
			}
			return Long.parseLong(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 获取SD卡的总大小
	 * @param context
	 * @return 返回格式化后的大小，含有单位
	 */
	public static String getSDTotalSize(Context context){
		File storageDirectory = Environment.getExternalStorageDirectory();
		long totalSpace = storageDirectory.getTotalSpace();
		String size = Formatter.formatFileSize(context, totalSpace);
		return size;
	}
	
	
	/**
	 * 获取SD卡的可用空间大小
	 * @param context
	 * @return 返回已经格式化后的可用空间大小，含单位
	 */
	public static String getSDAvailableSize(Context context){
		File storageDirectory = Environment.getExternalStorageDirectory();
		long freeSpace = storageDirectory.getFreeSpace();
		String size = Formatter.formatFileSize(context, freeSpace);
		return size;
	}
	
	
	
	/**
	 * 获取手机内部的存储空间总大小，即ROM的总大小
	 * @param context
	 * @return 返回格式化后的大小，含有单位
	 */
	public static String getROMTotalSize(Context context){
		
		File dataDirectory = Environment.getDataDirectory();
		long totalSpace = dataDirectory.getTotalSpace();
		String size = Formatter.formatFileSize(context, totalSpace);
		return size;
	}
	
	
	/**
	 * 获取手机内部存储空间的可用大小，即ROM的可用空间
	 * @param context
	 * @return 返回已经格式化后的可用空间大小，含单位
	 */
	public static String getROMAvailableSize(Context context){
		File dataDirectory = Environment.getDataDirectory();
		long freeSpace = dataDirectory.getFreeSpace();
		String size = Formatter.formatFileSize(context, freeSpace);
		return size;
	}
	

}
