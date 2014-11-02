package com.rolfwang.mobilesafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.rolfwang.mobilesafe.service.AddressService;
import com.rolfwang.mobilesafe.utils.FileUtils;
import com.rolfwang.mobilesafe.utils.StreamUtils;

public class SplashActivity extends Activity {

	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int ENTER_HOME = 2;
	protected static final int NET_FAIL = 3;
	private String newVersion;
	private String apkUrl;
	private String des;

	private TextView tv_splash_version;
	private String versionName;
	private TextView tv_load;
	private SharedPreferences sp;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SHOW_UPDATE_DIALOG:
				showUpdateDialog();
				break;
			case ENTER_HOME:
				enterHome();
				break;
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "链接网络不成功", 0).show();
				enterHome();
				break;
			default:
				break;
			}
		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_load = (TextView) findViewById(R.id.tv_load);
		
		versionName = getVersion();
		tv_splash_version.setText("版本号:" + versionName);

		// 2，升级检查
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean autoUpdate = sp.getBoolean("auto_update", true);
		if (autoUpdate) {
			checkUpdate();
		} else {
			// 不进行检查更新，则让Splash界面2s后进入主界面
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
						enterHome();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
		
		//将资产目录assets目录下的归属地数据库拷贝到当前应用目录下
		copyDBtoApp("address.db");
		//将assets目录下的病毒数据库，到应用程序中
		copyDBtoApp("antivirus.db");
		
		Intent intent = new Intent(this,AddressService.class);
		startService(intent);	
	}


	/**
	 * 将assets目录下的电话号码归属地数据库拷贝到当前应用下
	 */
	private void copyDBtoApp(final String dbName) {
		//如果数据库已经存在于应用的目录下，则返回，否则进行copy操作
		final File dest = new File(getFilesDir(),dbName);
		if(dest != null && dest.exists()){
			return ;
		}
		
		//因为copy数据是一个比较耗时的操作，所以可以使用异步框架，进行数据库的copy
		new AsyncTask<Void, Void, Void>(){
			InputStream is = null;
			@Override
			protected Void doInBackground(Void... params) {
				FileUtils.copyFile(is, dest);
				return null;
			}
			
			protected void onPreExecute() {
				AssetManager assetManager = getAssets();
				try {
					is = assetManager.open(dbName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.execute();
	}

	/**
	 * 检查是否需要升级
	 */
	private void checkUpdate() {
		final String urlPath = "http://192.168.168.113:8080/updateInfo.html";
		// 1，链接网络
		new Thread() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				long startNetTime = 0;
				long endNetTime = 0;
				try {
					URL url = new URL(urlPath);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000); // 连接 超时时间
					// conn.setReadTimeout(timeoutMillis);// 读取超时时间
					int code = conn.getResponseCode();
					startNetTime = System.currentTimeMillis();
					if (code == 200) {
						InputStream is = conn.getInputStream();
						String jsonStr = StreamUtils.parseInputStream(is);

						// 解析Json
						JSONObject jsonObj = new JSONObject(jsonStr);
						newVersion = jsonObj.getString("version");
						des = jsonObj.getString("des");
						apkUrl = jsonObj.getString("url");

						if (!versionName.equals(newVersion)) {
							// 更新对话框进行更新
							msg.what = SHOW_UPDATE_DIALOG;
							// System.out.println(newVersion+">>>>>>"+des+"........"+apkUrl);
						} else {
							// 不需要进行更新时，进入主界面,
							endNetTime = System.currentTimeMillis();
							msg.what = ENTER_HOME;
						}
					} else {
						// 链接失败,给客户提示，并进入主界面
						endNetTime = System.currentTimeMillis();
						msg.what = NET_FAIL;
					}
				} catch (Exception e) {
					// 链接失败,给客户提示，并进入主界面
					endNetTime = System.currentTimeMillis();
					msg.what = NET_FAIL;
				} finally {
					// 如果不许要联网，或者联网失败，进行睡眠2s在进入主界面************
					if (endNetTime - startNetTime < 2000) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	/**
	 * 进入主界面
	 */
	protected void enterHome() {

		Intent intent = new Intent(getApplicationContext(), HomeActivity.class);

		startActivity(intent);

		// 注意点：进入主界面之后，要见Splash界面finish
		finish();
	}

	/**
	 * 更新对话框
	 */
	protected void showUpdateDialog() {
		// 对话框构造器
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("发现新版本：" + newVersion);
		builder.setMessage(des);
		// 注意，弹出对话框时，应该禁止用户按返回键取消对话框***********
		builder.setCancelable(false);
		// 添加确认按钮
		builder.setPositiveButton("更新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 下载新的版本
				downloadApk();
			}
		});
		// 添加取消按钮
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 点击取消时，进入主界面
				enterHome();
			}
		});

		// 显示对话框
		builder.show();
	}

	/**
	 * 下载最新的apk应用
	 */
	protected void downloadApk() {
		HttpUtils http = new HttpUtils();
		final String savePath = "/sdcard/JeffMobileSafe2.0.apk";
		http.download(apkUrl, savePath, new RequestCallBack<File>() {

			@Override
			public void onSuccess(ResponseInfo<File> arg0) {
				// 下载成功,进行安装
				// Toast.makeText(getApplicationContext(), "下载完成", 0).show();
				installApk(savePath);

			}

			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				super.onLoading(total, current, isUploading);
				String apkTotal = Formatter.formatFileSize(
						getApplicationContext(), total);
				String currentSize = Formatter.formatFileSize(
						getApplicationContext(), current);
				tv_load.setVisibility(View.VISIBLE);// 设置可见
				tv_load.setText(currentSize + "/" + apkTotal);
			}

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// 下载失败，进入下一个页面
				enterHome();
			}
		});
	}

	protected void installApk(String url) {
		File file = new File(url);
		// 1,设置隐式意图
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");

		// 2,摄者数据和类型
		Uri data = Uri.fromFile(file);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(data, type);
		// 3，启动Activity
		startActivity(intent);
	}

	/**
	 * 获取系统的版本信息
	 */
	private String getVersion() {
		String versionName = null;
		// 1，获取包管理对象
		PackageManager pm = getPackageManager();
		try {
			// 2，获取包信息对象
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			// 可以通过packageInfo中的属性获取版本信息的属性
			versionName = packageInfo.versionName;// 版本名称
			// int versionCode = packageInfo.versionCode;//版本号
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}
}
