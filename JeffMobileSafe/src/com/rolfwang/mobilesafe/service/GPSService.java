package com.rolfwang.mobilesafe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class GPSService extends Service {

	private LocationManager locationManager;
	private SharedPreferences sp;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		sp = getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
		
		//1,获取LocationManager对象
		locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
		
		//2，获取最佳的定位方式
		Criteria criteria = new Criteria();
		criteria.setAltitudeRequired(true);
		String bestProvider = locationManager.getBestProvider(criteria, true);
		//3，请求定位
		locationManager.requestLocationUpdates(bestProvider, 0, 0, new MyLocationListener());
		
		
	}
	private class MyLocationListener implements LocationListener{

		//位置改变时调用的方法
		@Override
		public void onLocationChanged(Location location) {
			
			double latitude = location.getLatitude();//纬度
			double longitude = location.getLongitude();//经度
			float accuracy = location.getAccuracy();//精确度
			System.out.println( latitude+"::"+longitude+"::"+accuracy);
			
			//存入配置文件
			Editor edit = sp.edit();
			edit.putString("latitude", latitude+"");
			edit.putString("longitude", longitude+"");
			edit.putString("accuracy", accuracy+"");
			edit.commit();			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}
		
	}
	
}
