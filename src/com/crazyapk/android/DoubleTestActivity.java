package com.crazyapk.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DoubleTestActivity extends Activity {

	static String DATA_URL = "http://bbx2.sj.91.com/softs.ashx?act=225&iv=7&pi=1&tagid=3&mt=4&sv=3.2.9&osv=4.1&cpu=armeabi-v7a,armeabi&imei=860308026089173&imsi=460005051701878&nt=10&dm=MI+2";
	static String SEARCH_URL = "http://ressearch.sj.91.com/service.ashx?act=203&size=25&proj=300&iv=7&keyword=91%E6%A1%8C%E9%9D%A2&mt=4&sv=3.2.9&osv=4.1&cpu=armeabi-v7a,armeabi&imei=860308026089173&imsi=460005051701878&nt=10&dm=MI+2&page=1";
	static String MAIN_URL = "http://bbx2.sj.91.com/softs.ashx?act=225&iv=7&pi=1&tagid=1&mt=4&sv=3.2.9&osv=4.1&cpu=armeabi-v7a,armeabi&imei=860308026089173&imsi=460005051701878&nt=10&dm=MI+2";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.double_test);

		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				startActivity(SEARCH_URL);
			}
		});

		btn = (Button) findViewById(R.id.button2);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				startActivity(DATA_URL);
			}
		});
		
		btn = (Button) findViewById(R.id.button3);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				startActivity(MAIN_URL);
			}
		});
	}
	
	void startActivity(String url){
		Intent intent = new Intent(this,MainActivity.class);
		intent.putExtra("URL", url);
		startActivity(intent);
	}
}
