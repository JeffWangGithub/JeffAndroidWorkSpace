package com.rolfwang.mobilesafe.utils;

import android.os.Handler;
/**
 * 模板代码
 * @author yu
 *
 */
public abstract class MyAsykTask {
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			postTask();
		}
	};
	/**
	 * 在子线程之前执行的代码
	 */
	public abstract void preTask();
	/**
	 * 在子线程之后执行的代码 
	 */
	public abstract void postTask();
	/**
	 * 在子线程中执行的代码
	 */
	public abstract void doInback();
	// 执行
	public void execute(){
		preTask();
		new Thread(){
			public void run() {
				 doInback();
				 handler.sendEmptyMessage(0);
			};
		}.start();
	}
}