package com.rolfwang.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.db.dao.AddressDao;

public class AddressService extends Service {

	private OutgoingCallReceiver receiver;
	private MyPhoneStateListener listener;
	private TelephonyManager tm;
	private View view;
	private WindowManager windowManager;
	private SharedPreferences sp;
	private WindowManager.LayoutParams params;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);

		// 1，监听来电电话，并显示号码归属地
		listener = new MyPhoneStateListener();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		// 2,注册广播接收者，监听外拨电话
		receiver = new OutgoingCallReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(receiver, filter);
	}

	private class MyPhoneStateListener extends PhoneStateListener {

		// 电话状态改变时被调用
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			// 响铃状态
			case TelephonyManager.CALL_STATE_RINGING:
				// 查询来电号码的归属地
				AddressDao addressDao = new AddressDao();
				String address = addressDao.queryAddress(incomingNumber,
						getApplicationContext());
				// Toast.makeText(getApplicationContext(), address, 1).show();
				showMyToast(address);
				break;
			// 电话空闲状态
			case TelephonyManager.CALL_STATE_IDLE:
				hideMyToast();
				break;
			// 摘机状态，即接电话后的状态
			case TelephonyManager.CALL_STATE_OFFHOOK:

				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消监听电话的状态
		if (listener != null) {
			tm.listen(listener, PhoneStateListener.LISTEN_NONE);// 停止监听
			listener = null;// 将监听器对象置为null，有利于回收机制进行回收。
		}

		// 取消外拨电话的监听，取消注册广播接收者
		unregisterReceiver(receiver);
	}

	/**
	 * 监听外拨电话的广播接收者
	 */
	class OutgoingCallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取广播数据
			String outingNum = getResultData();
			// 查询外拨号码为归属地信息
			AddressDao addressDao = new AddressDao();
			String address = addressDao.queryAddress(outingNum,
					getApplicationContext());
			// Toast.makeText(getApplicationContext(), address, 1).show();
			showMyToast(address);
		}
	}

	/**
	 * 显示自定义Toast
	 */
	private void showMyToast(String str){
		int[] bgcolor = new int[] { 
				R.drawable.call_locate_white,
				R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };
		
		
		//1，获取WindowManager
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		//2，准备View对象
		view = View.inflate(this, R.layout.mytoast_layout, null);
		view.setBackgroundResource(bgcolor[sp.getInt("style", 0)]);//动态设置归属地对话框的背景
		TextView tv = (TextView) view.findViewById(R.id.tv_show);
		tv.setText(str);
		
		//设置onTouch事件，完成拖拽的功能
		dragView();
		
		params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;  //View对象高度
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;		//View对象宽度
        params.format = PixelFormat.TRANSLUCENT;					//半透明
//        params.type = WindowManager.LayoutParams.TYPE_TOAST;		//类型为toast类型
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;//将类型设置为：优先于电话UI界面，注意此处需要权限system.alert_window
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON	//保持屏幕
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;			//不能获得焦点
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;		//不能触摸操作
        
        
        //根据配置文件中位置进行回显
        params.gravity = Gravity.LEFT+Gravity.TOP;//将控件放置到父控件的left和top位置
        params.x = sp.getInt("positionLeft", 0);
        params.y = sp.getInt("positionRight", 0);        
        
		//4，给windowManager添加View对象，并指定布局参数信息
		windowManager.addView(view, params);
	
	}

	private void dragView() {
		
		//给Mytoast显示的控件添加触摸事件
		view.setOnTouchListener(new OnTouchListener() {
			private int startX = 0;
			private int startY = 0;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					//获取移动的距离
					int dx = newX-startX;
					int dy = newY - startY;
					
					//更新控件的位置
					params.x = params.x +dx;   //注意使用控件本身的位置属性params.x+dx，用startX+dx会产生偏移
				    params.y = params.y + dy;
				    //更新widowManager中控件的params属性**************
					windowManager.updateViewLayout(view, params);
					
					startX = newX;
					startY = newY;
					break;
				}
				return true;
			}
		});
	}

	/**
	 * 隐藏自定义Toast
	 */
	public void hideMyToast() {
		if (windowManager != null && view != null) {
			windowManager.removeView(view);
			windowManager = null;
			view = null;
		}
	}

}
