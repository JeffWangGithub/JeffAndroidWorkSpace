package com.rolfwang.mobilesafe.antivirus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.db.dao.AntivirusDao;
import com.rolfwang.mobilesafe.utils.AppUtils;
import com.rolfwang.mobilesafe.utils.StringUtils;

public class AntivirusActivity extends Activity{
	
	protected static final int SCANING_APP = 10;
	protected static final int Virus = 1;
	@ViewInject(R.id.tv_scanner)
	private TextView tv_scanner;
	@ViewInject(R.id.pb_scanner)
	private ProgressBar pb_scanner;
	@ViewInject(R.id.iv_sanner)
	private ImageView iv_sanner;
	@ViewInject(R.id.ll_content)
	private LinearLayout ll_content;
	@ViewInject(R.id.tv_count)
	private TextView tv_count;
	
	private PackageManager packageManager;
	private List<PackageInfo> installedPackages;
	private List<String> virusPackageNames = new ArrayList<String>();
	private List<String> virusLables = new ArrayList<String>();
	private AntivirusDao antivirusDao;
	private int virusCount;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			
			if(msg.what == SCANING_APP){
				String lable = (String) msg.obj;
				tv_scanner.setText("正在扫描:"+lable);
				TextView tv = new TextView(AntivirusActivity.this);
				tv.setText(lable);
				tv.setTextColor(Color.BLACK);
				if(msg.arg1==Virus){//病毒按照红色字体进行显示
					tv.setTextColor(Color.RED);
				}
				ll_content.addView(tv, 0);//添加到布局文件中显示
				String str = virusCount==0?"":",发现:"+virusCount+"个病毒";
				tv_count.setText("已经扫描："+pb_scanner.getProgress()+"个程序"+str);
			}
		};
	};
	private RotateAnimation animation;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_antivirus);
		
		ViewUtils.inject(this);
		
		antivirusDao = new AntivirusDao(this);
		
		animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(4000);
		animation.setRepeatCount(Animation.INFINITE);//无线次循环
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		scannerVirus();
		
	}
	
	/**
	 * 扫描病毒的方法
	 */
	private void scannerVirus() {
		iv_sanner.startAnimation(animation);
		tv_scanner.setText("正在初始化...");
		virusPackageNames.clear();//扫描前，清楚病毒列表
		new Thread(){
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				packageManager = getPackageManager();
				installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
				pb_scanner.setMax(installedPackages.size());
				int count = 0;
				
				for (PackageInfo info : installedPackages) {
					String packageName = info.packageName;
					String lable = info.applicationInfo.loadLabel(packageManager).toString();

					Message msg = Message.obtain();//从消息池中获取消息
					msg.what = SCANING_APP;
					msg.obj = lable;
					
					Signature[] signatures = info.signatures;
					String signature = signatures[0].toCharsString();//获取签名文件的字符串
					String signatureMD5 = StringUtils.md5Digest(signature);
					//查询病毒数据库
					if(antivirusDao.isVirus(signatureMD5)){
						//是病毒
						virusPackageNames.add(packageName);//添加到病毒的列表中
						virusLables.add(lable);
						virusCount++;
						msg.arg1 = Virus; //是病毒
					}
					
					handler.sendMessage(msg);
					count++;
					pb_scanner.setProgress(count);//设置进度条的进度
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				iv_sanner.clearAnimation();//清楚动画效果
				
				//显示扫描的内容
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(virusPackageNames.size()<1){
							tv_scanner.setText("扫描完成，您的手机非常安全！");
						}else{
							tv_scanner.setText("发现"+virusPackageNames.size()+"个病毒,建议清理");
							tv_scanner.setTextColor(Color.RED);
						}
					}});
			};
		}.start();
	}
	
	
	/**
	 * 重新扫描
	 * @param v
	 */
	public void againScan(View v){
		if(pb_scanner.getProgress()<pb_scanner.getMax()){
			Toast.makeText(this, "正在进行扫描...请稍后", 0).show();
			return;
		}
		scannerVirus();//重新扫描
	}
	
	/**
	 * 清理
	 * @param v
	 */
	public void clear(View v){
		if(pb_scanner.getProgress()<pb_scanner.getMax()){
			Toast.makeText(this, "正在进行扫描...请稍后", 0).show();
			return;
		}
		
		//TODO 待完成查看详情按钮
		AlertDialog.Builder builder = new Builder(AntivirusActivity.this);
		builder.setTitle("详情");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setNegativeButton("取消", null);
		if(virusPackageNames.size()<1){
			builder.setMessage("您的手机很安全,请放心使用！");
			builder.show();
			return;
		}
		
		if(pb_scanner.getProgress()==pb_scanner.getMax()&&virusPackageNames.size()>0){
			for(String packageName : virusPackageNames){
				AppUtils.uninstalApp(this, packageName);				
			}
			
		}
	}
}
