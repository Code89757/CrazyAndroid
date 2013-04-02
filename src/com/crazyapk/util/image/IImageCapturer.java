package com.crazyapk.util.image;

import android.graphics.Bitmap;
/**
 * 图像获取接口
 * @author wen.yugang </br> 2012-6-14
 */
public interface IImageCapturer {
    Bitmap request();
    Bitmap get();
    String getCacheKey();
	void recycle();
}