package com.rolfwang.mobilesafe;

import com.rolfwang.mobilesafe.utils.DensityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public abstract class SetupBaseActivity extends Activity {
	
	private GestureDetector detector;

	/**
	 * 进入下一个activity的方法
	 */
	public abstract void next_activity();
	/**
	 * 进入上一个activity的方法
	 */
	public abstract void previous_activity(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		detector = new GestureDetector(getApplicationContext(), new MyGestureListener());
		
	}
	
	//2,创建手势的监听器
	private class MyGestureListener extends SimpleOnGestureListener{

		//猛动，滑动的手势：当在屏幕上快速的滑动时，调用此方法
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//手势开始点的坐标
			float startX = e1.getRawX();
			float startY = e1.getRawY();
			
			//手势结束点的坐标
			float endX = e2.getRawX();
			float endY = e2.getRawY();
			
			int dX = DensityUtils.dip2px(getApplicationContext(), 100);
			int dY = DensityUtils.dip2px(getApplicationContext(), 30);
			
			//判断手势的方向，进行相应界面的显示
			if(startX-endX>dX && Math.abs(startY-endY)<dY){
				next_activity();
			}else if(endX-startX>dX && Math.abs(startY-endY)<dY){
				previous_activity();
			}
			//注意返回值要指定为true表示处理了此手势
			return true;
		}
	}
	//3,注册手势（此步骤非常容易忘记）
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//调用手势的onTouchEvent方法，此方法处理手势事件，并调用监听器的响应方法
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	//处理返回按键的操作,方法一
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		previous_activity();
	}
	
	
	//对所有按键操作的监听：
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if(keyCode == KeyEvent.KEYCODE_BACK){//判断是否是返回键，注意处理返回按键，，推荐使用方法一
			previous_activity();
			return true;
		}
		return false;
	}
	
	
	
	
	/**
	 * 下一步按钮的点击事件
	 */
	public void next(View v){
		next_activity();
	}
	
	/**
	 * 上一步按钮的点击事件
	 */
	public void pre(View v){
		previous_activity();
	}
	

}
