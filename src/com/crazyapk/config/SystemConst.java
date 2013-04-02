package com.crazyapk.config;

import java.io.File;

import com.crazyapk.util.StorageUtil;

import android.content.Context;

public class SystemConst {
	public static String CACHE_DIR = null;
	public static String CACHE_IMAGE_DIR = null;

	public static Context APP_CONTEXT;

	public static void init(Context context) {
		CACHE_DIR = StorageUtil.getDiskCacheDir(context);
		CACHE_IMAGE_DIR = CACHE_DIR + File.separator + "img";

		APP_CONTEXT = context.getApplicationContext();
	}
}
