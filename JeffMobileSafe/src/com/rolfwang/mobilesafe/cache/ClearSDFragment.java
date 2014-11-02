package com.rolfwang.mobilesafe.cache;

import com.rolfwang.mobilesafe.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ClearSDFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_sd_clear, null);
	}
	
	

}
