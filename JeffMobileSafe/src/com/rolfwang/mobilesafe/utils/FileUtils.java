package com.rolfwang.mobilesafe.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
	
	public static void copyFile(InputStream is, File dest){
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dest);
			
			byte[] buf = new byte[1024*4];
			int lens = -1;
			
			while((lens=is.read(buf))!=-1){
				fos.write(buf, 0, lens);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				is.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
