package com.rolfwang.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WatchDogDBHelper extends SQLiteOpenHelper {
	
	public WatchDogDBHelper(Context context){
		super(context, "lock.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table info(id integer primary key autoincrement, packagename varchar(100), locked integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
