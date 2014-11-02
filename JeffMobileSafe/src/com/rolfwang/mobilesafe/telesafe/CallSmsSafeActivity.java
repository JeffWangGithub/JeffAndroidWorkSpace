package com.rolfwang.mobilesafe.telesafe;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.db.dao.BlackNumDao;
import com.rolfwang.mobilesafe.domain.BlackNumInfo;

public class CallSmsSafeActivity extends Activity{
	@ViewInject(R.id.lv_blacknum)
	private ListView lv_blacknum;
	private List<BlackNumInfo> blackNumInfos;
	private BlackNumDao dao;
	private MyListAdapter adapter;
	@ViewInject(R.id.bt_addBlack)
	private Button bt_addBlack;
	private int pageSize = 20;
	private int startIndex = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_sms_safe);
		
		ViewUtils.inject(this);
		
		//查询并设置数据
		fillData();
		//添加黑名单的点击事件
		addBlackNum();
		
		//listview设置滑动事件
		setScrollListener();
		
		
	}
	/**
	 * 设置listview的滑动事件
	 */
	private void setScrollListener() {
		lv_blacknum.setOnScrollListener(new OnScrollListener() {
			// 当listVIew 滑动状态发生变化调用
			//ListView滑动事件的三种状态
			//静止(闲置)：OnScrollListener.SCROLL_STATE_IDLE 
			//触摸滑动 ：OnScrollListener.SCROLL_STATE_TOUCH_SCROLL 使用手指在进行滑动
			//惯性滑动 飞速滑动： OnScrollListener.SCROLL_STATE_FLING 惯性滑动，手指已经离开屏幕
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//处理滑动时分页加载的逻辑
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					//最后显示的是blackNumInfos中最后一个数据是加载新的数据**********
					int lastVisiblePosition = lv_blacknum.getLastVisiblePosition();
					if(lastVisiblePosition == blackNumInfos.size()-1){
						startIndex = startIndex + pageSize;
						fillData();			//加载新的数据
					}
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});
	}
	/**
	 * 添加黑名单的点击事件
	 */
	private void addBlackNum() {
		bt_addBlack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setTitle("添加黑名单");
				
				View view = View.inflate(CallSmsSafeActivity.this, R.layout.dialog_add_black, null);	
				//初始化控件
				final EditText et_phoneNum = (EditText) view.findViewById(R.id.et_phoneNum);
				final RadioGroup rg_black_type = (RadioGroup) view.findViewById(R.id.rg_black_type);
				
				Button bt_ok = (Button) view.findViewById(R.id.bt_ok);
				Button bt_cancle = (Button) view.findViewById(R.id.bt_cancel);
				
				final AlertDialog dialog = builder.create();
				
				bt_ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String blackNum = et_phoneNum.getText().toString().trim();
						int mode = rg_black_type.getCheckedRadioButtonId();
						
						if(TextUtils.isEmpty(blackNum)){
							Toast.makeText(getApplicationContext(), "电话号码不能为空", 0).show();
							return;
						}
						
						dao.insertBlackNum(blackNum, mode);
						blackNumInfos.add(0,new BlackNumInfo(blackNum, mode));//添加数据是指定了，添加到list的第一个位置
						adapter.notifyDataSetChanged();
						dialog.dismiss();	
						//通知adapter更新数据
					}
				});
				bt_cancle.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();	
					}
				});
				dialog.setView(view);
				dialog.show();	//show方法内部会先执行create的操作。
				//注意这两个地方，要用dialog进行setView和show，而不能使用builder，如果用builder.show(),其实是创建了两个弹出框
			}
		});
	}
	/**
	 * 使用异步框架从数据库中查询数据,并设置到listView中
	 */
	private void fillData() {
		new AsyncTask<Object, Void, Object>(){
			protected void onPostExecute(Object result) {
				//当adapter为空时进行创建，非空时只进行数据的刷新，避免了每次加载数据listView都回滚到首条************
				if(adapter ==null){
					adapter = new MyListAdapter();
					lv_blacknum.setAdapter(adapter);
				}else {
					adapter.notifyDataSetChanged();//通知刷新数据，此方法刷新的数据后listView不会回滚到首条******
				}
				
				
			};
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dao = new BlackNumDao(CallSmsSafeActivity.this);
			}
			@Override
			protected Void doInBackground(Object... params) {
				//进行分页加载
				if(blackNumInfos==null){
					blackNumInfos = dao.queryPart(pageSize, startIndex);
				}else{
					blackNumInfos.addAll(dao.queryPart(pageSize, startIndex));
				}
				return null;
			}
		}.execute();
	}
	
	/**
	 *对ListView进行优化的Adapter
	 */
	class MyListAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			if(blackNumInfos!=null){
				return blackNumInfos.size();
			}
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//ListView优化第一步：1，复用convertView对象
			//			  第二步：2，使用自定义holder类，优化findViewById
			final BlackNumInfo blackNumInfo = blackNumInfos.get(position);
			Holder holder = null;
			if(convertView == null){
				
				convertView = View.inflate(getApplicationContext(), R.layout.item_blacknum_listview, null);
				holder = new Holder();
				holder.iv_remove = (ImageView) convertView.findViewById(R.id.iv_remove);
				holder.tv_blacknum = (TextView) convertView.findViewById(R.id.tv_blacknum);
				holder.tv_mode = (TextView) convertView.findViewById(R.id.tv_mode);
				//向convertView对象传递数据
				convertView.setTag(holder);
			}
			//convertView不为空的时候，获取holder中holder对象，给它重新设置数据
			holder = (Holder) convertView.getTag();
			holder.tv_blacknum.setText(blackNumInfo.getBlackNum());
			int mode = blackNumInfo.getMode();
			if(mode == BlackNumDao.MODE_SMS){
				holder.tv_mode.setText("短信拦截");
			}else if(mode == BlackNumDao.MODE_CALL){
				holder.tv_mode.setText("电话拦截");
			}else if(mode == BlackNumDao.MODE_ALL){
				holder.tv_mode.setText("全部拦截");
			}
			
			//删除黑名单
			holder.iv_remove.setOnClickListener(new OnClickListener() {
				String blackNum = blackNumInfo.getBlackNum();
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
					//dialog弹出框
					builder.setIcon(R.drawable.ic_launcher);
					builder.setTitle("警告：");
					builder.setMessage("是否阐述黑名单"+blackNum+"?");
					
					builder.setNegativeButton("取消", null);
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dao.deleteBlackNum(blackNum);
							//*1删除集合中的元素****
							blackNumInfos.remove(blackNumInfo);
							//*2让adapter更新显示的数据，***********
							adapter.notifyDataSetChanged();
							dialog.dismiss();//隐藏弹出窗口
						}
					});
					builder.show();
				}
			});
			
			return convertView;
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
	/**
	 * 自定义Holder类，封装listView表单向中的view对象
	 *
	 */
	class Holder {
		private TextView tv_blacknum;
		private TextView tv_mode;
		private ImageView iv_remove;	
	}
	

}
