package com.rolfwang.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LostFindActivity extends Activity {
	
	
	private TextView tv_safeNum;
	private SharedPreferences sp;
	private ImageView iv_protected;
	private boolean protectedFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_lostfind);
		iv_protected = (ImageView) findViewById(R.id.iv_protected);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		if(sp.getBoolean("first", true)){
			//第一次进入手机防盗界面时，跳转到设置向导界面
			Intent intent = new Intent(this,Setup1Activity.class);
			startActivity(intent);
		}	
		
		tv_safeNum = (TextView) findViewById(R.id.tv_safeNum);
		String safeNum = sp.getString("safeNum", "");
		tv_safeNum.setText(safeNum);	
		
		boolean protectedState = sp.getBoolean("protected", false);
		if(protectedState){
			iv_protected.setImageResource(R.drawable.lock);
			protectedFlag = true;
		}else{
			iv_protected.setImageResource(R.drawable.unlock);
			protectedFlag = false;
		}
	}
	
	/**
	 * 是否开启防盗功能的点击事件
	 */
	public void protectedFun(View v){
		Editor edit = sp.edit();
		if(protectedFlag){
			//如果当前为开启，则将其变为关闭
			iv_protected.setImageResource(R.drawable.unlock);
			edit.putBoolean("protected", false);
			protectedFlag = false;
		}else{
			iv_protected.setImageResource(R.drawable.lock);
			edit.putBoolean("protected", true);
			protectedFlag = true;
		}
		edit.commit();
	}

	/**
	 * 重新进入设置向导按钮的点击事件
	 */
	public void resetGuid(View v){
		Intent intent = new Intent(this,Setup1Activity.class);
		
		startActivity(intent);
		finish();
	}
	
}
