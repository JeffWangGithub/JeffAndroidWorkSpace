package com.rolfwang.mobilesafe.softmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;

public class WatchDogUnlockActivity extends Activity {
	
	@ViewInject(R.id.iv_icon)
	private ImageView iv_icon;
	@ViewInject(R.id.tv_name)
	private TextView tv_name;
	@ViewInject(R.id.et_password)
	private EditText et_password;
	private PackageManager packageManager;
	private ApplicationInfo applicationInfo;
	private String packageName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watchdog);
		ViewUtils.inject(this);//初始化控件
		
		packageManager = getPackageManager();
		packageName = getIntent().getStringExtra("currentPackageName");
		try {
			//显示待代开的应用的icon和名字
			applicationInfo = packageManager.getApplicationInfo(packageName, 0);
			iv_icon.setImageDrawable(applicationInfo.loadIcon(packageManager));
			tv_name.setText(applicationInfo.loadLabel(packageManager));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
				
	}
	//解决临时解锁后，再次打开不需要验证的bug
	
	
	
	
	//解决：返回按钮不能退出的bug。返回时，直接进入系统桌面
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);//"android.intent.action.MAIN"
		intent.addCategory(Intent.CATEGORY_HOME);//"android.intent.category.HOME"
		startActivity(intent);	
		
		super.onBackPressed();//父类的方法，默认finish当前activity
	}
	
	//解决：看门狗图标不正确的bug。连续打开两个加锁应用时，第二个应用图标显示异常
	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
	
	/**
	 * 解锁
	 * @param v
	 */
	public void unlock(View v){
		String password = et_password.getText().toString().trim();
		if("123".equals(password)){
			//解锁
			//向服务发送广播，将此应用保存到一个临时解锁的集合总
			Intent intent = new Intent();
			intent.setAction("com.rolfwang.mobilesafe.UNLOCK");
			intent.putExtra("packageName", packageName);
			sendBroadcast(intent);			
			finish();
		}else{
			Toast.makeText(this, "密码不正确", 0).show();
		}
	}
	
	/**返回桌面
	 * @param v
	 */
	public void cancel(View v){
		
		onBackPressed();		
	}
	
	

}
