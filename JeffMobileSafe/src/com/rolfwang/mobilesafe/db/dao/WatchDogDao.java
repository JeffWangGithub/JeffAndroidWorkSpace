package com.rolfwang.mobilesafe.db.dao;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.rolfwang.mobilesafe.db.WatchDogDBHelper;

public class WatchDogDao {
	
	public final static int APP_LOCKED = 1;
	public final static int APP_UNLOCKED = 0;
	public final static Uri URI_LOCK_DB_CHANGED = Uri.parse("content://com.rolfwang.mobilesafe.LOCK_DB_CHANGED");
	private Context context;
	public WatchDogDao(Context context){
		this.context = context;
	}
	
	/**
	 * 添加锁定的应用程序
	 * @param packageName
	 * @return
	 */
	public long addLockedApp(String packageName){
		
		WatchDogDBHelper dbHelper = new WatchDogDBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("packagename", packageName);
		values.put("locked", WatchDogDao.APP_LOCKED);
		long insert = db.insert("info", null, values);
		db.close();		
		//数据库发生变化时，内容解析这通知内容观察者
		context.getContentResolver().notifyChange(URI_LOCK_DB_CHANGED, null);
		
		return insert;
	}
	
	
	/**
	 * 更改某个应用的锁定状态
	 * @param packageName
	 * @param lockedState
	 * @return
	 */
	public int updateLockedState(String packageName,int lockedState){
		
		if(lockedState >= 1){
			lockedState = WatchDogDao.APP_LOCKED;
		}else if(lockedState <= -1){
			lockedState = WatchDogDao.APP_UNLOCKED;
		}
		WatchDogDBHelper dbHelper = new WatchDogDBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("locked", lockedState);		
		int updateResult = db.update("info", values, "packagename = ?", new String[]{packageName});
		db.close();
		//数据库发生变化时，内容解析这通知内容观察者
		context.getContentResolver().notifyChange(URI_LOCK_DB_CHANGED, null);
		
		return updateResult;
	}
	
	/**
	 * 查询所有的数据
	 * @return
	 */
	public Map<String,Integer> queryAllLockedApp(){
		Map<String,Integer> result = new HashMap<String, Integer>();
		WatchDogDBHelper dbHelper = new WatchDogDBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("info", new String[]{"packagename", "locked"}, null, null, null, null, null);
		while(cursor.moveToNext()){
			String packageName = cursor.getString(cursor.getColumnIndex("packagename"));
			int locked = cursor.getInt(cursor.getColumnIndex("locked"));
			result.put(packageName, locked);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	
	
	

}
