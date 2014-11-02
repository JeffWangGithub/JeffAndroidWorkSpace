package com.rolfwang.mobilesafe;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.rolfwang.mobilesafe.receiver.Admin;
import com.rolfwang.mobilesafe.ui.SettingView;

public class Setup4Activity extends SetupBaseActivity {

	private SharedPreferences sp;
	private CheckBox cb_protected;
	private SettingView sv_remote;
	private TextView tv_active;
	private DevicePolicyManager dpm;
	private ComponentName component;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setup4);
		
		tv_active = (TextView) findViewById(R.id.tv_active);
		
		cb_protected = (CheckBox) findViewById(R.id.cb_protected);
		
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		component = new ComponentName(this,Admin.class);
		
		//激活状态的回显
		if(!dpm.isAdminActive(component)){
			tv_active.setText("远程锁屏与远程擦除未激活\n激活后可以远程锁屏清除数据");
		}else{
			tv_active.setText("远程锁屏与远程擦除已激活\n可以远程锁屏清除数据");
		}
		
		//状态回显
		boolean protectedState = sp.getBoolean("protected", false);
		cb_protected.setChecked(protectedState);	
		if(protectedState){
			cb_protected.setText("成功开启防护功能");
		}else{
			cb_protected.setText("没有开启防护功能");
		}
		
		
		cb_protected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//当选中时，将数据存储到配置文件中
				Editor edit = sp.edit();
				if(isChecked){
					edit.putBoolean("protected", true);
					cb_protected.setText("成功开启防护功能");
				}else{
					edit.putBoolean("protected", false);
					cb_protected.setText("没有开启防护功能");
				}
				edit.commit();
			}
		});
		
	}

	@Override
	public void next_activity() {
		
		//完成设置向导时，需要将配置文件中的first的值改为false
		Editor edit = sp.edit();
		edit.putBoolean("first", false);
		edit.commit();
		
		Intent intent = new Intent(getApplicationContext(), LostFindActivity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.next_new_enter, R.anim.next_old_out);
	}

	@Override
	public void previous_activity() {
		Intent intent = new Intent(getApplicationContext(), Setup3Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.previous_new_enter, R.anim.prevoius_old_out);
	}
	
	/**
	 * 激活Admin管理员的按钮点击事件
	 */
	public void active(View v){
		
		if(!dpm.isAdminActive(component)){
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
	        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
	        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
	                 "激活后可以进行远程锁屏和远程擦除数据");
	        startActivityForResult(intent, 0);
	        tv_active.setText("远程锁屏与远程擦除已激活\n可以远程锁屏清除数据");
		}else{
			//反激活
			dpm.removeActiveAdmin(component);//反激活Admin权限
			tv_active.setText("远程锁屏与远程擦除未激活\n激活后可以远程锁屏清除数据");
		}
	}
	
}
