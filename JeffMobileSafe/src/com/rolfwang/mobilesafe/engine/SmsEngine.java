package com.rolfwang.mobilesafe.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

public class SmsEngine {
	
	//定义一个借口，便于其他类进行回调
	public interface ProcessListener{
		/**
		 * 总的进度
		 * @param max： 总进度
		 */
		public void max(int max);
		
		/**
		 * 正在执行的进度
		 * @param process：当前进度
		 */
		public void process(int process);
	}
	
	/**
	 * 备份短信
	 * @param context
	 * @param savePath  备份文件的路径如：/mnt/sdcard/back.xml
	 * @param listener  备份进度的监听器
	 */
	public static void backupSms(Context context,String savePath, ProcessListener listener){
		//方法里引用接口
		XmlSerializer serializer = Xml.newSerializer();//创建序列化器
		try {
			serializer.setOutput(new FileOutputStream(savePath), "UTF-8");//初始化序列化器
			
			ContentResolver resovler = context.getContentResolver();
			Uri uri = Uri.parse("content://sms");
			Cursor cursor = resovler.query(uri, new String[]{"address","date","type","body"}, null, null, null);
			listener.max(cursor.getCount());
			
			serializer.startDocument("UTF-8", true);//设置xml的头信息
			serializer.startTag(null, "Smss");
			
			int count = 0;
			while(cursor.moveToNext()){
				serializer.startTag(null, "sms");
				
				serializer.startTag(null, "address");
				String address = cursor.getString(cursor.getColumnIndex("address"));
				if(!TextUtils.isEmpty(address)){//地址有可能为空
					serializer.text(address);
				}
				serializer.endTag(null, "address");
				
				serializer.startTag(null, "date");
				String date = cursor.getString(cursor.getColumnIndex("date"));
				serializer.text(date);
				serializer.endTag(null, "date");
				
				serializer.startTag(null, "type");
				String type = cursor.getString(cursor.getColumnIndex("type"));
				serializer.text(type);
				serializer.endTag(null, "type");
				
				serializer.startTag(null, "body");
				String body = cursor.getString(cursor.getColumnIndex("body"));
				if(!TextUtils.isEmpty(body)){
					serializer.text(body);
				}
				serializer.endTag(null, "body");
				
				serializer.endTag(null, "sms");
				
				count++;
				listener.process(count);
			}
			serializer.endTag(null, "Smss");
			serializer.endDocument();
			serializer.flush();//刷新序列化器
			cursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 恢复短信，向短信数据库中插入制定xml文件中的数据
	 * @param context
	 * @param filePath
	 * @param listener
	 */
	public static void restoreSms(Context context,String filePath,ProcessListener listener){
		//恢复短信
		
		List<ContentValues> list = new ArrayList<ContentValues>();
		
		XmlPullParser parser = Xml.newPullParser();
		
		ContentResolver resovler = context.getContentResolver();
		Uri uri = Uri.parse("content://sms");
		//"address","date","type","body"
		try {
			FileInputStream fis = new FileInputStream(filePath);
			parser.setInput(fis, "UTF-8");
			int type = parser.getEventType();
			ContentValues values = null;
			while(type != XmlPullParser.END_DOCUMENT){
				
				switch (type) {
				case XmlPullParser.START_TAG:
					if("sms".equals(parser.getName())){
						values = new ContentValues();
					}
					if("address".equals(parser.getName())){
						String address = parser.nextText();
						values.put("address", address);
					}
					if("date".equals(parser.getName())){
						String date = parser.nextText();
						values.put("date", Long.parseLong(date));
					}
					if("type".equals(parser.getName())){
						String smsType = parser.nextText();
						values.put("type", smsType);
					}
					if("body".equals(parser.getName())){
						String body = parser.nextText();
						values.put("body", body);
					}
					break;
				case XmlPullParser.END_TAG:
					if("sms".equals(parser.getName())){
						//
						list.add(values);
					}
					break;
				}
				type = parser.next();//下一个事件
			}
			
			listener.max(list.size());
			for(int i = 1 ;i <=list.size(); i++){
				ContentValues contentValues = list.get(i);
				resovler.insert(uri, contentValues);
//				resovler.update(uri, contentValues, where, selectionArgs)
				listener.process(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 移除重复的短信
	 */
	public static void removeSameSms(Context context){
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse("content://sms");
		//delete from sms where _id not in(select min(_id) from sms group by date,address,type,body)
		resolver.delete(uri, "_id not in(select min(_id) from sms group by date,address,type,body)", null);		
	}
	
	
	

}
