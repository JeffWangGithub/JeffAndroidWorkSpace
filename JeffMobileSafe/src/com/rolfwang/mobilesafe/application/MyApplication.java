package com.rolfwang.mobilesafe.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.os.Environment;

/**
 * 处理整个应用中未捕获的异常
 * @author Jeff
 *
 */
public class MyApplication extends Application {
	
	//此方法在应用程序启动时就被调用，其他所有对象调用之前就会被调用
	@Override
	public void onCreate() {
		super.onCreate();
		//1，拿到当前线程
		Thread currentThread = Thread.currentThread();
		//2，设置未捕获异常处理器
		currentThread.setUncaughtExceptionHandler(new MyExceptionHandler());
		
	}
	
	class MyExceptionHandler implements UncaughtExceptionHandler{
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			
			File externalStorageFile = Environment.getExternalStorageDirectory();
			File file = new File(externalStorageFile,"mobileSafeLog.txt"); 
			try {
				FileOutputStream fos = new FileOutputStream(file,true);
				PrintStream ps = new PrintStream(fos,true);
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = sdf.format(new Date());
				ps.println(time);
				ex.printStackTrace(ps);		//3，将错误信息写入日志
				ps.println("---------------------");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//4，杀死当前应用，
			android.os.Process.killProcess(android.os.Process.myPid());//此方法只能用于自杀
		}
	}
}
