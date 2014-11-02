package com.rolfwang.mobilesafe.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
	
	public static String md5Digest(String str){
		//注意null和""空字符也可以进行加密
		//判断字符串是否为空
		if(str==null||"".equals(str)){
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] byteArr = digest.digest(str.getBytes());
			//将字节数组转化成正数，1表示整数，-1表示负数，0表示0
			BigInteger bigInteger = new BigInteger(1, byteArr);
			//转换成十六进制字符串
			String resultStr = bigInteger.toString(16);
			return resultStr;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

}
