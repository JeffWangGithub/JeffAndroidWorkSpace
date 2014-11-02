package com.rolfwang.mobilesafe;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rolfwang.mobilesafe.domain.ContactInfo;
import com.rolfwang.mobilesafe.engine.ContactsEngine;

public class ContactActivity extends Activity {
	
	private ListView lv_contact;
	private List<ContactInfo> allContacts;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			lv_contact.setAdapter(new MyListViewAdapter());
			pb.setVisibility(View.INVISIBLE);
		};
	};


	private ProgressBar pb;	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		
		lv_contact = (ListView) findViewById(R.id.lv_contact);
		
		pb = (ProgressBar) findViewById(R.id.pb);
		
		//获取所有的联系人这是一个很耗时的操作，因此我们可以在子线程中完成此操作
		/*new Thread(){
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				allContacts = ContactsEngine.getAllContacts(getApplicationContext());
				Message msg = Message.obtain();
				//发送消息通知更新UI
				handler.sendEmptyMessage(0);
			};
		}.start();*/
		
		/*//使用自定义的异步任务工具类
		new MyAsyncTask() {
			@Override
			public void preTask() {
				pb.setVisibility(View.VISIBLE);//显示进度条
			}
			
			@Override
			public void postTask() {
				lv_contact.setAdapter(new MyListViewAdapter());
				pb.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void doInback() {
				//模拟耗时操作，让线程休眠3秒
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//查询联系人在子线程中执行
				allContacts = ContactsEngine.getAllContacts(getApplicationContext());
			}
		}.execute();*/
		
		//使用系统自带的异步任务工具类
		//三个泛型参数第一个参数：执行的参数；第二个执行的结果；第三个执行的结果
		new AsyncTask<Void, Void, Void>() {

			//在子线程中执行的代码
			@Override
			protected Void doInBackground(Void... params) {
				//模拟耗时操作，让线程休眠3秒
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//查询联系人在子线程中执行
				allContacts = ContactsEngine.getAllContacts(getApplicationContext());
				return null;
			}
			
			//在子线程之前执行的代码
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pb.setVisibility(View.VISIBLE);//显示进度条
			}
			
			//在子线程之后执行的代码
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				lv_contact.setAdapter(new MyListViewAdapter());
				pb.setVisibility(View.INVISIBLE);
			}
		}.execute();
		
		lv_contact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactInfo contactInfo = allContacts.get(position);
				
				Intent data = new Intent();
				data.putExtra("num", contactInfo.getNum());
				setResult(RESULT_OK, data);
				finish();
				
			}
		});
	}
	
	
	private class MyListViewAdapter extends BaseAdapter{
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null){
				convertView = View.inflate(getApplicationContext(), R.layout.item_contacts, null);
			}
			TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			tv_name.setText(allContacts.get(position).getName());
			TextView tv_num = (TextView) convertView.findViewById(R.id.tv_num);
			tv_num.setText(allContacts.get(position).getNum());
			return convertView;
		}
		@Override
		public int getCount() {
			return allContacts.size();
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

}
