package com.crazyapk.download;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;

import com.crazyapk.android.db.DownloadSqliteHelper;
import com.crazyapk.util.StorageUtil;

/**
 * 任务管理
 * 
 * @author wyg
 * 
 */
public class DownloadManager implements DownloadTask.IStateChangedObserver {

	private static Executor sSingleExcutor = Executors.newSingleThreadExecutor();

	private final Handler mStateChangedHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {			
			mTaskStateChanged.excute();
		};
	};
	
	TaskStateChanged mTaskStateChanged;

	public DownloadManager(TaskStateChanged changed) {
		mTaskStateChanged = changed;
	}
	
	public DownloadManager() {
	}

	public void add(Context context, DownloadTask task) {		
		task.setStateChangedObserver(this);
		sSingleExcutor.execute(task);
	}

	@Override
	public void onStateChanged(DownloadTask task) {
		if(mTaskStateChanged == null)
			return;
		mTaskStateChanged.setInfo(task);
		mStateChangedHandler.sendMessage(mStateChangedHandler.obtainMessage(0,mTaskStateChanged));
	}
	
	public ArrayList<DownloadTask> getAllTask(Context context){
		//查找未完成的文件
		File cacheDir = StorageUtil.getDiskCacheDirWithSring(context, "APK");
		FilenameFilter filter = new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith("."+DownloadTask.TEMP_SUFFIX)){
					return true;
				}
				return false;
			}
		};
		File[] list = cacheDir.listFiles(filter);	
		
		//查找对应文件的信息
		ArrayList<DownloadTask> task = DownloadSqliteHelper.search(context);
		DownloasTaskFilter tf = new DownloasTaskFilter() {
			@Override
			public boolean accept(DownloadTask t, File file) {
				return file.getName().equalsIgnoreCase(Util.md5(t.DownloadUrl));
			}
		};	
		
		return listTasks(task,list,tf);
	}
	
	private ArrayList<DownloadTask> listTasks(List<DownloadTask> task, File[] list, DownloasTaskFilter tf) {
		ArrayList<DownloadTask> ts = new ArrayList<DownloadTask>();		
		final int len = list == null ? 0 : list.length;		
		for (int i = 0; i < len; i++) {
			File file = list[i];
			for (Iterator<DownloadTask> iterator = ts.iterator(); iterator.hasNext();) {
				DownloadTask t = (DownloadTask) iterator.next();
				if(tf.accept(t,file)){
					ts.add(t);
					break;
				}	
			}
		}
		return ts;
	}

	interface DownloasTaskFilter{
		boolean accept(DownloadTask t, File file);
	}
}
