package com.rolfwang.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import com.rolfwang.mobilesafe.domain.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppProvider {

	/**
	 * 获取所有应用程序的信息
	 * @param context
	 * @return
	 */
	public static List<AppInfo> getAllAppInfos(Context context) {
		List<AppInfo> list = new ArrayList<AppInfo>();
		PackageManager packageManager = context.getPackageManager();
		// packageManager.getInstalledApplications(0);//此方法是获取清单文件中<application>标签的信息
		// 获取安装的所有应用程序的包信息，可以拿到清单文件中的所有信息
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
		
		for (PackageInfo packageInfo : installedPackages) {
			AppInfo appInfo = new AppInfo();
			
			String appPackageName = packageInfo.packageName;//应用程序的包名
			appInfo.setAppPackageName(appPackageName);
			String appVersion = packageInfo.versionName;//应用程序的版本
			appInfo.setAppVersion(appVersion);
			
			//ApplicationInfo封装了清单文件中application标签下的所有信息
			ApplicationInfo applicationInfo = packageInfo.applicationInfo;
			
			String appName = applicationInfo.loadLabel(packageManager).toString();
			appInfo.setAppName(appName);
			Drawable appIcon = applicationInfo.loadIcon(packageManager);//应用程序图标
			appInfo.setAppIcon(appIcon);
			
			//获取应用程序的标签，次标签使用的是状态机的原理，通过标签可以获取应用的很多信息
			int flags = packageInfo.applicationInfo.flags;
			if((flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE){
				//应用程序安装在外部存储设备
				appInfo.setSD(true);
			}else{
				appInfo.setSD(false);
			}
			if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
				//系统应用
				appInfo.setUser(false);
			}else{
				appInfo.setUser(true);
			}
			list.add(appInfo);
		}
		
		return list;
	}

}
