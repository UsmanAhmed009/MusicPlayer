package com.sillylife.simpleaudioplayer;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class FullscreenActivity extends AppCompatActivity {
	/**
	 * This project was created by Shubham Ratrey
	 * Date - 30 Jan 2019
	 */

	private View mContentView;


	@TargetApi(Build.VERSION_CODES.KITKAT)
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
//		mContentView = findViewById(R.id.fullscreen_content);
//		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//				| View.SYSTEM_UI_FLAG_FULLSCREEN
//				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

	}
}
