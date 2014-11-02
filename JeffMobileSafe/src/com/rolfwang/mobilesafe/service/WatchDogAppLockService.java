package com.rolfwang.mobilesafe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;

import com.rolfwang.mobilesafe.db.dao.WatchDogDao;
import com.rolfwang.mobilesafe.softmanager.WatchDogUnlockActivity;

public class WatchDogAppLockService extends Service {

	private ActivityManager activityManager;
	private List<RunningTaskInfo> runningTasks;
	private WatchDogDao watchDogDao;
	private Map<String, Integer> appLockedState;
	private List<String> tempUnlock = new ArrayList<String>();
	private WatchDogReiceiver watchDogReiceiver;
	private String tempUnLockPackageName;  //广播传递过来的临时解锁的包名
	private boolean flag = true;//监听线程开始的标志


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		watchDogReiceiver = new WatchDogReiceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.rolfwang.mobilesafe.UNLOCK");//接收临时解锁的广播
		filter.addAction(Intent.ACTION_SCREEN_OFF);//接收锁定屏幕的广播
		filter.addAction(Intent.ACTION_SCREEN_ON);//接收开启屏幕的广播
		
		registerReceiver(watchDogReiceiver, filter);
		
		watchDogDao = new WatchDogDao(this);
		appLockedState = watchDogDao.queryAllLockedApp();
		//注册内容观察者：当数据库改变时，进程重新查询数据库
		getContentResolver().registerContentObserver(WatchDogDao.URI_LOCK_DB_CHANGED, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				//数据库发生变化时，进行重新查询数据库
				appLockedState = watchDogDao.queryAllLockedApp();
			}
		});
		
		dogRun();//执行看门狗，监听加锁应用
	}

	private void dogRun() {
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final Intent intent = new Intent(WatchDogAppLockService.this,WatchDogUnlockActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		new Thread(){
			public void run() {
				while(flag){
					runningTasks = activityManager.getRunningTasks(1);//获取当前运行的任务栈列表，参数：表示获取任务栈的个数
					RunningTaskInfo currentTaskInfo = runningTasks.get(0);//当前正在运行的任务栈
					ComponentName topActivity = currentTaskInfo.topActivity;//拿到当前打开的activity
					String currentPackageName = topActivity.getPackageName();//当前运行程序的包名
					System.out.println(currentPackageName);
					Integer isLocked = appLockedState.get(currentPackageName);
					if(isLocked !=null && isLocked == WatchDogDao.APP_LOCKED){//此应用在数据库中记录为加锁
						if(!tempUnlock.contains(currentPackageName)){
							//临时解锁的队列中不包含此应用
							intent.putExtra("currentPackageName", currentPackageName);
							startActivity(intent);//密码验证
						}
					}
					
					//判断当前运行的程序是否已经改变，如果改变，则再次加锁*************
					List<RunningTaskInfo> runningTasks2 = activityManager.getRunningTasks(1);
					RunningTaskInfo currentTaskInfo2 = runningTasks2.get(0);//当前正在运行的任务栈
					ComponentName topActivity2 = currentTaskInfo2.topActivity;//拿到当前打开的activity
					String currentPackageName2 = topActivity2.getPackageName();//当前运行程序的包名
					
//					if(!getPackageName().equals(currentPackageName2)&&currentPackageName != currentPackageName2){
//						tempUnlock.remove(tempUnLockPackageName);//当任务栈改变时移出传过来的临时解锁包名
//					}
					System.out.println(currentPackageName2+"再次获取");
					System.out.println(tempUnLockPackageName+"传递的包名");
					//当前运行的程序不是广播传递过来的程序，并且不是解锁的程序时，再次加锁传过来的程序
					if(!currentPackageName2.equals(tempUnLockPackageName)&&!getPackageName().equals(currentPackageName2)){
						tempUnlock.remove(tempUnLockPackageName);
					}
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	class WatchDogReiceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if("com.rolfwang.mobilesafe.UNLOCK".equals(action)){
				//临时解锁的广播
				tempUnLockPackageName = intent.getStringExtra("packageName");
				tempUnlock.add(tempUnLockPackageName);//加入临时解锁的集合中
			}
			
			if(Intent.ACTION_SCREEN_OFF.equals(action)){
				System.out.println("屏幕锁定");
				//锁屏的广播
				tempUnlock.remove(tempUnLockPackageName);//清楚传递过来的数据
				flag = false;							//停止循环
			}
			if(Intent.ACTION_SCREEN_ON.equals(action)){
				System.out.println("屏幕开启");
				flag = true;					//开始循环
				dogRun();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		flag = false;//停止监听线程的循环
		//反注册广播接收者
		if(watchDogReiceiver != null){
			unregisterReceiver(watchDogReiceiver);
		}
	}

}
