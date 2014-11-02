package com.rolfwang.mobilesafe.procmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.domain.TaskInfo;
import com.rolfwang.mobilesafe.engine.TaskProvider;
import com.rolfwang.mobilesafe.utils.MemoryUtils;
import com.rolfwang.mobilesafe.utils.TaskUtils;

public class TaskManagerActivity extends Activity {

	@ViewInject(R.id.tv_ramSize)
	private TextView tv_ramSize;
	@ViewInject(R.id.tv_proc_count)
	private TextView tv_proc_count;
	@ViewInject(R.id.lv_proc)
	private ListView lv_proc;
	@ViewInject(R.id.tv_category)
	private TextView tv_category;
	
	private MyProcessAdapter adapter;
	private List<TaskInfo> allTaskInfo;//所有运行进程信息列表
	private List<TaskInfo> userTaskInfo;
	private List<TaskInfo> systemTaskInfo;
	private ActivityManager activityManager;
	
	private boolean isShowSysTask = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_proc_manager);
		ViewUtils.inject(this);

		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//当前总进程数
		int runningTaskCount = TaskUtils.getRunningTaskCount(getApplicationContext());
		tv_proc_count.setText("正在运行的进程:\n"+runningTaskCount+"个");
		// 设置内存可用状况
		getMemoryStatus();
		//填充数据
		fillData();
		
		//设置滚动事件的监听
		setListScrollListener();
		
		//设置条目的点击事件
		setListItemClickListener();
	}

	/**
	 * 设置ListView条目得点击事件
	 */
	private void setListItemClickListener() {
		lv_proc.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(position==0 || position==(userTaskInfo.size()+1)){
					return ;
				}
				//获取单击条目的TaskInfo
				TaskInfo taskInfo = null;
				if(position<=userTaskInfo.size()){
					taskInfo = userTaskInfo.get(position-1);
				}else if(position>=userTaskInfo.size()+1){
					taskInfo = systemTaskInfo.get(position-userTaskInfo.size()-2);
				}
				//点击时更改TaskInfo对象中的状态，并改变实现的状态
				ViewHolder holder = (ViewHolder) view.getTag();
				if(taskInfo.isChecked()){
					taskInfo.setChecked(false); //更改对象中isChecked状态
					holder.cb_check.setChecked(false);//更改界面显示的状态
				}else{
					taskInfo.setChecked(true);
					holder.cb_check.setChecked(true);
				}
			}
		});
	}

	/**
	 * 设置ListView的滚动事件
	 */
	private void setListScrollListener() {
		lv_proc.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//动态更新固定条目的内容
				if(firstVisibleItem >= userTaskInfo.size()){
					tv_category.setText("系统进程:"+systemTaskInfo.size()+"个");
				}else{
					tv_category.setText("用户进程:"+userTaskInfo.size()+"个");
				}
			}
		});
	}

	/**
	 * 给ListView填充数据
	 */
	private void fillData() {
		userTaskInfo = new ArrayList<TaskInfo>();
		systemTaskInfo = new ArrayList<TaskInfo>();
		
		new AsyncTask<Void, Void, Void>() {

			protected void onPostExecute(Void result) {
				if(adapter==null){//第一次加载进行创建adapter，并给ListView设置adapter****
					adapter = new MyProcessAdapter(); 
					lv_proc.setAdapter(adapter);
				}else{ //再次加载数据之后，只需通知adapter进行更新数据******
					adapter.notifyDataSetChanged();
				}
			};
			
			@Override
			protected Void doInBackground(Void... params) {//异步任务进行加载数据
				//获取所有运行进程的进程信息列表
				allTaskInfo = TaskProvider.getRunningTaskInfos(getApplicationContext());
				for (TaskInfo task : allTaskInfo) {
					
					if(task.isUser()){
						userTaskInfo.add(task);
					}else{
						systemTaskInfo.add(task);
					}
				}
				return null;
			}
		}.execute();
	}
	
	class MyProcessAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(isShowSysTask){
				return userTaskInfo.size()+systemTaskInfo.size()+2;
			}else{
				return userTaskInfo.size()+1;
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == 0) { //显示用户进程
				TextView tv = new TextView(getApplicationContext());
				tv.setText("用户进程" + userTaskInfo.size() + "个");
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(18);
				return tv;
			} else if (position == userTaskInfo.size() + 1) { //显示系统进程
				TextView tv = new TextView(getApplicationContext());
				tv.setText("系统进程" + systemTaskInfo.size() + "个");
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(18);
				return tv;
			}
			TaskInfo taskInfo = null;
			if (position <= userTaskInfo.size()) {
				taskInfo = userTaskInfo.get(position - 1);
			} else {
				taskInfo = systemTaskInfo.get(position - userTaskInfo.size() - 2);
			}
			View view;
			ViewHolder holder;
			if (convertView != null && !(convertView instanceof TextView)) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(getApplicationContext(),
						R.layout.item_task_manager, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_mem = (TextView) view.findViewById(R.id.tv_mem);
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				holder.cb_check = (CheckBox) view.findViewById(R.id.cb_check);
				view.setTag(holder);
			}
			holder = (ViewHolder) view.getTag();
			if (TextUtils.isEmpty(taskInfo.getName())) {
				holder.tv_name.setText(taskInfo.getPackageName());
			} else {
				holder.tv_name.setText(taskInfo.getName());
			}
			if (taskInfo.getIcon() == null) {
				holder.iv_icon.setImageResource(R.drawable.default_img);
			} else {
				holder.iv_icon.setImageDrawable(taskInfo.getIcon());
			}
			holder.tv_mem.setText("手机内存:"
					+ Formatter.formatFileSize(getApplicationContext(),
							taskInfo.getMem()));
			//如果当前位置显示的是本应用，那么将cb_check进行隐藏，不让用户清理本应用
			if (taskInfo.getPackageName().equals(getPackageName())) {
				holder.cb_check.setVisibility(View.INVISIBLE);
			} else {
				holder.cb_check.setVisibility(View.VISIBLE);
			}
			
			holder.cb_check.setChecked(taskInfo.isChecked());
			
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
	
	class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_mem;
		CheckBox cb_check;
	}

	/**
	 * 获取内存可用状况，并显示到界面
	 */
	private void getMemoryStatus() {
		long ramTotal = MemoryUtils.getRAMTotalSize2(getApplicationContext());
		String ramTotalSize = Formatter.formatFileSize(getApplicationContext(),
				ramTotal);
		long ramAvi = MemoryUtils.getRAMAviSize(getApplicationContext());
		String ramAviSize = Formatter.formatFileSize(getApplicationContext(),
				ramAvi);
		tv_ramSize.setText("可用/总内存:\n" + ramAviSize + "/" + ramTotalSize);
	}
	
	/**
	 * 清理进程
	 * @param v
	 */
	public void clear(View v){
		List<TaskInfo> deleteList = new ArrayList<TaskInfo>();//临时存储需要清理的任务信息
		
		for(TaskInfo taskInfo:userTaskInfo){//杀死选中的用户进程
			if(taskInfo.isChecked()){
				activityManager.killBackgroundProcesses(taskInfo.getPackageName());
				deleteList.add(taskInfo);  //将需要杀死的进程添加到一个list中
			}
		}
		for(TaskInfo taskInfo:systemTaskInfo){ //杀死选中的系统进程
			if(taskInfo.isChecked()){
				activityManager.killBackgroundProcesses(taskInfo.getPackageName());
				deleteList.add(taskInfo);
			}
		}
		
		long releaseMem = 0;
		//分别从userTaskInfo和systemTaskInfo中删除需要清理的任务
		for(TaskInfo taskInfo : deleteList){
			if(taskInfo.isUser()){
				
				userTaskInfo.remove(taskInfo);
			}else{
				systemTaskInfo.remove(taskInfo);
			}
			releaseMem += taskInfo.getMem();
		}
		//通知adapter进行更新数据
		adapter.notifyDataSetChanged();
		
		String releaseStr = Formatter.formatFileSize(getApplicationContext(), releaseMem);
		
		//重新设置系统可用的内存数据
		getMemoryStatus();
		
		int runningTaskCount = TaskUtils.getRunningTaskCount(getApplicationContext());
		tv_proc_count.setText("正在运行的进程:\n"+runningTaskCount+"个");
		Toast.makeText(getApplicationContext(), "共为您释放了:"+releaseStr+"内存", 0).show();		
	}
	
	
	/**
	 * 全选
	 * @param v
	 */
	public void selectAll(View v){
		for(TaskInfo taskInfo : userTaskInfo){
			//当前应用不允许选中
			if(getPackageName().equals(taskInfo.getPackageName())){
				continue;
			}
			taskInfo.setChecked(true);
		}
		for(TaskInfo taskInfo : systemTaskInfo){
			taskInfo.setChecked(true);
		}
		adapter.notifyDataSetChanged();//通知adapter更新数据
	}
	
	/**
	 * 取消选择
	 * @param v
	 */
	public void cancel(View v){
		for(TaskInfo taskInfo : userTaskInfo){
			taskInfo.setChecked(false);
		}
		for(TaskInfo taskInfo : systemTaskInfo){
			taskInfo.setChecked(false);
		}
		adapter.notifyDataSetChanged();//通知adapter更新数据
	}
	
	/**
	 * 隐藏系统应用
	 * @param v
	 */
	public void setting(View v){
		if(isShowSysTask){
			isShowSysTask = false;
		}else{
			isShowSysTask = true;
		}
		adapter.notifyDataSetChanged();  //更新数据
	}
	
}
