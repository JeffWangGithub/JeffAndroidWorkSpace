package com.rolfwang.mobilesafe.advanced;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rolfwang.mobilesafe.R;
import com.rolfwang.mobilesafe.db.dao.AddressDao;

public class QueryAddressActivity extends Activity {
	@ViewInject(R.id.et_phoneNum)
	private EditText et_phoneNum;
	@ViewInject(R.id.tv_address)
	private TextView tv_address;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query_address);
		
		ViewUtils.inject(this);
		
		et_phoneNum.addTextChangedListener(new TextWatcher() {
			//当文本内容改变时，调用
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				AddressDao addressDao = new AddressDao();
				String address = addressDao.queryAddress(s.toString(), getApplicationContext());
				tv_address.setText(address);	
			}
			//改变之前调用
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			//文本改变之后调用
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	
	public void query(View v){
		String phoneNum = et_phoneNum.getText().toString().trim();
		if(TextUtils.isEmpty(phoneNum)){
			//如果输入的内容为空，让EditText进行抖动的效果
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			et_phoneNum.startAnimation(shake); //让EditView开始动画的方法
			
			//调用系统震动器进行震动
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(200);//震动200毫秒
			return;
		}
		
		AddressDao addressDao = new AddressDao();
		String address = addressDao.queryAddress(phoneNum, this);
		tv_address.setText(address);		
	}
	
	
	
	

}
