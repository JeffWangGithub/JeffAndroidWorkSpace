package com.rolfwang.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		System.out.println("系统启动完成");
		//获取配置文件中的sim卡串号
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String simSerialNum = sp.getString("sim", "");
		//获取当前sim卡的串号
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String currentSimSerialNum = tm.getSimSerialNumber();
		
		//判断sim卡是否更换
		if(!TextUtils.isEmpty(simSerialNum)){
			//sim更换时，想安全好吗发送短信，并发出报警音乐
			if(!simSerialNum.equals(currentSimSerialNum)){
				//发送报警短信
				SmsManager smsManager = SmsManager.getDefault();
				String safeNum = sp.getString("safeNum", "");
				if(!TextUtils.isEmpty(safeNum)){
					smsManager.sendTextMessage(safeNum, null, "手机sim卡被更换", null, null);
				}
			}
		}
	}

}
