package com.rolfwang.mobilesafe.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.rolfwang.mobilesafe.db.dao.BlackNumDao;

public class BlackNumService extends Service {

	private SmsReceiver receiver;
	private TelephonyManager tm;
	private MyPhoneStateListener phoneStateListener;
	private BlackNumDao dao;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		dao = new BlackNumDao(getApplicationContext());
		
		//监听短信的到来
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(Integer.MAX_VALUE);//设置优先级，其实真正的最优先级不是1000，而是Integer.MAX_VALUE
		receiver = new SmsReceiver();
		registerReceiver(receiver, filter);//注册广播接收者
		
		//监听电话到来
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		phoneStateListener = new MyPhoneStateListener();
		tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
	}
	
	class MyPhoneStateListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, final String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING://当电话处于响铃状态的时候
//				System.out.println("电话到来----------");
				int mode = dao.queryMode(incomingNumber);
				if(mode==BlackNumDao.MODE_CALL||mode ==BlackNumDao.MODE_ALL){
//					System.out.println("挂断电话----------");
					//挂断电话
					endCall();
					//删除通话记录
					deleteCallLog(incomingNumber);
				}
				break;
			}
		}

		/**
		 * 删除通话记录（固定代码）
		 */
		private void deleteCallLog(final String incomingNumber) {
			//删除通话记录
			//1，获取内容解析者，
			final ContentResolver resolver = getContentResolver();
			final Uri uri = Uri.parse("content://call_log/calls");
			//2，注册内容观察者，当发现数据库数据变化时，进行删除通话记录
			//第二个参数true表示，uri不必进行完全匹配，只要主机名正确即可，一般都是用true
			resolver.registerContentObserver(uri, true, new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					super.onChange(selfChange);
					//3，当数据库数据发生改变时，删除通话记录的数据
					resolver.delete(uri, "number=?", new String[]{incomingNumber});
					//4,取消注册内容观察者
					resolver.unregisterContentObserver(this);//删除完毕数据后，将观察者关闭
				}
			});
		}

		/**
		 * 挂断电话的代码（固定代码）
		 */
		private void endCall() {
			try {
				//暴力反射TelephonyManager，调用它的私有getITelephony方法获取ITelephony对象，在进行调用器endCall
				Class<?> loadClass = BlackNumService.class.getClassLoader().loadClass("android.telephony.TelephonyManager");
				
				Method getITelephonyMethod = loadClass.getDeclaredMethod("getITelephony", null);
				getITelephonyMethod.setAccessible(true);//
				ITelephony telephony = (ITelephony) getITelephonyMethod.invoke(tm);
				
				telephony.endCall();//调用ITelephony的endCall方法，挂断电话
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
	}
	

	/**
	 * 接收短信的监听者
	 *
	 */
	class SmsReceiver extends BroadcastReceiver{
		

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			// #1.1通过"pdus"键返回的是一个Object数组，数组中的每个元素代表条短信，每条短信都用byte[]数组表示，
			Object[] pdus = (Object[]) bundle.get("pdus");

			for (Object pdu : pdus) {
				byte[] bytePdu = (byte[]) pdu;
				// #1.2,将字节数组构建成一个短信对象
				SmsMessage sms = SmsMessage.createFromPdu(bytePdu);
				// #1.3,取出短信的号码，和内容
				String address = sms.getOriginatingAddress();// 获取短信的发起地址
//				String body = sms.getMessageBody();
				System.out.println("短信到来："+"address"+address);
				
				int mode = dao.queryMode(address);
				if(mode == BlackNumDao.MODE_SMS||mode == BlackNumDao.MODE_ALL){
					abortBroadcast(); //进行来接
				}
			}
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//广播接收者的反注册
		if(receiver !=null){
			unregisterReceiver(receiver);
		}
		
		//服务停止时，停止监听来电
		if(tm!=null&&phoneStateListener != null){
			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		
		
	}

}
