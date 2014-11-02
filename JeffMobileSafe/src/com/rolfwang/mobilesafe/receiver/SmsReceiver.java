package com.rolfwang.mobilesafe.receiver;

import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.service.GPSService;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		
		// System.out.println("新短信到来");
		// 1,获取接受到的短信内容
		Bundle bundle = intent.getExtras();
		// #1.1通过"pdus"键返回的是一个Object数组，数组中的每个元素代表条短信，每条短信都用byte[]数组表示，
		Object[] pdus = (Object[]) bundle.get("pdus");

		for (Object pdu : pdus) {
			byte[] bytePdu = (byte[]) pdu;
			// #1.2,将字节数组构建成一个短信对象
			SmsMessage sms = SmsMessage.createFromPdu(bytePdu);
			// #1.3,取出短信的号码，和内容
			String address = sms.getOriginatingAddress();// 获取短信的发起地址
			String body = sms.getMessageBody();
			
			boolean protectedState = sp.getBoolean("protected", false);
			//当用户开启手机防盗功能时，监听短信指令
			if(protectedState){
				if("#*location*#".equals(body)){
					//进行定位操作
					System.out.println("location");
					//开启GPS定位服务
					Intent service = new Intent(context,GPSService.class);
					context.startService(service);
					
					//获取位置信息，并向安全号码发送信息
					String latitude = sp.getString("latitude", "");
					String longitude = sp.getString("longitude", "");
					String safeNum = sp.getString("safeNum", "");
					SmsManager sm = SmsManager.getDefault();
					sm.sendTextMessage(safeNum, null, "latitude:"+latitude+"\nlongitude:"+longitude, null, null);
					abortBroadcast();
				}else if("#*alarm*#".equals(body)){
					//播放报警音乐
					System.out.println("alarm");
					
					//设置系统音量:
					//#1,获取音频管理者
					AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
					//#2,获取手机的最大音量
					int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					//#3,将手机的音量设为最大
					am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
					
					//播放音乐
					MediaPlayer mp = MediaPlayer.create(context, R.raw.ylzs);
					mp.start();	
					abortBroadcast();
				}else if("#*lockscreen*#".equals(body)){
					//进行锁屏操作
					System.out.println("lockscreen");
					//获取设备策略对象
					DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
					ComponentName component = new ComponentName(context, Admin.class);
					//判断是否已经获取Admin权限
					if(dpm.isAdminActive(component)){
						dpm.lockNow();//锁屏的命令
					}
					abortBroadcast();
				}else if("#*lockscreen*#".equals(body)){
					//进行远程擦除数据
					System.out.println("lockscreen");
					//获取设备策略对象
					DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
					ComponentName component = new ComponentName(context, Admin.class);
					//判断是否已经获取Admin权限
					if(dpm.isAdminActive(component)){
						dpm.wipeData(0);//擦除数据
					}				
					abortBroadcast();
				}
			}
		}
	}

}
