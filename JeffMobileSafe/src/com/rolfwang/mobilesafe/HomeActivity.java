package com.rolfwang.mobilesafe;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rolfwang.mobilesafe.advanced.AToolsActivity;
import com.rolfwang.mobilesafe.antivirus.AntivirusActivity;
import com.rolfwang.mobilesafe.cache.ClearCacheActivity;
import com.rolfwang.mobilesafe.procmanager.TaskManagerActivity;
import com.rolfwang.mobilesafe.softmanager.SoftManagerActivity;
import com.rolfwang.mobilesafe.telesafe.CallSmsSafeActivity;
import com.rolfwang.mobilesafe.trafficstate.TrafficStateActivity;
import com.rolfwang.mobilesafe.utils.StringUtils;

public class HomeActivity extends Activity {
	
	private GridView gv_home;
	
	private SharedPreferences sp;
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		//第一次开启应用时，提醒用户是否需要创建快捷图标
		boolean isCreate = sp.getBoolean("shortcut", true);
		if(isCreate){
			createShortCut();
			sp.edit().putBoolean("shortcut", false).commit();
		}
		
		gv_home = (GridView) findViewById(R.id.gv_home);
		
		gv_home.setAdapter(new MyGridViewAdapter());
		
		gv_home.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent; 
				switch (position) {
				case 0://表示点击了手机防盗
					if(!TextUtils.isEmpty(sp.getString("password", ""))){
						//从配置文件中获取密码，如果不为空，弹出输入密码框
						showEnterDialog();
					}else{
						showSetupDialog();					
					}
					break;
				case 1://进入通信卫士模块
					intent = new Intent(getApplicationContext(),CallSmsSafeActivity.class);
					startActivity(intent);
					break;
				case 2:
					intent = new Intent(getApplicationContext(),SoftManagerActivity.class);
					startActivity(intent);
					break;
				case 3:
					intent = new Intent(getApplicationContext(),TaskManagerActivity.class);
					startActivity(intent);
					break;
				case 4:
					intent = new Intent(getApplicationContext(),TrafficStateActivity.class);
					startActivity(intent);
					break;
				case 5:
					intent = new Intent(getApplication(),AntivirusActivity.class);
					startActivity(intent);
					break;
				case 6:
					intent = new Intent(getApplicationContext(),ClearCacheActivity.class);
					startActivity(intent);
					break;
				case 7:
					intent = new Intent(getApplicationContext(),AToolsActivity.class);
					startActivity(intent);
					break;
				case 8://表示设置中心按钮
					intent = new Intent(getApplicationContext(),SettingActivity.class);
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 创建快捷图标
	 */
	private void createShortCut() {
/*	<receiver   系统桌面应用创建快捷图标的广播接收者
         android:name="com.android.launcher2.InstallShortcutReceiver"
         android:permission="com.android.launcher.permission.INSTALL_SHORTCUT">
         <intent-filter>
             <action android:name="com.android.launcher.action.INSTALL_SHORTCUT" />
         </intent-filter>
     </receiver>*/
		
		
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提醒");
		builder.setMessage("是否创建桌面图标?");
		builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//向桌面发送广播的意图
				Intent intent = new Intent();  
				intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				
				//指定快捷图标的动作、lable、icon等信息
				Intent value = new Intent();
				//此处只能使用隐式意图（因为桌面不能使用我们自己以应用的显示意图）
				value.setAction("com.rolfwang.mobilesafe.SHUTCUT_START_APP");
				//**制定快捷图标启动的acivity属于哪一个应用，如果不设置，则会产生当应用被卸载时，快捷图标不能同时卸载的bug
				value.setClass(getApplicationContext(), SplashActivity.class);
				//图标的动作
				intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, value);
				
				//快捷图标的名字
				intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士");
				
				//图标的icon
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
				
				sendBroadcast(intent);	//想桌面发送广播	
			}
		});
		
		builder.setNegativeButton("取消", null);
		builder.show();		
	}


	/**
	 * 输入密码弹出框，密码正确则进入下一个页面
	 */
	protected void showEnterDialog() {
		//1，设置自定义对话框
		AlertDialog.Builder builder = new Builder(this);
		
		View view = View.inflate(this, R.layout.enter_dialog, null);//使用布局文件填充View对象
		//2，调用builder的setView方法，自定义dialog对话框
		builder.setView(view);
		final AlertDialog dialog = builder.show();
		
		final EditText et_password = (EditText) view.findViewById(R.id.et_password);
		Button bt_confirm = (Button) view.findViewById(R.id.bt_confirm);
		Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
		
		//确定按钮的点击事件，
		bt_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//校验密码是否正确
				String password = et_password.getText().toString().trim();
				password = StringUtils.md5Digest(password);
				String truePassword = sp.getString("password", "");
				if(!TextUtils.isEmpty(password)){
					if(password.equals(truePassword)){
						//密码正确，进入手机防盗页面
						Intent intent = new Intent(getApplicationContext(),LostFindActivity.class);
						startActivity(intent);
						dialog.dismiss();//取消对话框的显示
					}else{
						Toast.makeText(getApplicationContext(), "密码不正确，请重新输入", 0).show();
						return;
					}
				}else{
					Toast.makeText(getApplicationContext(), "密码不能为空", 0).show();
					return;
				}
			}
		});
		// 取消按钮的点击事件
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();// 取消对华框的现实
			}
		});
		
		//长安密码框后的图标显示密码明文
		ImageView iv_show_password = (ImageView) view.findViewById(R.id.iv_show_password);
		
		iv_show_password.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//此处值得获取是一个小技巧：如果在代码中没有找到布局文件中对应属性的值 ,
				//可以去找系统的属性文件中去查找此属性，查看其对应的值。
				//系统属性文件的路径：sdk\platforms\android-16\data\res\values\attrs.xml  
				count++;
				if(count%2==1){
					//显示密码明文
					et_password.setInputType(0x00000001);
				}else{
					//显示密文
					et_password.setInputType(0x00000081);
				}
				
				
			}
		});
		
		
	}


	/**
	 * 显示设置密码对话框
	 */
	protected void showSetupDialog() {
		//1，设置自定义对话框
		AlertDialog.Builder builder = new Builder(this);
		
		View view = View.inflate(this, R.layout.setup_dialog, null);//使用布局文件填充View对象
		//2，调用builder的setView方法，自定义dialog对话框
		builder.setView(view);
		//此属性，设置了对话框的禁止按返回按钮取消
		builder.setCancelable(false);
		final AlertDialog dialog = builder.show();
		
		final EditText et_password = (EditText) view.findViewById(R.id.et_password);
		final EditText et_password_confirm = (EditText) view.findViewById(R.id.et_password_confirm);
		Button bt_confirm = (Button) view.findViewById(R.id.bt_confirm);
		Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
		
		//确定按钮的点击事件，
		bt_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = et_password.getText().toString().trim();
				String passwordConfirm = et_password_confirm.getText().toString().trim();
				//点击确定按钮，进行保存数据
				if(TextUtils.isEmpty(password)){
					Toast.makeText(HomeActivity.this, "密码不能为空", 0).show();
					return;
				}else if(TextUtils.isEmpty(passwordConfirm)){
					Toast.makeText(HomeActivity.this, "请再次输入密码", 0).show();
					return;
				}else if(!password.equals(passwordConfirm)){
					Toast.makeText(HomeActivity.this, "密码不一致，请重新输入", 0).show();
					return;
				}else if(password.equals(passwordConfirm)){
					//两次密码相同，将密码进行加密处理
					String digestPassword = StringUtils.md5Digest(password);
					//将数据保存到配置文件
					Editor editor = sp.edit();
					editor.putString("password", digestPassword);
					editor.commit();
					dialog.dismiss();//取消对话框
					Toast.makeText(HomeActivity.this, "密码设置成功", 0).show();
				}
			}
		});
		//取消按钮的点击事件
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();//取消对华框的现实
			}
		});
	}


	private class MyGridViewAdapter extends BaseAdapter{
		int[] imageId = { R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app,
				R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
				R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings };
		
		String[] names = { "手机防盗", "通讯卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理",
				"高级工具", "设置中心" };
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = View.inflate(getApplicationContext(), R.layout.gridview_item_home, null);
			}
			//给GridView中的图片和标题设置内容
			ImageView iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
			TextView tv_des = (TextView) convertView.findViewById(R.id.tv_des);
			
			iv_icon.setImageResource(imageId[position]);
			tv_des.setText(names[position]);
			
			return convertView;
		}
		@Override
		public int getCount() {
			return 9;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
	
	

}
