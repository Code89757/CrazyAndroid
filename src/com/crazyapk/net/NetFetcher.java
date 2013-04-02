package com.crazyapk.net;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.net.Proxy;

import com.crazyapk.config.SystemConst;
import com.crazyapk.download.Util;
import com.crazyapk.util.LogUtil;

/**
 * 新的获取get请求的方
 * 
 * @author wen.yugang </br> 2012-7-24
 */
public class NetFetcher {

	private static ThreadFactory sThreadFactory = new ThreadFactory() {
		AtomicInteger aAutoIncrease = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "NetFetcher # " + aAutoIncrease.get());
		}
	};

	private static Executor singleExecutors = Executors
			.newSingleThreadExecutor(sThreadFactory);
	private boolean mSingleThread = true;

	public interface INetFetcher {
		void sendCompletedMessage(String data);

		void sendErrorMessage(Throwable able);
	}

	public NetFetcher(boolean singleThread) {
		this.mSingleThread = singleThread;
	}

	public NetFetcher() {
	}

	public void requestSync(String url, INetFetcher fetcher) {
		try {
			LogUtil.d("NewNetFetcher", url);
			String data = requestByGet(url);
			// LogUtil.d("NewNetFetcher", data);
			fetcher.sendCompletedMessage(data);
		} catch (ClientProtocolException e) {
			fetcher.sendErrorMessage(e);
			e.printStackTrace();
		} catch (IOException e) {
			fetcher.sendErrorMessage(e);
			e.printStackTrace();
		}
	}

	public void request(final String url, final INetFetcher fetcher) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				requestSync(url, fetcher);
			}
		};

		if (this.mSingleThread)
			singleExecutors.execute(r);
		else
			sThreadFactory.newThread(r).start();
	}

	/**
	 * 通过GET方式向服务端请求数据
	 * 
	 * @param url
	 * @return 服务端返回的结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

	String requestByGet(String url) throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		String hostIp = Proxy.getDefaultHost();
		if (hostIp != null && !hostIp.equals("")) {
			HttpHost proxy = new HttpHost(hostIp, Proxy.getDefaultPort());
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		DefaultHttpClient client = new DefaultHttpClient();
		client.setParams(params);
		HttpResponse httpResponse = client.execute(get);
		return EntityUtils.toString(httpResponse.getEntity());
	}
}
