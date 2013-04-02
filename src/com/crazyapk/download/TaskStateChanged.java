package com.crazyapk.download;


public abstract class TaskStateChanged implements DownloadTask.IStateChangedObserver {
	DownloadTask pTask;

	public void setInfo(DownloadTask task) {
		this.pTask = task;
	}

	@Override
	public abstract void onStateChanged(DownloadTask task);
	
	void excute(){
		onStateChanged(pTask);
	}

}
