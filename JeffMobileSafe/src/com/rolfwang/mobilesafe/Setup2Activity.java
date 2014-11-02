package com.rolfwang.mobilesafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.rolfwang.mobilesafe.ui.SettingView;

public class Setup2Activity extends SetupBaseActivity {

	private SettingView sv_bind;
	private SharedPreferences sp;
	private TelephonyManager tm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setup2);
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		//获取电话管理者，需要权限
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		sv_bind = (SettingView) findViewById(R.id.sv_bind);
		
		//数据回显的操作
		String simSerialNumber = sp.getString("sim", "");
		if("".equals(simSerialNumber)){
			sv_bind.setChecked(false);
		}else{
			sv_bind.setChecked(true);
		}
		
		sv_bind.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isChecked = sv_bind.isChecked();
				Editor edit = sp.edit();
				if(isChecked){
					sv_bind.setChecked(!isChecked);
					//解绑操作
					edit.putString("sim", "");
				}else {
					sv_bind.setChecked(true);
					//绑定的操作
					String simSerialNumber = tm.getSimSerialNumber();
					edit.putString("sim", simSerialNumber);
				}
				edit.commit();//提交
			}
		});
	}

	@Override
	public void next_activity() {
		//检查是否绑定sim，
		if(!sv_bind.isChecked()){
			Toast.makeText(getApplicationContext(), "请绑定SIM卡", 0).show();
			return;
		}
		
		Intent intent = new Intent(getApplicationContext(), Setup3Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.next_new_enter, R.anim.next_old_out);
	}

	@Override
	public void previous_activity() {
		Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.previous_new_enter, R.anim.prevoius_old_out);
	}
	
}
