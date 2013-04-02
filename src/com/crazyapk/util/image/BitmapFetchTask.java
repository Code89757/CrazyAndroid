package com.crazyapk.util.image;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.widget.ImageView;
/**
 * 图形下载任务管理。
 * @author wen.yugang
 *
 */
public class BitmapFetchTask {
	
	private InterlFutureTask mFuture;
	private BitmapDownloadRunable mTask;
	private Bitmap mResult;	
	private OnCompletedEvent mCompletedEvent;
	Object DATA;
	
	private final WeakReference<ImageView> mImageViewReference;
	
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "BitmapDownloadAsyncTask #"
					+ mCount.getAndIncrement());
		}
	};
	
	private static final Executor SERIAL_EXECUTOR = Executors
			.newFixedThreadPool(2, sThreadFactory);

	private static final InternalHandler sHandler = new InternalHandler();

	public BitmapFetchTask(ImageView view, String data) {
		this.mImageViewReference = new WeakReference<ImageView>(view);
		this.DATA = data;
	}

	public void excute(IImageCapturer capture) {
		mTask = new BitmapDownloadRunable(capture, this);
		mFuture = new InterlFutureTask(mTask);
		SERIAL_EXECUTOR.execute(mFuture);
	}

	public void cancel(boolean mayInterruptIfRunning) {
		mFuture.cancel(mayInterruptIfRunning);
	}
	
	private void completed() {
		ImageView view = this.mImageViewReference.get();
		if (view == null)
			return;
		Drawable map = view.getDrawable();
		if (map instanceof PandaBitmap) {
			
			PandaBitmap bitmap = (PandaBitmap) map;			
			BitmapFetchTask task = bitmap.getTask();
			
			if (task != null && task.mResult != null) {
				view.setImageBitmap(task.mResult);
			}
			
			if(mCompletedEvent != null)
				mCompletedEvent.onCompleted();
		}
	}
	
	public void setCompletedObserver(OnCompletedEvent event){
		this.mCompletedEvent = event;
	}


	private static class InterlFutureTask implements Runnable {
		Runnable pRun;
		boolean cancel = false;

		InterlFutureTask(Runnable run) {
			this.pRun = run;
		}

		public void cancel(boolean mayInterruptIfRunning) {
			cancel = true;
		}

		@Override
		public void run() {
			if (!cancel) {
				this.pRun.run();
			}
		}
	}

	private static class BitmapDownloadRunable implements Runnable {
		IImageCapturer mCapturerHelper;
		BitmapFetchTask pTask;

		public BitmapDownloadRunable(IImageCapturer chelp,
				BitmapFetchTask task) {
			this.mCapturerHelper = chelp;
			pTask = task;
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			this.pTask.mResult = this.mCapturerHelper.request();
			sHandler.obtainMessage(0, pTask).sendToTarget();
		}
	}
	
	public interface OnCompletedEvent {
		void onCompleted();
	}

	private static class InternalHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			BitmapFetchTask task = (BitmapFetchTask) msg.obj;
			if (task != null) {
				task.completed();
			}
		}
	}
}
