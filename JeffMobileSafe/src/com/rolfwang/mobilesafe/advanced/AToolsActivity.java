package com.rolfwang.mobilesafe.advanced;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.engine.SmsEngine;
import com.rolfwang.mobilesafe.engine.SmsEngine.ProcessListener;

public class AToolsActivity extends Activity{
	@ViewInject(R.id.bt_query_address)
	private Button bt_query_address;
	private static  final int SMS_BACKUP_OVER = 1;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SMS_BACKUP_OVER:
				Toast.makeText(getApplicationContext(), "备份完成", 0).show();
				break;

			default:
				break;
			}
			
		};
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
		
		ViewUtils.inject(this);
	}
	
	public void queryAddress(View v){
		Intent intent = new Intent(this, QueryAddressActivity.class);
		startActivity(intent);		
	}
	
	/**
	 * 备份短信
	 * @param v
	 */
	public void backupSms(View v){
		
		final ProgressDialog progressDialog = new ProgressDialog(AToolsActivity.this);
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度弹出框的样式：水平条样式
		progressDialog.setTitle("短信备份");
		progressDialog.setMessage("正在备份短信....");
		progressDialog.show();
		
		final File sdPath = Environment.getExternalStorageDirectory();
		
		new Thread(){
			public void run() {
				SmsEngine.backupSms(getApplicationContext(), sdPath+"/backupSms.xml", new ProcessListener() {
					int max = 0;
					@Override
					public void process(int process) {
						try {
							sleep(60);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						progressDialog.setProgress(process);
						if(max == process){
							progressDialog.dismiss();
							Message msg = Message.obtain();
							msg.what = SMS_BACKUP_OVER;
							handler.sendMessage(msg);
						}
					}
					@Override
					public void max(int max) {
						this.max = max;
//						System.out.println("总进度："+max);
						progressDialog.setMax(max);
					}
				});
			};
		}.start();
	}
	
	
	public void restore(View v){
		final ProgressDialog progressDialog = new ProgressDialog(AToolsActivity.this);
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度弹出框的样式：水平条样式
		progressDialog.setTitle("短信恢复");
		progressDialog.setMessage("正在恢复短信....");
		progressDialog.show();
		
		final File sdPath = Environment.getExternalStorageDirectory();
		
		new Thread(){
			public void run() {
				SmsEngine.restoreSms(getApplicationContext(), sdPath+"/backupSms.xml", new ProcessListener() {
					int max = 0;
					@Override
					public void process(int process) {
						try {
							sleep(60);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						progressDialog.setProgress(process);
						if(max-1 == process){
							progressDialog.dismiss();
							Message msg = Message.obtain();
							msg.what = SMS_BACKUP_OVER;
							handler.sendMessage(msg);
							//恢复之后，取出重复的短信
							SmsEngine.removeSameSms(getApplicationContext());
						}
					}
					@Override
					public void max(int max) {
						this.max = max;
//						System.out.println("总进度："+max);
						progressDialog.setMax(max);
					}
				});
			};
		}.start();
		
	}
	
	
	

}
