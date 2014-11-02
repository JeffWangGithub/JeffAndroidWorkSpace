package com.rolfwang.mobilesafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setup3Activity extends SetupBaseActivity {

	private SharedPreferences sp;
	private EditText et_safeNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setup3);
		
		et_safeNum = (EditText) findViewById(R.id.et_safeNum);
		
		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		String safeNum = sp.getString("safeNum", "");
		if(!"".equals(safeNum)){
			et_safeNum.setText(safeNum);
		}
		
	}
	
	@Override
	public void next_activity() {
		//将安全号码保存到配置文件中
		String safeNum = et_safeNum.getText().toString().trim();
		if(TextUtils.isEmpty(safeNum)){
			Toast.makeText(getApplicationContext(), "安全密码不能为空", 0).show();
			return;
		}
		
		Editor edit = sp.edit();
		edit.putString("safeNum", safeNum);
		edit.commit();		
		
		Intent intent = new Intent(getApplicationContext(), Setup4Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.next_new_enter, R.anim.next_old_out);
	}

	@Override
	public void previous_activity() {
		Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.previous_new_enter, R.anim.prevoius_old_out);
	}
	
	/**
	 *选择联系人按钮的单击事件
	 */
	public void selectContant(View v){
//		//调用系统联系人应用，获取手机号码
//		Intent intent = new Intent();
//		intent.setAction("android.intent.action.PICK");
//    	intent.addCategory("android.intent.category.DEFAULT");
//    	intent.setType("vnd.android.cursor.dir/phone_v2");
//		
//		startActivityForResult(intent, 0);
		
		//使用内容提供者获取联系人
		Intent intent = new Intent(this,ContactActivity.class);
		
		startActivityForResult(intent, 0);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		
//		if(data!=null){
//    		Uri uri = data.getData();
//    		
//    		ContentResolver resolver = getContentResolver();
//    		Cursor cursor = resolver.query(uri, null, null, null, null);
//    		while(cursor.moveToNext()){
//    			String name = cursor.getString(cursor.getColumnIndex("data1"));
//    			name = name.replaceAll("-", "");
//    			et_safeNum.setText(name);
//    		}
//    		cursor.close();
//    	}
		
		if(data != null){
			String num = data.getStringExtra("num");
			et_safeNum.setText(num);
			
		}
		
		
	}
	
}
