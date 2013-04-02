package com.crazyapk.util.image;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class RemoteImage implements IImageCapturer {

	protected static ImageCache imageCache;
	protected ImageCache mSelfCache;
	protected String url;

	protected boolean mRoundCorner = false;
	protected Integer mCornerPixel = null;
	protected boolean mAllowStop = false;
	protected boolean mRotate = false;
	protected boolean mCut = false;

	protected float mWidth = 0;
	protected float mHeight = 0;

	protected Bitmap tplBitmap = null;

	public void setImageCache(ImageCache cache) {
		this.mSelfCache = cache;
	}
	
	public void setWidth(float width) {
		mWidth = width;
	}

	public void setHeight(float height) {
		mHeight = height;
	}

	public void setRotate(boolean rotate) {
		mRotate = rotate;
	}

	public RemoteImage(String url) {
		this.url = url;
	}

	public void setRoundCorner(boolean round) {
		mRoundCorner = round;
	}

	public void setRoundCornerPixel(Integer pixel) {
		mCornerPixel = pixel;
	}

	public void setAllowStop(boolean allowStop) {
		mAllowStop = allowStop;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public Bitmap request() {
		if (getCacheMgner() == null) {
			// imageCache = ImageCache.getImageCache(context);
		}

		if (TextUtils.isEmpty(url)) {
			return null;
		}

		Bitmap bitmap = null;

		bitmap = getCacheMgner().getBitmapFromDisk(url, mWidth, mHeight);
		if (bitmap == null) {
			bitmap = BitmapFetchHelper.downloadBitmap(url, mWidth, mHeight);
		}

		if (bitmap != null) {
			getCacheMgner().cacheBitmapToMemory(url, bitmap);
		}

		return bitmap;
	}

	@Override
	public Bitmap get() {
		if (getCacheMgner() == null) {
			return null;
		}
		return getCacheMgner().getBitmapFromMemory(url);
	}

	private ImageCache getCacheMgner() {
		return mSelfCache == null ? imageCache : mSelfCache;
	}

	@Override
	public String getCacheKey() {
		return url;
	}

	@Override
	public void recycle() {
		Bitmap bitmap = getCacheMgner().getBitmapFromMemory(url);
		if (bitmap != null) {
			getCacheMgner().remove(url);
			bitmap = null;
		}
	}
}
