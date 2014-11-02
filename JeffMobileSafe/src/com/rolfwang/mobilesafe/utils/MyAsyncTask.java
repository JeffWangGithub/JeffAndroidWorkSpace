package com.rolfwang.mobilesafe.utils;

import android.os.Handler;
import android.os.Message;

/**
 * 异步任务的工具类
 *
 */
public abstract class MyAsyncTask {
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//子线程执行完之后，调用子线程之后的方法
			postTask();
		};
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
	
	/**
	 * 开始执行的方法
	 */
	public void execute(){
		//先执行preTask
		preTask();
		
		new Thread(){
			public void run() {
				//调用子线程执行的方法
				doInback();
				Message msg = Message.obtain();
				//发送消息执行，子线程完成之后的方法
				handler.sendEmptyMessage(0);
			};
		}.start();
	}
}
