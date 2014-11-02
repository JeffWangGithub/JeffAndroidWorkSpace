package com.rolfwang.mobilesafe.engine;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.rolfwang.mobilesafe.domain.ContactInfo;


public class ContactsEngine {
	/**
	 * 利用系统的内容提供者获取全部联系人的信息
	 * @param context
	 * @return 无联系人信息返回null，有联系人返回list列表
	 */
	public static List<ContactInfo> getAllContacts(Context context){
		List<ContactInfo> list = null;
		
		//1，获取内容解析者
		ContentResolver resolver = context.getContentResolver();
		//创建需要的Uri，此Uri的写法需要查看系统的联系人内容提供者的源码中Provider的配置情况，已经源码的状况
		//具体查看方法：#1先查看清单文件，找到对应的provider的authorities主机名属性；
		//#2，查看Provider的Java源码中是如何定义UriMatcher的匹配信息的。
		//内容提供者uri的组成：content：// + authorities(主机名)+path(uriMatcher中的path路径)
		//matcher.addURI(ContactsContract.AUTHORITY, "raw_contacts", RAW_CONTACTS);
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");//代表查询raw_contacts表
		Uri uri2 = Uri.parse("content://com.android.contacts/data");//代表查询view_data表
		
		//2，用内容解析这，查询数据
		Cursor cursor = resolver.query(uri, new String[]{"contact_id"}, null, null, null);
		if(cursor != null){
			list = new ArrayList<ContactInfo>();
			while(cursor.moveToNext()){
				ContactInfo contactInfo = new ContactInfo();
				
				//从raw_contacts表中获取到contact_id(联系人id)
				String contact_id = cursor.getString(cursor.getColumnIndex("contact_id"));
				Cursor cursor2 = resolver.query(uri2, new String[]{"data1","mimetype"}, "contact_id=?", new String[]{contact_id}, null);
				while(cursor2.moveToNext()){
					String mimetype = cursor2.getString(cursor2.getColumnIndex("mimetype"));
					if("vnd.android.cursor.item/name".equals(mimetype)){
						//根据minetype判断是联系人的姓名
						String name = cursor2.getString(cursor2.getColumnIndex("data1"));
						contactInfo.setName(name);						
					}else if("vnd.android.cursor.item/phone_v2".equals(mimetype)){
						//联系人电话
						String phoneNum = cursor2.getString(cursor2.getColumnIndex("data1"));
						//替换电话号码中的连接符-
						phoneNum = phoneNum.replace("-", "");
						contactInfo.setNum(phoneNum);
					}else if("vnd.android.cursor.item/email_v2".equals(mimetype)){
						String email = cursor2.getString(cursor2.getColumnIndex("data1"));
						contactInfo.setEmail(email);
					}
				}
				//将联系人信息添加到List列表中
				list.add(contactInfo);				
			}
		}
		return list;
	}
}
