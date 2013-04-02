package com.crazyapk.android;

import java.util.ArrayList;
import java.util.Iterator;

import com.crazyapk.android.InitDataHandler.InitDataRunable;
import com.crazyapk.config.SystemConst;
import com.crazyapk.download.DownloadManager;
import com.crazyapk.download.DownloadTask;
import com.crazyapk.util.LogUtil;
import com.crazyapk.util.image.ImageCache;

import android.app.Application;

public class CApplication extends Application {
	protected String tag = CApplication.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		InitDataHandler handler = new InitDataHandler();
		final DownloadManager dm = new DownloadManager();
		InitDataRunable command = handler.new InitDataRunable(InitDataHandler.KEY_CLLECTION_DOWNLOAD_TASK) {
			
			@Override
			public void run() {
				DataCacheContainer.AllTasks = dm.getAllTask(getApplicationContext());
				ArrayList<DownloadTask> array = new ArrayList<DownloadTask>();
				array.addAll(DataCacheContainer.AllTasks);
				for (Iterator<DownloadTask> iterator = array.iterator(); iterator.hasNext();) {
					DownloadTask downloadTask = (DownloadTask) iterator.next();
					LogUtil.v(tag, downloadTask.toString());
				}
				postDone();
			}
		};
		handler.excute(command);
		
		SystemConst.init(this);
		
		ImageCache.getImageCache(this);
	}
}
