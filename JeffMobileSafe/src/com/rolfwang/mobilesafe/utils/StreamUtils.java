package com.rolfwang.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamUtils {
	
	/**
	 * 解析一个字节输入流，将内容解析成字符串并返回
	 * @param is 自己输入流
	 * @return 字符串
	 */
	public  static String parseInputStream(InputStream is){
		String resultStr = null;
		try {
			byte[] buf = new byte[1024];
			int len = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			baos.close();
			is.close();
			resultStr = new String(baos.toByteArray());
		} catch (Exception e) {
		}
		return resultStr;
	}

}
