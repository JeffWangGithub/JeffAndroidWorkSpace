package com.rolfwang.mobilesafe.cache;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;

public class ClearCacheActivity extends FragmentActivity {
	
	@ViewInject(R.id.ll_content)
	private LinearLayout llcontent;
	@ViewInject(R.id.tv_clear_cache)
	private TextView tv_clear_cache;
	@ViewInject(R.id.tv_clear_sd)
	private TextView tv_clear_sd;
	
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private boolean flag = true;	//缓存清理是否选中的标识
	
	private Fragment clearCacheFragment;
	private Fragment clearSDFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clear_cache);
		
		ViewUtils.inject(this);
		
		clearCacheFragment = new ClearCacheFragment();
		clearSDFragment = new ClearSDFragment();
		
		fragmentManager = getSupportFragmentManager();
		
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.ll_content, clearCacheFragment);
		fragmentTransaction.commit();
		
		tv_clear_cache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(flag){
					flag = false;
					tv_clear_cache.setBackgroundResource(R.drawable.btn_green_pressed);
					tv_clear_sd.setBackgroundResource(R.drawable.btn_green_normal);
					//切换fragment
					fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.ll_content, clearCacheFragment);
					fragmentTransaction.commit();					
				}
			}
		});
		
		tv_clear_sd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(!flag){
					flag = true;
					tv_clear_cache.setBackgroundResource(R.drawable.btn_green_normal);
					tv_clear_sd.setBackgroundResource(R.drawable.btn_green_pressed);
					//切换fragment
					fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.ll_content, clearSDFragment);
					fragmentTransaction.commit();			
				}
			}
		});	
		
	}
}
