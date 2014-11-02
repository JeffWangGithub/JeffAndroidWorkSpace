package com.rolfwang.mobilesafe.db.dao;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AntivirusDao {
	Context context;

	public AntivirusDao(Context context) {
		this.context = context;
	}

	public  boolean isVirus(String signatureMD5) {

		boolean flag;
		File dbFile = new File(context.getFilesDir(), "antivirus.db");
//		System.err.println(dbFile.getPath()+"----------");
		// 打开数据库
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.query("datable", new String[] { "name" }, "md5=?",
				new String[] { signatureMD5 }, null, null, null);
		if(cursor.moveToNext()){
			flag = true;			
		}else{
			flag = false;
		}
		cursor.close();
		db.close();
		return flag;
	}

}
