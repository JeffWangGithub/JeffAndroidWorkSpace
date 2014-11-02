package com.rolfwang.mobilesafe.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rolfwang.mobilesafe.R;

public class ClearCacheFragment extends Fragment {

	private ImageView iv_scanner;
	private TextView tv_scanner;
	private ProgressBar pb_scanner;
	private ListView lv_content;
	private Button bt_again;
	private Button bt_clear;
	
	private PackageManager packageManager;
	private FragmentActivity activity;
	private MyAdapter adapter;

	private List<CacheInfo> allCacheInfos = new ArrayList<CacheInfo>();

	// 加载Fragment的布局文件
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_cache_clear, null);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		
		iv_scanner = (ImageView) activity.findViewById(R.id.iv_scanner);
		tv_scanner = (TextView) activity.findViewById(R.id.tv_scanner);
		pb_scanner = (ProgressBar) activity.findViewById(R.id.pb_scanner);
		lv_content = (ListView) activity.findViewById(R.id.lv_content);
		bt_again = (Button) activity.findViewById(R.id.bt_again);
		bt_clear = (Button) activity.findViewById(R.id.bt_clear);

		packageManager = activity.getPackageManager();
		
		bt_again.setOnClickListener(new OnClickListener() {//开始扫描按钮的单击事件
			@Override
			public void onClick(View v) {
				bt_again.setClickable(false);
				bt_again.setBackgroundColor(Color.GRAY);
				bt_again.setText("正在扫描");
				// 扫描缓存信息
				scannerCache();
			}
		});
		
		bt_clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(allCacheInfos.size()<1){
					Toast.makeText(activity.getApplicationContext(), "请先进行扫描", 0).show();
					return;
				}else{
					clearAllCache();//清理缓存
				}
			}
		});
		
		
	}

	/**
	 * 扫描应用程序缓存信息
	 */
	private void scannerCache() {
		RotateAnimation animation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animation.setDuration(4000);
		animation.setRepeatCount(Animation.INFINITE);
		iv_scanner.startAnimation(animation);
		tv_scanner.setText("正在初始化....");
		
		allCacheInfos.clear();//扫描之前，先清理上次扫描的缓存

		
		new Thread() {
			public void run() {
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				List<PackageInfo> installedPackages = packageManager
						.getInstalledPackages(0);
				pb_scanner.setMax(installedPackages.size());

				int count = 0;
				for (PackageInfo packageInfo : installedPackages) {
					String packageName = packageInfo.packageName;
					final String lable = packageInfo.applicationInfo.loadLabel(
							packageManager).toString();

					getAppCacheSize(packageName);

					count++;
					pb_scanner.setProgress(count);// 设置进度
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_scanner.setText("正在扫描:" + lable);
						}
					});

					try {
						Thread.sleep(70);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// 扫面完成修改UI
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (allCacheInfos.size() < 1) {
							tv_scanner.setText("您的手机未发现任何缓存!");
						} else {
							tv_scanner.setText("扫描完成共发现：" + allCacheInfos.size()
									+ "个缓存");
							adapter = new MyAdapter();
							lv_content.setAdapter(adapter);
						}
						bt_again.setClickable(true);
						bt_again.setBackgroundResource(R.drawable.btn_green_normal);
						bt_again.setText("开始扫描");
					}
				});

				iv_scanner.clearAnimation();// 扫描完成清楚动画
			};
		}.start();
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return allCacheInfos.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder = null;
			if (convertView != null) {
				view = convertView;
			} else {
				view = View.inflate(activity, R.layout.item_cache, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_cache_size = (TextView) view
						.findViewById(R.id.tv_cache_size);

				view.setTag(holder);
			}
			holder = (ViewHolder) view.getTag();

			CacheInfo cacheInfo = allCacheInfos.get(position);
			String packageName = cacheInfo.getPackageName();
			
			try {
				String name = packageManager.getPackageInfo(packageName, 0).applicationInfo.loadLabel(packageManager).toString();
				holder.tv_name.setText(name);
				Drawable icon = packageManager.getPackageInfo(packageName, 0).applicationInfo
						.loadIcon(packageManager);
				holder.iv_icon.setImageDrawable(icon);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			String cacheSize = Formatter.formatFileSize(activity,
					cacheInfo.getCacheSize());
			holder.tv_cache_size.setText(cacheSize);

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
		private TextView tv_cache_size;
	}

	/**
	 * 封装缓存的数据
	 */
	class CacheInfo {
		private String packageName;
		private long cacheSize;
		private String lable;
		public String getLable() {
			return lable;
		}

		public void setLable(String lable) {
			this.lable = lable;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public long getCacheSize() {
			return cacheSize;
		}

		public void setCacheSize(long cacheSize) {
			this.cacheSize = cacheSize;
		}

	}

	// public void getPackageSizeInfo(String packageName, IPackageStatsObserver
	// observer) {
	// getPackageSizeInfo(packageName, UserHandle.myUserId(), observer);
	// }
	// 获取应用程序的缓存信息：
	/**
	 * 获取应用程序缓存的方法，此方法将缓存信息封装到了IPackageStats的回调方法中
	 * 
	 * @param packageName
	 */
	private void getAppCacheSize(String packageName) {
		try {
			Class<?> loadClass = ClearCacheFragment.class.getClassLoader()
					.loadClass("android.content.pm.PackageManager");

			Method method = loadClass.getDeclaredMethod("getPackageSizeInfo",
					String.class, IPackageStatsObserver.class);
			method.setAccessible(true);
			method.invoke(packageManager, packageName, observer);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//获取应用程序缓存的回调方法
	IPackageStatsObserver.Stub observer = new IPackageStatsObserver.Stub() {
		//应用状态获取完成时调用此方法
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			//再次回调方法内处理缓存信息
			long cacheSize = pStats.cacheSize;  //获取应用程序的缓存大小
			String packageName = pStats.packageName;
			if (cacheSize > 0) {
				CacheInfo info = new CacheInfo();
				info.setCacheSize(cacheSize);
				info.setPackageName(packageName);
				
//				System.out.println(packageName + "::" + cacheSize);
				allCacheInfos.add(info);
			}
		}
	};
	
	//    public abstract void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer);
	
	/**
	 * 清理全部缓存,利用系统的漏洞，申请一个很大的空间，系统会自动删除所有的缓存
	 */
	private void clearAllCache(){
		try {
			Class<?> clazz = Class.forName("android.content.pm.PackageManager");
			Method declaredMethod = clazz.getDeclaredMethod("freeStorageAndNotify", Long.TYPE, IPackageDataObserver.class);
			declaredMethod.invoke(packageManager, Long.MAX_VALUE,dataObserver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//清理全部缓存的回调
	IPackageDataObserver.Stub dataObserver = new IPackageDataObserver.Stub() {
		//清理完成后调用此方法
		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			//因为此回调方法是在异步线程中执行的，所以在此处修改UI需要使用UI线程
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					allCacheInfos.clear();
					adapter.notifyDataSetChanged();
					tv_scanner.setText("缓存已经全部清理！");
				}
			});
		}
	};

}
