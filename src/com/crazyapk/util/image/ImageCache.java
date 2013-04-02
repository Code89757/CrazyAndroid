package com.crazyapk.util.image;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

/**
 * 图像缓存
 * @author wen.yugang </br> 2012-6-14
 */
public class ImageCache {
	private static final String DISK_CACHE_PATH = "/web_image_cache/";
	private static final int MAX_SIZE = 30;
	private static String diskCachePath;
	private static boolean selfPath = false;
	protected LruCache<String, WeakReference<Bitmap>> memoryCache;
	private boolean diskCacheEnabled = false;
	private static ImageCache instance;
	
	public ImageCache(Context context) {
		memoryCache = new LruCache<String, WeakReference<Bitmap>>(MAX_SIZE);

		// 策略中，如果用户设置了自定义的路径，则不设置默认路径
		if (!selfPath) {
			Context appContext = context.getApplicationContext();
			diskCachePath = appContext.getCacheDir().getAbsolutePath() + DISK_CACHE_PATH;
		}

		File outFile = new File(diskCachePath);
		outFile.mkdirs();

		diskCacheEnabled = outFile.exists();
	}

	//单例模式
	public static ImageCache getImageCache(Context context){
		if(instance == null)
			instance = new ImageCache(context);
		return instance;
	}

	// 设置缓存路径
	public static void setCachePath(String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		diskCachePath = path;
		selfPath = true;
	}


	public void remove(String url) {
		if (url == null) {
			return;
		}
		memoryCache.remove(getKeyByUrl(url));
	}

	/**
	 * 清除内存中缓存
	 */
	public void clearMemory() {
		memoryCache.evictAll();
	}

	public void cacheBitmapToMemory(final String url, final Bitmap bitmap) {
		String key = getKeyByUrl(url);
		if (key == null || bitmap == null)
			return;
		memoryCache.put(key, new WeakReference<Bitmap>(bitmap));
	}

	public Bitmap getBitmapFromMemory(String url) {
		if (TextUtils.isEmpty(url))
			return null;
		Bitmap bitmap = null;
		String key = getKeyByUrl(url);
		WeakReference<Bitmap> softRef = memoryCache.get(key);
		if (softRef != null) {
			bitmap = softRef.get();
			if (bitmap == null || bitmap.isRecycled()) {
				memoryCache.remove(key);
				return null;
			}
		}
		return bitmap;
	}

	public Bitmap getBitmapFromDisk(String mfilePath, float width, float height){
		return getBitmapFromDisk(mfilePath, width, height, true);
	}
	public Bitmap getBitmapFromDisk(String mfilePath, float width, float height,boolean fromCache) {
		Bitmap bitmap = null;
		if (diskCacheEnabled) {
			String filePath =null; 
			if(fromCache){
				filePath=getCachePath(mfilePath);
			}else{
				filePath=mfilePath;
			}
			
			File file = new File(filePath);
			if (file.exists()) {
				bitmap = BitmapFetchHelper.getBitmapFromDisk(filePath, width, height);
			}
		}
		return bitmap;
	}

	private String getCachePath(String url) {
		return diskCachePath + getKeyByUrl(url);
	}
	
	/**
	 * 根据Url获取对应的唯一键值
	 * 
	 * @param url
	 * @return 如果url为空，只返回的数据为null.
	 */
	public static String getKeyByUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			return String.valueOf(url.hashCode());
		}
		return null;
	}
}