package com.rolfwang.mobilesafe.db.dao;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AddressDao {

	public AddressDao() {
		super();
	}

	/**
	 * 查询电话的归属地
	 * 
	 * @return
	 */
	public String queryAddress(String phoneNum, Context context) {
		String result = null;

		File parent = context.getFilesDir();
		File dbFile = new File(parent, "address.db");

		//打开一个已有的数据库文件**************
		// 第一个参数：数据库的路径；第二个游标工厂：null表示使用系统自带游标；第三个参数：数据库的访问模式
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
		
		//判断phoneNum是否为电话号码
		String regex = "[1][3578]\\d{5,9}";
		if(phoneNum.matches(regex)){
			//如果号码为手机号，则进行查询数据库
			// select location from data2 where id = (select outkey from data1 where
			// id = 1300002);
			String sql = "select location from data2 where id = (select outkey from data1 where id = ?)";
			Cursor cursor = db.rawQuery(sql,
					new String[] { phoneNum.substring(0, 7) });
			while (cursor.moveToNext()) {
				result = cursor.getString(cursor.getColumnIndex("location"));
			}
		}else{

			switch (phoneNum.length()) {
			case 3:   // 110  120  999  911
				result="特殊号码";
				break;
			case 4 :
			
				result="模拟器";
				break;
			case 5:   // 95588
				result="服务电话";
				break;
			case 6:    // 本地电话
			case 7:
			case 8:  
				result="本地电话";
				break;  
			default:
				//查询电话的归属地
				if(phoneNum.startsWith("0")&&phoneNum.length()>=10){
					//result="长途电话";  phonenum   1112313123
					//  3 位   4位 
					Cursor cursor = db.rawQuery("select location from data2 where area=?", new String[]{phoneNum.substring(1, 3)});
					if(cursor.moveToNext()){
						result=cursor.getString(0);
						result=result.substring(0, result.length()-2);
						cursor.close();
					}else{
						cursor=db.rawQuery("select location from data2 where area=?", new String[]{phoneNum.substring(1, 4)});
						if(cursor.moveToNext()){
							result=cursor.getString(0);
							result=result.substring(0, result.length()-2);
							cursor.close();
						}
					}
				}
				break;
			}
		}
		return result;
	}

}
