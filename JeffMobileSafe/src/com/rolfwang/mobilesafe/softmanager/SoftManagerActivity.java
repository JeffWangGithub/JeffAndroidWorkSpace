package com.rolfwang.mobilesafe.softmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.db.dao.WatchDogDao;
import com.rolfwang.mobilesafe.domain.AppInfo;
import com.rolfwang.mobilesafe.engine.AppProvider;
import com.rolfwang.mobilesafe.utils.AppUtils;
import com.rolfwang.mobilesafe.utils.MemoryUtils;

public class SoftManagerActivity extends Activity implements OnClickListener {
	@ViewInject(R.id.lv_soft)
	private ListView lv_soft;
	@ViewInject(R.id.tv_category)
	private TextView tv_category;
	@ViewInject(R.id.tv_romsize)
	private TextView tv_romsize;
	@ViewInject(R.id.tv_sdsize)
	private TextView tv_sdsize;
	
	
	private PopupWindow popupWindow;
	private AppInfo appInfo;

	private List<AppInfo> allAppInfos;
	private List<AppInfo> userAppInfos;// 用户程序
	private List<AppInfo> systemAppInfos;// 系统程序
	
	private Map<String,Integer> appLockedState;

	private MyListViewAdapter adapter;
	private WatchDogDao watchDogDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_soft_manager);
		ViewUtils.inject(this);
		appLockedState = new HashMap<String, Integer>();
		
		watchDogDao = new WatchDogDao(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//设置存储设备空间大小
		fillMemoryInfo();

		// 获取所有应用程序信息，变更填充到listView
		fillData();

		setListScrollListener();

		// 设置listView的Item点击事件
		setItemClickListener();
		
		//设置listView 条目的长按点击事件
		setItemLongClickeListener();
		
	}


	/**
	 * ListView条目长按点击的事件
	 */
	private void setItemLongClickeListener() {
		
		lv_soft.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0 || position == userAppInfos.size()) {
					// 点击两个标题控件时，直接返回
					return true;
				}

				if (position <= userAppInfos.size()) {
					appInfo = userAppInfos.get(position - 1);
				} else {
					appInfo = systemAppInfos.get(position - userAppInfos.size()
							- 2);
				}
				String packageName = appInfo.getAppPackageName();
				if(getPackageName().equals(packageName)){
					//如果是当前应用则直接返回*****
					return true;
				}
				Integer locked = appLockedState.get(packageName);
				ViewHolder holder = (ViewHolder) view.getTag();
				if(locked !=null && locked == WatchDogDao.APP_LOCKED){
					//解锁
					holder.iv_lock.setImageResource(R.drawable.unlock);
					watchDogDao.updateLockedState(packageName, WatchDogDao.APP_UNLOCKED);//更改数据库	
					appLockedState.put(packageName, WatchDogDao.APP_UNLOCKED);//修改集合
				}else{
					//加锁
					holder.iv_lock.setImageResource(R.drawable.lock);
					if(locked == null){//数据库中没有，添加数据
						watchDogDao.addLockedApp(packageName);
						appLockedState.put(packageName, WatchDogDao.APP_LOCKED);//添加到集合
					}else{	//数据库中含有，更新数据
						watchDogDao.updateLockedState(packageName, WatchDogDao.APP_LOCKED);
						appLockedState.put(packageName, WatchDogDao.APP_LOCKED);//修改集合
					}
				}
				//修改了appLockedState中的数据，通知更改界面
				adapter.notifyDataSetChanged();
				return true;
			}

		});
		
	}

	/**
	 * 设置rom和sd卡控件大小
	 */
	private void fillMemoryInfo() {
		
		String romAvailableSize = MemoryUtils.getROMAvailableSize(getApplicationContext());
		String romTotalSize = MemoryUtils.getROMTotalSize(getApplicationContext());
		tv_romsize.setText("ROM可用空间：\n"+romAvailableSize+"/"+romTotalSize);
		
		String sdAvailableSize = MemoryUtils.getSDAvailableSize(getApplicationContext());
		String sdTotalSize = MemoryUtils.getSDTotalSize(getApplicationContext());
		tv_sdsize.setText("SD卡可用空间：\n"+sdAvailableSize+"/"+sdTotalSize);
		
	}

	/**
	 * ListView的滑动事件
	 */
	private void setListScrollListener() {
		lv_soft.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// 处理tv_category的显示变化
				if (firstVisibleItem >= userAppInfos.size()) {
					tv_category.setText("系统程序：" + systemAppInfos.size() + "个");
				} else {
					tv_category.setText("用户程序：" + userAppInfos.size() + "个");
				}
				if (popupWindow != null) {
					popupWindow.dismiss();
					popupWindow = null;
				}
			}
		});

	}

	/**
	 * ListView条目的单击事件
	 */
	private void setItemClickListener() {

		final View popupView = View.inflate(this, R.layout.popup_view_layout,
				null);
		final LinearLayout ll_uninstall = (LinearLayout) popupView
				.findViewById(R.id.ll_uninstall);
		ll_uninstall.setOnClickListener(SoftManagerActivity.this);// 设置单击事件
		LinearLayout ll_start = (LinearLayout) popupView
				.findViewById(R.id.ll_start);
		ll_start.setOnClickListener(SoftManagerActivity.this);
		LinearLayout ll_share = (LinearLayout) popupView
				.findViewById(R.id.ll_share);
		ll_share.setOnClickListener(SoftManagerActivity.this);
		final LinearLayout ll_info = (LinearLayout) popupView
				.findViewById(R.id.ll_info);
		ll_info.setOnClickListener(SoftManagerActivity.this);

		lv_soft.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (position == 0 || position == userAppInfos.size()) {
					// 点击两个标题控件时，直接返回
					return;
				}

				if (position <= userAppInfos.size()) {
					appInfo = userAppInfos.get(position - 1);
				} else {
					appInfo = systemAppInfos.get(position - userAppInfos.size()
							- 2);
				}
				//如果点击的应用程序为当前应用程序，则不现实卸载和详细信息页面
				if(getPackageName().equals(appInfo.getAppPackageName())){
					ll_uninstall.setVisibility(View.GONE);
					ll_info.setVisibility(View.GONE);
				}else{
					ll_uninstall.setVisibility(View.VISIBLE);
					ll_info.setVisibility(View.VISIBLE);
				}

				// 指定PopuWindow需要挂载的窗体以及宽高，宽高为包裹内容，LayoutParams.WRAP_CONTENT是用-2表示的，所以可以直接指定-2
				popupWindow = new PopupWindow(popupView,
						LayoutParams.WRAP_CONTENT, -2);
				//设置popup的背景，因为动画播放必须要有背景
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				// 指定popupWindow显示父窗体和宽高,
				popupWindow.showAsDropDown(view, view
						.findViewById(R.id.iv_icon).getWidth() + 10, -view
						.findViewById(R.id.iv_icon).getHeight()-10);
				// popupWindow.showAtLocation()//制定显示的绝对位置

				AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//				alphaAnimation.setDuration(1000);
				ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
						Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.5f);
//				scaleAnimation.setDuration(1000);
				AnimationSet animSet = new AnimationSet(false);//参数为是否制定动画插入器
				animSet.addAnimation(alphaAnimation);
				animSet.addAnimation(scaleAnimation);
				animSet.setDuration(800);
				popupView.startAnimation(animSet);

			}
		});

	}

	/**
	 * 获取所有应用程序信息，并填充数据到listView
	 */
	private void fillData() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// 如果是分批加载，必须这样写，如果不是分批加载，不必对adapter进行非空判断*****
				if (adapter == null) {
					adapter = new MyListViewAdapter();
					lv_soft.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				allAppInfos = new ArrayList<AppInfo>();
			}

			@Override
			protected Void doInBackground(Void... params) {
				allAppInfos = AppProvider
						.getAllAppInfos(SoftManagerActivity.this);
				appLockedState = watchDogDao.queryAllLockedApp();//查询锁定状态

				for (AppInfo appInfo : allAppInfos) {
					if (appInfo.isUser()) {
						userAppInfos.add(appInfo);
					} else {
						systemAppInfos.add(appInfo);
					}
				}
				return null;
			}
		}.execute();
	}

	class MyListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return allAppInfos.size() + 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv_category = new TextView(SoftManagerActivity.this);
			tv_category.setBackgroundColor(Color.GRAY);
			tv_category.setPadding(3, 2, 0, 2);
			tv_category.setTextSize(18);
			tv_category.setTextColor(Color.WHITE);
			
			View view = null;
			if(position==0){
				tv_category.setText("用户程序:"+userAppInfos.size()+"个");
				return tv_category;
			}else if(position == userAppInfos.size()+1){
				tv_category.setText("系统程序:"+systemAppInfos.size()+"个");
				return tv_category;
			}
			
			AppInfo appInfo = null;
			ViewHolder holder = null;
			if(position<=userAppInfos.size()){
				appInfo = userAppInfos.get(position-1);
			}else if(position > userAppInfos.size()){
				appInfo = systemAppInfos.get(position-userAppInfos.size()-2);
			}
			
			if(convertView !=null && !(convertView instanceof TextView)){
				view = convertView;
			}else{
				view = View.inflate(getApplicationContext(), R.layout.item_soft_manager, null);
				
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_position = (TextView) view.findViewById(R.id.tv_position);
				holder.tv_version = (TextView) view.findViewById(R.id.tv_version);
				holder.iv_lock = (ImageView) view.findViewById(R.id.iv_lock);
				view.setTag(holder);
			}
			
			holder = (ViewHolder) view.getTag();
			holder.iv_icon.setImageDrawable(appInfo.getAppIcon());
			holder.tv_name.setText(appInfo.getAppName());
			boolean isSD = appInfo.isSD();
			if (isSD) {
				holder.tv_position.setText("SD卡");
			} else {
				holder.tv_position.setText("手机内存");
			}
			holder.tv_version.setText(appInfo.getAppVersion());
			
			//设置锁的状态
			String appPackageName = appInfo.getAppPackageName();
			Integer lockedState = appLockedState.get(appPackageName);
			if(getPackageName().equals(appPackageName)){
				//如果是当前应用，则隐藏锁的图标******
				holder.iv_lock.setVisibility(View.GONE);
			}
			
			if(lockedState!=null && lockedState == WatchDogDao.APP_LOCKED){
				holder.iv_lock.setImageResource(R.drawable.lock);
			}else{
				holder.iv_lock.setImageResource(R.drawable.unlock);
			}
			return view;
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

	class ViewHolder {

		private ImageView iv_icon;
		private TextView tv_name;
		private TextView tv_position;
		private TextView tv_version;
		private ImageView iv_lock;

	}

	/*
	 * PopupWindow的点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_uninstall:
			if(!getPackageName().equals(appInfo.getAppPackageName())&&appInfo.isUser()){
				AppUtils.uninstalApp(this, appInfo.getAppPackageName());
				adapter.notifyDataSetChanged();//通知ListView刷新数据
				fillData();//卸载之后再进行设置存储设备可用大小
			}else if(!appInfo.isUser()){
				Toast.makeText(getApplicationContext(), "系统应用不能卸载", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "本应用不能卸载", 0).show();
			}
			break;
		case R.id.ll_start:
			AppUtils.launchApp(this, appInfo.getAppPackageName());
			break;
		case R.id.ll_share:
			AppUtils.shareApp(this, appInfo.getAppPackageName());
			break;
		case R.id.ll_info:
			AppUtils.appInfo(this, appInfo.getAppPackageName());
			adapter.notifyDataSetChanged();//通知ListView刷新数据,详情界面用户也可能卸载应用
			fillData();//卸载之后再进行设置存储设备可用大小
			break;
		}
	}

}
