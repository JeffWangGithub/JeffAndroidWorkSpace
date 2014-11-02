package com.rolfwang.mobilesafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class ChangePostionActivity extends Activity {
	@ViewInject(R.id.ll_show)
	private LinearLayout ll_show;
	@ViewInject(R.id.tv_top)
	private TextView tv_top;
	@ViewInject(R.id.tv_bottom)
	private TextView tv_bottom;
	private SharedPreferences sp;

	private int displayWidth;
	private int displayHeight;
	private WindowManager wm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_position);
		ViewUtils.inject(this);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//获取屏幕宽高
		getDisplaySize();
		
		//改变提示框位置
		onchangePosition();
		
		//设置双击事件
		doubleClick();
	}

	/**
	 * 双击剧中的方法
	 */
	private void doubleClick() {
		ll_show.setOnClickListener(new OnClickListener() {
			//1，创建一个数组保存点击的时间
			long[] hits = new long[2];
			@Override
			public void onClick(View v) {
				//2，点击时，将数组中的值向前平移
				System.arraycopy(hits, 1, hits, 0, hits.length-1);
				//3，将本次点击的时间放置到数组的最后
				hits[hits.length-1] = SystemClock.uptimeMillis();
				//4,判断最后一次点击和第一点击的时间间隔
				if(hits[hits.length-1]-hits[0]<300){
					int l=(displayWidth-ll_show.getWidth())/2;
		        	int t=((displayHeight-ll_show.getHeight())-100)/2;
		        	int r=l+ll_show.getWidth();
		        	int b=t+ll_show.getHeight();
		        	ll_show.layout(l, t, r, b);// 重新分配控件的位置
				}
			}
		});
	}

	/**
	 * 获取屏幕大小的方法
	 */
	private void getDisplaySize() {
		
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point outSize = new Point();
		display.getRealSize(outSize);
		displayWidth = outSize.x;
		displayHeight = outSize.y;
		
//		DisplayMetrics outMetrics = new DisplayMetrics(); // 创建了一个白纸
//		wm.getDefaultDisplay().getMetrics(outMetrics);// 在白纸上写上屏幕的款和高
//		displayWidth = outMetrics.widthPixels;
//		displayHeight = outMetrics.heightPixels;
	}

	@Override
	protected void onStart() {
		super.onStart();
		int[] bgcolor = new int[] { R.drawable.call_locate_white,
				R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };
//		// 样式回显
		int which = sp.getInt("style", 0);
		ll_show.setBackgroundResource(bgcolor[which]);

		// 位置回显
		int l = sp.getInt("positionLeft", 0);
		int t = sp.getInt("positionRight", 0);
		ll_show.layout(l, t, l + ll_show.getWidth(), t + ll_show.getHeight());
		//渲染界面要分为三个步骤onMeasure,onLayout(),onDraw第一步没有执行完成，不能执行第二步
		
		//位置回显的正确方式 ,
		//因为其父容器是一个相对布局，我们要设置此控件的参数，就需要用相对布局的参数。
		RelativeLayout.LayoutParams params = (LayoutParams) ll_show.getLayoutParams();
		params.leftMargin = l;
		params.topMargin = t;
		ll_show.setLayoutParams(params);
		
		int b = t+ll_show.getHeight();
		if(b>displayHeight/2){
			tv_top.setVisibility(View.VISIBLE);
			tv_bottom.setVisibility(View.INVISIBLE);
		}else{
			tv_top.setVisibility(View.INVISIBLE);
			tv_bottom.setVisibility(View.VISIBLE);
		}
	}

	private void onchangePosition() {
		
		// 设置一个触摸事件
		ll_show.setOnTouchListener(new OnTouchListener() {
			int startX;
			int startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 1,按下时获取开始位置
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;

				case MotionEvent.ACTION_MOVE:
					// 2，移动时获取一个新的位置
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					// 3，计算移动的距离
					int dx = newX - startX;
					int dy = newY - startY;
					// 4，设置控件的位置
					int l = ll_show.getLeft() + dx;
					int t = ll_show.getTop() + dy;
					int r = l + ll_show.getWidth();
					int b = t + ll_show.getHeight();
					//禁止移出到屏幕外
					if(l<0||t<0||r>displayWidth||b>displayHeight-15){
						break;
					}
					if(b>displayHeight/2){
						tv_top.setVisibility(View.VISIBLE);
						tv_bottom.setVisibility(View.INVISIBLE);
					}else{
						tv_top.setVisibility(View.INVISIBLE);
						tv_bottom.setVisibility(View.VISIBLE);
					}
					
					ll_show.layout(l, t, r, b);

					// 5,更新开始的位置
					startX = newX;
					startY = newY;
					break;

				case MotionEvent.ACTION_UP:
					// 将配置存储到配置文件
					Editor edit = sp.edit();
					edit.putInt("positionLeft", ll_show.getLeft());
					edit.putInt("positionRight", ll_show.getTop());
					edit.commit();
					break;
				}
				return false;  //特别注意：必须返回false，因为此控件即注册了触摸事件又注册了单击事件
			}
		});
	}

}
