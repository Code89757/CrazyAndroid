package com.crazyapk.util.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 百宝箱图像的默认行为ImageView
 * 
 * @author wen.yugang </br> 2012-6-13
 */
public class ImageViewLoader {

	public static void load(ImageView view, IImageCapturer capture, int resId) {
		if(view == null || capture == null){
			return;
		}

		final ImageView imageView = view;

		// 内存中获取
		Bitmap tmpBitmap = capture.get();
		if (tmpBitmap != null) {
			imageView.setImageBitmap(tmpBitmap);
			return;
		}

		if (cancelSameOrUnusedTask(imageView, capture.getCacheKey())) {
			return;
		}

		final Resources res = imageView.getResources();
		final Bitmap drawable;
		if(resId != 0){
			drawable = BitmapFactory.decodeResource(res, resId);
		}
		else{
			drawable = null;
		}
		

		BitmapFetchTask task = new BitmapFetchTask(imageView,capture.getCacheKey());
		PandaBitmap image = new PandaBitmap(res, drawable, task);
		imageView.setImageDrawable(image);		

		task.excute(capture);
	}

	/**
	 * 
	 * @param view
	 * @return 相同true,过时任务false.
	 */
	private static boolean cancelSameOrUnusedTask(ImageView view, Object data) {
		Drawable map = view.getDrawable();
		if (map instanceof PandaBitmap) {
			PandaBitmap bitmap = (PandaBitmap) map;
			BitmapFetchTask task = bitmap.getTask();
			if (task != null) {
				if (task.DATA.equals(data)) {
					return true;
				}
				task.cancel(false);
			}
		}
		return false;
	}
}
