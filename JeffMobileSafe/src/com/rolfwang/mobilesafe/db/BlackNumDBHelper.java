package com.rolfwang.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackNumDBHelper extends SQLiteOpenHelper {


	public BlackNumDBHelper(Context context) {
		// name 数据库的名字 factory 游标工厂 version 版本号
		super(context, "black.db", null, 1);
	}

	// 数据库第一次创建的时候调用 适合初始化表结构
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建info表
		db.execSQL("create table info (id integer primary key autoincrement, blacknum varchar(20), mode integer)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
