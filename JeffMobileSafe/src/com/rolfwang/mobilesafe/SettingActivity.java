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
import android.view.View;
import android.view.View.OnClickListener;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.service.AddressService;
import com.rolfwang.mobilesafe.service.BlackNumService;
import com.rolfwang.mobilesafe.service.WatchDogAppLockService;
import com.rolfwang.mobilesafe.ui.SettingEnterView;
import com.rolfwang.mobilesafe.ui.SettingView;
import com.rolfwang.mobilesafe.utils.ServiceUtils;

public class SettingActivity extends Activity {
	// @ViewInject(id)xUtils的注解技术，可以用来初始化控件
	@ViewInject(R.id.sv_update)
	private SettingView sv_update;
	@ViewInject(R.id.sv_address)
	private SettingView sv_address;
	@ViewInject(R.id.sev_style)
	private SettingEnterView sev_style;
	@ViewInject(R.id.sev_postion)
	private SettingEnterView sev_postion;
	@ViewInject(R.id.sv_black_service)
	private SettingView sv_black_service;
	@ViewInject(R.id.sev_shortcut)
	private SettingEnterView sev_shortcut;
	@ViewInject(R.id.sv_sorf_lock)
	private SettingView sv_sorf_lock;
	
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		// 使用xUtils的注解结束，初始化所有添加注解的控件********
		ViewUtils.inject(this);

		sp = getSharedPreferences("config", MODE_PRIVATE);

		// #1，进行回显，从配置文件中读取出来，进行状态显示
		boolean autoUpdate = sp.getBoolean("auto_update", true);
		sv_update.setChecked(autoUpdate);

		// #2，设置监听事件，修改状态
		sv_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checkedState = sv_update.isChecked();
				sv_update.setChecked(!checkedState);

				Editor editor = sp.edit();
				editor.putBoolean("auto_update", !checkedState);
				editor.commit();// 注意，需要进行提交
			}
		});

		// #2，Address服务点击后改变状态
		sv_address.setOnClickListener(new OnClickListener() {
			Intent intent = new Intent(getApplicationContext(),
					AddressService.class);

			@Override
			public void onClick(View v) {
				if (sv_address.isChecked()) {
					sv_address.setChecked(false);
					// 修改显示状态
					sv_address.setDes("隐藏号码归属地");
					// 停止服务
					stopService(intent);
				} else {
					sv_address.setChecked(true);
					// 修改状态
					sv_address.setDes("显示号码归属地");
					// 开启服务
					startService(intent);
				}
			}
		});

		// 设置号码风格的点击事件
		changeLocationStyle();

		enterChangePostion();

		// 拦截黑名单点按钮击事件
		setBlackNumClick();

		// 创建快捷图标
		shortcut();
		
		// 软件锁服务
		setSoftLockClick();

	}
	

	// 一般在onStart方法中处理回显的操作
	@Override
	protected void onStart() {
		super.onStart();
		// Address服务状态回显
		boolean isRunning = ServiceUtils.isServiceRunning(
				getApplicationContext(),
				"com.rolfwang.mobilesafe.service.AddressService");

		if (isRunning) {
			sv_address.setChecked(true);
		} else {
			sv_address.setChecked(false);
		}

		// 黑名单服务的状态回显
		boolean isBlackServiceRuning = ServiceUtils.isServiceRunning(
				getApplicationContext(),
				"com.rolfwang.mobilesafe.service.BlackNumService");
		if (isBlackServiceRuning) {
			sv_black_service.setChecked(true);
		} else {
			sv_black_service.setChecked(false);
		}
		
		//软件锁服务的回显
		
		boolean isWatchDogRunning = ServiceUtils.isServiceRunning(getApplicationContext(), "com.rolfwang.mobilesafe.service.WatchDogAppLockService");
		if(isWatchDogRunning){
			sv_sorf_lock.setChecked(true);
		}else{
			sv_sorf_lock.setChecked(false);
		}

	}

	

	/**
	 * 软件锁服务的点击事件
	 */
	private void setSoftLockClick() {
		
		sv_sorf_lock.setOnClickListener(new OnClickListener() {
			Intent service = new Intent(getApplicationContext(),WatchDogAppLockService.class);
			@Override
			public void onClick(View v) {
				if(sv_sorf_lock.isChecked()){
					sv_sorf_lock.setChecked(false);
					//停止服务
					stopService(service);
				}else{
					sv_sorf_lock.setChecked(true);
					//开启服务
					startService(service);
				}
			}
		});
		
	}

	/**
	 * 设置是否床架快捷图标
	 */
	private void shortcut() {
		sev_shortcut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setTitle("提醒");
				builder.setMessage("是否创建桌面图标?");
				builder.setPositiveButton("创建",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 向桌面发送广播的意图
								Intent intent = new Intent(); // 此处只能使用隐式意图（因为桌面不能使用我们自己以应用的显示意图）
								intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

								// 指定快捷图标的动作、lable、icon等信息
								Intent value = new Intent();
								value.setAction("com.rolfwang.mobilesafe.SHUTCUT_START_APP");
								// 图标的动作
								intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
										value);

								// 快捷图标的名字
								intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
										"手机卫士");

								// 图标的icon
								intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
										BitmapFactory.decodeResource(
												getResources(),
												R.drawable.ic_launcher));

								sendBroadcast(intent); // 想桌面发送广播
							}
						});

				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});
	}

	/**
	 * 设置黑名单
	 */
	private void setBlackNumClick() {
		final Intent intent = new Intent(getApplicationContext(),
				BlackNumService.class);
		sv_black_service.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sv_black_service.isChecked()) {
					sv_black_service.setChecked(false);
					// 关闭服务
					stopService(intent);
				} else {
					sv_black_service.setChecked(true);
					startService(intent);
				}
			}
		});
	}

	/**
	 * 进入更改提示框位置的界面
	 */
	private void enterChangePostion() {

		sev_postion.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this,
						ChangePostionActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 改变归属地提示框的风格
	 */
	private void changeLocationStyle() {
		final String[] items = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };

		int which = sp.getInt("style", 0);
		sev_style.setDes(items[which]);

		sev_style.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 注意传递context的时候，弹出对话框必须用activity，
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setTitle("归属地风格");
				builder.setNegativeButton("取消", null);
				builder.setSingleChoiceItems(items, sp.getInt("style", 0),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								sp.edit().putInt("style", which).commit();
								sev_style.setDes(items[which]);
								dialog.dismiss();
							}
						});
				// 显示对话框
				builder.show();
			}
		});
	}

}
