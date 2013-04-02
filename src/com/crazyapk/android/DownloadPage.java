package com.crazyapk.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyapk.bean.AppBean;
import com.crazyapk.download.DownloadManager;
import com.crazyapk.download.DownloadTask;
import com.crazyapk.download.DownloadTask.DownloadState;
import com.crazyapk.download.TaskStateChanged;
import com.crazyapk.util.ApkPackageUtil;

public class DownloadPage extends Activity {

	Button mDownBtn;
	Button mDeleteBtn;
	ProgressBar mBar;

	AppBean mBean;
	DownloadManager mDm;
	Context mAppContext;
	DownloadTask mTaskDone;

	private final TaskStateChanged mTaskStateChanged = new TaskStateChanged() {

		@Override
		public void onStateChanged(DownloadTask task) {
			DownloadState state = task.getState();
			switch (state.getValue()) {
				case DownloadState.LOADING :
					int progress = state.getProgress();
					mBar.setProgress(progress);
					break;
				case DownloadState.CONNECTIING :
					mDownBtn.setText("下载中");
					mBar.setVisibility(View.VISIBLE);
					break;
				case DownloadState.DONE :
					mDownBtn.setText("安装");
					mDownBtn.setEnabled(true);
					mTaskDone = task;
					mDeleteBtn.setVisibility(View.VISIBLE);
					mBar.setVisibility(View.GONE);
					break;
				case DownloadState.ERROR :
					Toast.makeText(mAppContext, "下载失败，请重新进入界面", Toast.LENGTH_LONG).show();
					mDeleteBtn.setVisibility(View.VISIBLE);
					mBar.setVisibility(View.GONE);
					mDownBtn.setText("下载");
					mDownBtn.setEnabled(true);
					break;
				case DownloadState.PAUSE:
					mDownBtn.setText("继续下载");
					mBar.setVisibility(View.VISIBLE);
					break;
				default :
					break;
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		mTaskDone.cancel();	
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_page);

		mBean = getIntent().getParcelableExtra("DATA");

		initViews();
		mDm = new DownloadManager(mTaskStateChanged);
		mAppContext = getApplicationContext();
		mTaskDone = new DownloadTask(mAppContext, mBean.downloadUrl, mBean.name);
		mTaskDone.PackageName = mBean.identifier;
		mTaskDone.VersionCode = mBean.versionCode;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mTaskStateChanged.onStateChanged(mTaskDone);
	}

	private void initViews() {
		setTitle(this.mBean.name);

		TextView size = (TextView) findViewById(R.id.app_name);
		size.setText("大小：" + this.mBean.size);

		mBar = (ProgressBar) findViewById(R.id.progress);

		this.mDownBtn = (Button) findViewById(R.id.download_btn);
		this.mDownBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTaskDone.getState().getValue() == DownloadState.DONE) {
					ApkPackageUtil.install(mAppContext, mTaskDone.TaskFile());
					return;
				}

				if (!mTaskDone.busy()) {
					mDm.add(mAppContext, mTaskDone);
				}
				else{
					return;
				}

				mDownBtn.setEnabled(false);
			}
		});

		this.mDeleteBtn = (Button) findViewById(R.id.delete);
		this.mDeleteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if ((mTaskDone.getState().getValue() == DownloadState.DONE ||
						mTaskDone.getState().getValue() == DownloadState.ERROR) 
						&& mTaskDone.TaskFile().delete()) {		
					
					mDownBtn.setText("下载");
					mBar.setProgress(0);
					mBar.setVisibility(View.GONE);
					mDeleteBtn.setVisibility(View.GONE);
				}
			}
		});
	}
}
