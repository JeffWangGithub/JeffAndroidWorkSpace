package com.rolfwang.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rolfwang.mobilesafe.db.BlackNumDBHelper;
import com.rolfwang.mobilesafe.domain.BlackNumInfo;

public class BlackNumDao {
	public static final int MODE_SMS = 0;
	public static final int MODE_CALL = 1;
	public static final int MODE_ALL = 2;

	
	private Context context;
	
	public BlackNumDao(Context context){
		this.context = context;
	}
	
	/**
	 *插入一条黑名单数据
	 */
	public void insertBlackNum(BlackNumInfo blackNumInfo){
		SQLiteDatabase db = null;
		try {
			BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
			db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("blacknum", blackNumInfo.getBlackNum());
			values.put("mode", blackNumInfo.getMode());
			db.insert("info", null, values);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			//关闭数据库
			db.close();
		}
	}
	
	/**
	 *插入一条黑名单数据
	 */
	public void insertBlackNum(String blacknum,int mode){
		SQLiteDatabase db = null;
		try {
			BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
			db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("blacknum", blacknum);
			values.put("mode", mode);
			db.insert("info", null, values);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			//关闭数据库
			db.close();
		}
	}

	/**
	 * 根据电话号码删除一条黑名单记录
	 */
	public int deleteBlackNum(String blackNum){
		int result;
		SQLiteDatabase db = null;
		try {
			BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
			db = dbHelper.getWritableDatabase();
			result = db.delete("info", "blacknum = ?", new String[] { blackNum });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			db.close();
		}
		return result;
	}
	
	/**
	 * 查询所有数据
	 */
	public List<BlackNumInfo> queryAll(){
		
		List<BlackNumInfo> list = new ArrayList<BlackNumInfo>();
		
		BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.query("info", null, null, null, null, null, "id desc");
		BlackNumInfo blackNumInfo = null;
		
		while(cursor.moveToNext()){
			blackNumInfo = new BlackNumInfo();
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String blackNum = cursor.getString(cursor.getColumnIndex("blacknum"));
			int mode = cursor.getInt(cursor.getColumnIndex("mode"));
			blackNumInfo.setId(id);
			blackNumInfo.setBlackNum(blackNum);
			blackNumInfo.setMode(mode);
			list.add(blackNumInfo);
		}
		
		//关闭
		cursor.close();
		db.close();		
		return list;
	}
	
	/**
	 * 分批查询数据
	 */
	public List<BlackNumInfo> queryPart(int pageSize,int offset){
		List<BlackNumInfo> list = new ArrayList<BlackNumInfo>();
		
		BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from info limit ? offset ?", new String[]{pageSize+"",offset+""});
		BlackNumInfo blackNumInfo = null;
		while(cursor.moveToNext()){
			blackNumInfo = new BlackNumInfo();
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String blackNum = cursor.getString(cursor.getColumnIndex("blacknum"));
			int mode = cursor.getInt(cursor.getColumnIndex("mode"));
			blackNumInfo.setId(id);
			blackNumInfo.setBlackNum(blackNum);
			blackNumInfo.setMode(mode);
			list.add(blackNumInfo);
		}
		//关闭
		cursor.close();
		db.close();		
		return list;
	}
	
	//"create table info (id integer primary key autoincrement, blacknum varchar(20), mode integer)"
	
	/**
	 * 根据电话号码查询拦截模式
	 * @param phoneNum
	 * @return 未查询到返回-1，查到则返回响应的值
	 */
	public int queryMode(String phoneNum){
		
		BlackNumDBHelper dbHelper = new BlackNumDBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("info", new String[]{"mode"}, "blacknum=?", new String[]{phoneNum}, null, null, null);
		if(cursor.moveToNext()){
			int mode = cursor.getInt(0);
			return mode;
		}
		return -1;
	}
	
	
}
