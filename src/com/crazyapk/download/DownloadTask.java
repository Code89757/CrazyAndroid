package com.crazyapk.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.crazyapk.android.db.DownloadSqliteHelper;
import com.crazyapk.config.LogConfig;
import com.crazyapk.config.SystemConst;
import com.crazyapk.util.FileUtil;
import com.crazyapk.util.LogUtil;
import com.crazyapk.util.StorageUtil;

/**
 * 下载任务
 * 
 * @author Administrator
 * 
 */
public class DownloadTask implements Runnable {

	final static String tag = DownloadTask.class.getSimpleName();
	final static int TIME_OUT = 30 * 1000;
	final static long PIECES_SIZE = 150 * 1024;
	final static String TEMP_SUFFIX = "dt";
	final static String INFO_SUFFIX = "info";

	private Context context;
	/**
	 * 下载名
	 */
	public String Name;
	/**
	 * 下载状态
	 */
	private DownloadState mState = new DownloadState();
	/**
	 * 下载地址
	 */
	public String DownloadUrl;
	/**
	 * 下载保存的文件名
	 */
	private String fileName;
	/**
	 * 下载缓存目录
	 */
	private File cacheDir;
	/**
	 * 临时下载文件，保存下载数据
	 */
	private File tempFileDt;
	/**
	 * 取消下载
	 */
	private boolean canceled;
	/**
	 * 包名
	 */
	public String PackageName;

	public long VersionCode;
	
	private IStateChangedObserver mStateObserver;
	private Proxy mWapProxy;


	public DownloadTask(Context ctx, String url, String name) {
		this.context = ctx.getApplicationContext();
		this.DownloadUrl = url;
		this.Name = name;

		initInfo();
	}

	private boolean initInfo() {

		//创建下载任务的基础信息
		fileName = getKey(DownloadUrl);
		cacheDir = StorageUtil.getDiskCacheDirWithSring(context, "APK");
		tempFileDt = new File(cacheDir, new StringBuffer().append(fileName)
				.append(".").append(TEMP_SUFFIX).toString());

		initState();

		return true;
	}

	/**
	 * 初始化下载任务的状态
	 */
	private void initState() {
		//如果文件已经存在，下载完成
		File doneFile = new File(cacheDir, fileName);
		if(doneFile.exists()){
			mState.pState = DownloadState.DONE;
		}
		else if(tempFileDt.exists()){
			
			mState.pState = DownloadState.PAUSE;
			mState.pCurr = tempFileDt.length();
			
			initBaseInfo();
		}
		else{
			mState.pState = DownloadState.PENDDING;
		}
	}
	/**
	 * 从数据库中获取信息
	 */
	private void initBaseInfo() {
		 DownloadSqliteHelper.query(SystemConst.APP_CONTEXT, Name, this);
	}

	/**
	 * 分片下载
	 * 
	 * @throws IOException
	 */
	private void slicesDownload() throws IOException {
		if (mWapProxy == null) {
			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "没有可用代理");
			return;
		}

		if (LogConfig.log_download_apk) {
			LogUtil.d(tag, String.valueOf(tempFileDt.getAbsolutePath()));
			LogUtil.d(
					tag,
					MessageFormat.format("下载地址：{0}",
							DownloadUrl.substring(0, 40)));
		}

		disableConnectionReuseIfNecessary();

		URL url = new URL(DownloadUrl);

		mState.pCurr = tempFileDt.length();

		// 收集下载前的信息
		{
			if (LogConfig.log_download_apk) {
				LogUtil.d(tag, "信息收集..");
			}

			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection(mWapProxy);
			initConnection(conn);
			conn.setRequestProperty("RANGE", "bytes=0-2");
			conn.connect();
			int responseCode = conn.getResponseCode();
			if (responseCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {

				if (LogConfig.log_download_apk)
					LogUtil.e(tag, MessageFormat.format("服务端错误：", responseCode));

				conn.disconnect();
				postError();
				return;
			}

			mState.pTotal = parseContentLength(conn
					.getHeaderField("Content-Range"));
			
			//更新数据
			putBaseInfoToDataBase();

			if (LogConfig.log_download_apk) {
				LogUtil.d(tag, "初次请求的响应");
				printResponseHeader(conn.getHeaderFields());
			}

			conn.disconnect();
		}

		int restSize = -1;
		// 验证是否需要下载
		{
			if (mState.pTotal == mState.pCurr) {
				postDone();

				if (LogConfig.log_download_apk)
					LogUtil.d(tag, MessageFormat.format("文件已经下载完成,无需下载:{0}",
							mState.pCurr));

				return;
			} else if (mState.pTotal < mState.pCurr) {

				if (LogConfig.log_download_apk)
					LogUtil.e(tag, MessageFormat.format("文件下载异常 ，大小不一致：{0}",
							mState.pTotal + "<" + mState.pCurr));

				tempFileDt.delete();
				postError();
				return;
			}

			restSize = (int) (mState.pTotal - mState.pCurr);

			if (LogConfig.log_download_apk) {
				LogUtil.d(tag, "总大小:" + mState.pTotal);
				LogUtil.d(tag, "已下载:" + mState.pCurr);
				LogUtil.d(tag, "剩余:" + restSize);
				LogUtil.d(tag, "开始下载");
			}
		}

		/*
		 * if (LogConfig.log_download_apk){ postDone(); return; }
		 */

		// 开始执行分片下载
		updateState(DownloadState.CONNECTIING);
		while (mState.getValue() == DownloadState.PENDDING
				|| mState.pCurr < mState.pTotal) {

			if (canceled) {
				if (LogConfig.log_download_apk)
					LogUtil.d(tag, "用户取消了操作");
				postPause();
				break;
			}

			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection(mWapProxy);

			initConnection(conn);

			if (Build.VERSION.SDK != null
					&& Integer.parseInt(Build.VERSION.SDK) > 13) {
				conn.setRequestProperty("Connection", "close");
			}

			if (restSize > PIECES_SIZE) {
				restSize = (int) (mState.pCurr + PIECES_SIZE);

				if (LogConfig.log_download_apk)
					LogUtil.d(tag, "切片");

			} else {
				restSize += mState.pCurr;

				if (LogConfig.log_download_apk)
					LogUtil.d(tag, "下载剩余的数据");
			}

			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "下载范围:" + mState.pCurr + "-" + restSize);

			conn.setRequestProperty("RANGE", "bytes=" + mState.pCurr + "-"
					+ restSize);
			conn.connect();

			if (LogConfig.log_download_apk) {
				LogUtil.d(tag, "响应码：" + conn.getResponseCode());
				LogUtil.d(tag, "请求后的响应头:");
				printResponseHeader(conn.getHeaderFields());
			}

			writeStream(conn.getInputStream());

			conn.disconnect();

			restSize = (int) (mState.pTotal - mState.pCurr);

			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "已完成:" + mState.pCurr);

			if (restSize <= 0) {
				postDone();
			} else if (LogConfig.log_download_apk)
				LogUtil.d(tag, "还剩余:" + restSize);
		}

	}

	private long parseContentLength(String headerField) {
		if (TextUtils.isEmpty(headerField)) {
			return 0;
		}

		int pos = headerField.indexOf('/') + 1;
		if (pos < 0 || pos >= headerField.length())
			return 0;

		String value = headerField.substring(pos);

		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			if (LogConfig.log_download_apk)
				LogUtil.e(tag, "无法获取实际大小");
		}
		return 0;
	}

	private void writeStream(InputStream inputStream) throws IOException {
		if (mState.pCurr > 0) {
			RandomAccessFile raf = new RandomAccessFile(tempFileDt, "rw");
			raf.seek((int) mState.pCurr);
			writeStream(inputStream, new FileOutputStream(raf.getFD()),
					(int) mState.pCurr);
		} else {
			writeStream(inputStream, new FileOutputStream(tempFileDt), 0);
		}

	}

	private void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	private void printResponseHeader(Map<String, List<String>> map) {
		Set<Entry<String, List<String>>> set = map.entrySet();
		Iterator<Entry<String, List<String>>> iterator = set.iterator();

		while (iterator.hasNext()) {
			LogUtil.v(tag, String.valueOf(iterator.next()));
		}
	}

	private void writeStream(InputStream is, OutputStream os, int beginPos)
			throws IOException {
		updateState(DownloadState.LOADING);

		byte[] cacheBuffer = new byte[1024];
		final BufferedInputStream bis = new BufferedInputStream(is, 8192);
		int read = 0;
		int total = beginPos;
		while (true) {

			if (canceled) {
				break;
			}

			read = bis.read(cacheBuffer);
			if (read == -1) {
				break;
			}

			total += read;
			os.write(cacheBuffer, 0, read);

			updateProgress(total);
		}

		if (!canceled) {
			os.flush();
		}

		try {
			os.close();
		} catch (final IOException e) {
		}

		try {
			bis.close();
		} catch (final IOException e) {
		}

		try {
			is.close();
		} catch (final IOException e) {
		}
	}

	private void initConnection(HttpURLConnection conn) throws IOException {
		conn.setConnectTimeout(TIME_OUT);
		conn.setReadTimeout(TIME_OUT);
		conn.setRequestMethod("GET");
	}

	@Override
	public void run() {
		// 设置当前线程的优先级别
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		putBaseInfoToDataBase();
		
		resetState();

		if (isDone()) {
			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "下载已经完成");
			postDone();
			return;
		}

		// 判断网络
		String type = Util.currentConnetionType(context);

		if (TextUtils.isEmpty(type)) {

			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "无法判断当前无网络");

		}

		if (LogConfig.log_download_apk)
			LogUtil.d(tag, "当前网络为：" + type);

		if (!type.toLowerCase().endsWith("wifi")
				&& !type.toLowerCase().contains("net")) {
			// 如果是wap方式，要加网关
			InetSocketAddress iska = null;

			if (type.toLowerCase().equals("ctwap")) {
				iska = new InetSocketAddress("10.0.0.200", 80);
			} else if (type.toLowerCase().contains("wap")) {
				iska = new InetSocketAddress("10.0.0.172", 80);
			} else {
				iska = new InetSocketAddress(
						android.net.Proxy.getDefaultHost(),
						android.net.Proxy.getDefaultPort());
			}

			mWapProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, iska);

			if (LogConfig.log_download_apk)
				LogUtil.d(tag, "成功创建代理:" + mWapProxy.toString());

			try {
				slicesDownload();
			} catch (Exception e) {
				e.printStackTrace();

				if (LogConfig.log_download_apk)
					LogUtil.e(tag, "下载异常:" + e.getMessage());

				postError();
			}

		} else {

			try {
				breakpointTransmission();
			} catch (Exception e) {
				e.printStackTrace();

				if (LogConfig.log_download_apk)
					LogUtil.e(tag, "下载异常:" + e.getMessage());

				postError();
			}
		}
	}

	private void putBaseInfoToDataBase() {
		DownloadSqliteHelper.put(context, this);
	}

	private void resetState() {
		this.canceled = false;
	}

	/**
	 * 断点续传
	 * 
	 * @throws IOException
	 */
	private void breakpointTransmission() throws IOException {
		if (LogConfig.log_download_apk) {
			LogUtil.d(tag, String.valueOf(tempFileDt.getAbsolutePath()));
			LogUtil.d(tag, "下载地址：" + DownloadUrl.substring(0, 40));
		}

		disableConnectionReuseIfNecessary();
		URL url = new URL(DownloadUrl);
		mState.pCurr = tempFileDt.length();

		if (LogConfig.log_download_apk)
			LogUtil.d(tag, "开始下载");

		// 开始下载
		updateState(DownloadState.CONNECTIING);
		{
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			initConnection(conn);
			conn.setRequestProperty("RANGE", "bytes=" + mState.pCurr + "-");

			conn.connect();
			mState.pTotal = conn.getContentLength();

			if (mState.pCurr == mState.pTotal) {
				conn.disconnect();
				postDone();
				return;
			} else if (mState.pTotal < mState.pCurr) {
				if (LogConfig.log_download_apk)
					LogUtil.e(tag, "文件下载异常 ，大小不一致：" + mState.pTotal + "<"
							+ mState.pCurr);

				tempFileDt.delete();

				conn.disconnect();
				postError();
				return;
			}

			writeStream(conn.getInputStream());

			conn.disconnect();

			if (canceled) {
				if (LogConfig.log_download_apk)
					LogUtil.d(tag, "用户取消了操作");
				postPause();
				return;
			}
		}

		postDone();
	}

	private String getKey(String url) {
		return Util.md5(url);
	}

	private void postError() {
		updateState(DownloadState.ERROR);
	}

	private void postDone() {
		File desFile = new File(cacheDir, fileName);
		FileUtil.rename(tempFileDt, desFile);

		updateState(DownloadState.DONE);

		if (LogConfig.log_download_apk)
			LogUtil.d(tag, "下载结束");
	}

	private void postPause() {
		canceled = false;
		updateState(DownloadState.PAUSE);
		if (LogConfig.log_download_apk)
			LogUtil.d(tag, "下载暂停");
	}

	private void updateProgress(int progress) {
		mState.pCurr = progress;
		notifyStateChanged();
	}

	private void updateState(int state) {
		mState.pState = state;
		notifyStateChanged();
	}

	private void notifyStateChanged() {
		if (mStateObserver != null)
			mStateObserver.onStateChanged(this);
	}

	public void setStateChangedObserver(IStateChangedObserver observer) {
		this.mStateObserver = observer;
	}

	public File TaskFile() {
		String fileName = getKey(DownloadUrl);
		File fileDir = StorageUtil.getDiskCacheDirWithSring(context, "APK");
		return new File(fileDir, fileName);
	}

	/**
	 * 开始任务前，使用该函数直接判断是否已经下载。
	 * 
	 * @return
	 */
	public boolean isDone() {
		return getState().done();
	}

	/**
	 * 获取当前任务的下载状态
	 * 
	 * @return
	 */
	public DownloadState getState() {
		return mState;
	}

	/**
	 * 侦听状态改变的观察者
	 */
	public interface IStateChangedObserver {
		void onStateChanged(DownloadTask task);
	}

	public void cancel() {
		this.canceled = true;
	}

	@Override
	public String toString() {
		return "DownloadTask [Name=" + Name + ", DownloadUrl=" + DownloadUrl
				+ "]";
	}

	/**
	 * 下载的当前状态。保存下载的一下临时信息
	 * 
	 * @author wyg
	 * 
	 */
	public class DownloadState {
		public final static int PENDDING = 0;
		public final static int CONNECTIING = 1;
		public final static int LOADING = 2;
		public final static int PAUSE = 3;
		public final static int DONE = 4;
		public final static int ERROR = 5;

		/**
		 * 需要下载的文件总大小
		 */
		long pTotal;
		/**
		 * 已经下载的文件大小
		 */
		long pCurr;
		/**
		 * 下载的状态
		 */
		int pState = PENDDING;

		public int getProgress() {
			int progress = (int) (this.pCurr * 100 / this.pTotal);
			LogUtil.v(tag, "下载进度：" + this.pCurr + "/" + this.pTotal);
			return progress;
		}

		public boolean done() {
			return this.pState == DONE;
		}

		public int getValue() {
			return pState;
		}
		
		public long size(){
			return pTotal;
		}

		int state() {
			return pState;
		}
	}

	public boolean busy() {
		return this.mState.pState == DownloadState.CONNECTIING
				|| this.mState.pState == DownloadState.LOADING ;
	}

	public void setSize(long value) {
		mState.pTotal = value;
	}

}
