package com.rolfwang.mobilesafe.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.sax.StartElementListener;
import android.widget.Toast;

public class AppUtils {

	/**
	 * 根据包名，卸载应用
	 */
	public static void uninstalApp(Context context, String packageName) {
		// <intent-filter>
		// <action android:name="android.intent.action.VIEW" />
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		// </intent-filter>

		Intent intent = new Intent();
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + packageName));
		context.startActivity(intent);
	}

	/**
	 * 根据应用成的包名启动应用
	 */
	public static void launchApp(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();
		// 获取应用中具有启动功能的intent
		Intent launchIntentForPackage = packageManager
				.getLaunchIntentForPackage(packageName);
		if (launchIntentForPackage != null) {
			context.startActivity(launchIntentForPackage);
		} else {
			Toast.makeText(context, "此应用不能启动", 0).show();
		}
	}

	/**
	 * 分享应用
	 * 
	 * @param context
	 * @param appName
	 */
	public static void shareApp(Context context, String appName) {
		// <intent-filter>
		// <action android:name="android.intent.action.SEND" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:mimeType="text/plain" />

		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.setType("text/plain");
		// Intent.EXTRA_TEXR 内部为"android.intent.extra.TEXT"字符串，这是google给提供的固定代码
		// 想intent设置文本数据或get文本数据，都使用这个字符串名字
		intent.putExtra(Intent.EXTRA_TEXT, "推荐您使用一款软件" + appName
				+ ",下载地址google市场");
		context.startActivity(intent);
	}

	/**
	 * 进入系统应用程序相信信息页面，查看应用程序相信信息
	 * @param context
	 * @param packageName
	 */
	public static void appInfo(Context context,String packageName) {
		// 详细信息
		Intent intent = new Intent();
		intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
		intent.setData(Uri.parse("package:" + packageName));
		context.startActivity(intent);
	}
	
	/**
	 * 获取应用程序的签名
	 * @param context
	 * @param packageName 应用程序的包名
	 * @return
	 */
	public static Signature getSignature(Context context, String packageName){
		Signature signature  = null;
		PackageManager packageManager = context.getPackageManager();
		 try {
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			Signature[] signatures = packageInfo.signatures;
			signature = signatures[0];
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		 return signature;
	}

}
