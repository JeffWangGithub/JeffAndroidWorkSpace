package com.rolfwang.mobilesafe;

import android.content.Intent;
import android.os.Bundle;

public class Setup1Activity extends SetupBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setup1);
	}

	@Override
	public void next_activity() {
		Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
		startActivity(intent);
		finish();
		//实现动画切换的方法
		overridePendingTransition(R.anim.next_new_enter, R.anim.next_old_out);
		
	}

	@Override
	public void previous_activity() {
		
		
	}
}
