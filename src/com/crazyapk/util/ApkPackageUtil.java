package com.crazyapk.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ApkPackageUtil {
	
	public static void install(Context context,File file) {
		if(!file.exists()){
			return;
		}		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
