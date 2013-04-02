package com.crazyapk.util.image;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class PandaBitmap extends BitmapDrawable {
	private WeakReference<BitmapFetchTask> mTaskRef;

	public PandaBitmap(Resources res, Bitmap loadingBitmap,
			BitmapFetchTask task) {
		super(res, loadingBitmap);
		this.mTaskRef = new WeakReference<BitmapFetchTask>(task);
	}
	
	public BitmapFetchTask getTask(){
		return this.mTaskRef.get();
	}

}
