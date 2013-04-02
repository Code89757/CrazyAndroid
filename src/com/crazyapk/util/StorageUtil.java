package com.crazyapk.util;

import java.io.File;

import com.crazyapk.util.bitmap.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;

public class StorageUtil {
	
	public static File getDiskCacheDirWithSring(Context context, String uniqueName) {
		return new File(getDiskCacheDir(context) + File.separator + uniqueName);
	}
	
	public static String getDiskCacheDir(Context context) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() : context
				.getCacheDir().getPath();

		return cachePath;
	}
	
	/**
	 * Check if external storage is built-in or removable.
	 * 
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@TargetApi(9)
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}
	
	public static File getDiskDataDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() : context
				.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Get the external app cache directory.
	 * 
	 * @param context
	 *            The context to use
	 * @return The external cache dir
	 */
	@TargetApi(8)
	public static File getExternalCacheDir(Context context) {
		if (Utils.hasFroyo()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	public static File getDiskCachePath(Context context, String string,
			String string2) {
		return null;
	}
}
