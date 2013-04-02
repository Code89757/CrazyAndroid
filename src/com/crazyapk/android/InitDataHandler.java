package com.crazyapk.android;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.util.SparseArray;

public class InitDataHandler {
	/**
	 * 初始化下载任务
	 */
	static SparseArray<Boolean> mSa = new SparseArray<Boolean>(1);
	
	public static final int KEY_INVAID = -1;
	public static final int KEY_CLLECTION_DOWNLOAD_TASK = 0;
	
	static{
		mSa.append(KEY_INVAID, false);
		mSa.append(KEY_CLLECTION_DOWNLOAD_TASK, false);
	}
	
	/**
	 * 初始化数据为单线程模式。
	 */
	static Executor sSingleExcutor = Executors.newSingleThreadExecutor();
	
	void excute(InitDataRunable command){
		sSingleExcutor.execute(command);
	}
	
	public abstract class InitDataRunable implements Runnable{
		protected int mSaKey = KEY_INVAID;
		
		public InitDataRunable(int key){
			this.mSaKey = key;
		}		
		protected void postDone(){
			if (this.mSaKey != KEY_INVAID && this.mSaKey < mSa.size())
				mSa.put(mSaKey, true);
		}
	}
	
	boolean done(int key){
		return mSa.get(key);
	}
}
