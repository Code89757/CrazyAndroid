package com.crazyapk.util;

public class TimeTickUtil {
	long mStart;
	long mTickPoint;
	String Tag = null;
	
	public TimeTickUtil(){
		this.Tag = TimeTickUtil.class.getSimpleName();
	}
	
	public TimeTickUtil(String tag){
		this.Tag = tag;
	}

	public void start(String tag) {
		mTickPoint = mStart = System.currentTimeMillis();
		LogUtil.i(Tag, tag + ":start=>");
	}

	public void end(String tag) {
		LogUtil.i(Tag, tag + ":end=>" + (System.currentTimeMillis() - mStart));
	}

	public void print(String tag) {
		LogUtil.i(Tag, tag + ":inteval=>" + (System.currentTimeMillis() - mTickPoint));
		mTickPoint = System.currentTimeMillis();
	}
}
