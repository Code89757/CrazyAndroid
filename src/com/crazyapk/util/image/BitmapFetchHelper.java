package com.crazyapk.util.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.text.TextUtils;

import com.crazyapk.config.SystemConst;
import com.crazyapk.util.FileUtil;

/**
 * Helper for fetching bitmap
 * 
 * @author wen.yugang
 * 
 */
public class BitmapFetchHelper {

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 5000;

	public interface ImageLoaderListener {
		void onFinish(Bitmap bitmap);
	}
	
	/**
	 * 下载图像
	 * 
	 * @param url
	 * @return 并返回图像的保存地址。
	 */
	public static String downloadBitmap(String url) {
		StringBuffer buffer = new StringBuffer(SystemConst.CACHE_IMAGE_DIR);
		buffer.append(ImageCache.getKeyByUrl(url));
		final String pathName = buffer.toString();
		if (downloadBitmap(url, pathName)) {
			return buffer.toString();
		} else {
			return null;
		}
	}

	/**
	 * 下载图像
	 * 
	 * @param url
	 *            图片下载地址
	 * @param filePath
	 *            图片保存地址
	 * @return 是否下载成功
	 */
	public static boolean downloadBitmap(String url, String filePath) {
		return downloadBtimap(url, filePath, false);
	}

	/**
	 * 下载图像，该方法提供了两种方法，一直至今下载到
	 * 
	 * @param url
	 * @param filePath
	 * @param big
	 * @return
	 */
	public static boolean downloadBtimap(String url, String filePath,
			boolean big) {
		boolean success = false;

		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath))
			return success;

		url = url.replaceAll("\\\\", "/");

		File file = FileUtil.createFile(filePath);
		if (file.exists()) {
			file.delete();
		}

		disableConnectionReuseIfNecessary();
		HttpURLConnection conn = null;

		try {
			conn =(HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);

			InputStream netin = conn.getInputStream();
			OutputStream fos = new FileOutputStream(file);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			int len = -1;
			final int BUFFER_SIZE = 1024;
			byte[] bs = new byte[BUFFER_SIZE];

			if (big) {
				while ((len = netin.read(bs)) != -1) {
					fos.write(bs, 0, len);
				}
			} else {
				while ((len = netin.read(bs)) != -1) {
					bos.write(bs, 0, len);
				}
				fos.write(bos.toByteArray());
				fos.close();
				fos.flush();
			}

			bs = null;
			bos.close();
			bos.flush();

			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}

		return success;
	}

	private static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < 2.2) {
			System.setProperty("http.keepAlive", "false");
		}
	}


	/**
	 * 
	 * @Title: downloadBitmap
	 * @Description: 图片下载完 进行回调
	 * @param url
	 * @param listener
	 * @throws
	 */
	public static void downloadBitmap(String url, ImageLoaderListener listener) {
		if (listener != null) {
			Bitmap bit = getBitmap(url);
			listener.onFinish(bit);
		}
	}

	/**
	 * 下载图像
	 * 
	 * @param url
	 *            图像的URL
	 * @return
	 */
	public static Bitmap getBitmap(String url) {
		// 验证参数
		if (TextUtils.isEmpty(url)) {
			return null;
		}

		// 下载图像
		final String imagePath = downloadBitmap(url);
		if (TextUtils.isEmpty(imagePath)) {
			return null;
		}

		return getBitmapFromDisk(imagePath);
	}

	/**
	 * 下载图像
	 * 
	 * @param url
	 *            图像的URL
	 * @param outWidth
	 *            要输出的预期宽度
	 * @param outHeight
	 *            要输出的预期高度
	 * @return 并根据所给的输出参数，直接返回
	 */
	public static Bitmap downloadBitmap(String url, float outWidth,
			float outHeight) {
		// 验证参数
		if (TextUtils.isEmpty(url) || outWidth <= 0 || outHeight <= 0) {
			return null;
		}

		// 下载图像
		final String imagePath = downloadBitmap(url);
		if (TextUtils.isEmpty(imagePath)) {
			return null;
		}

		return getBitmapFromDisk(imagePath, outWidth, outHeight);
	}

	/**
	 * 从磁盘中获取图像
	 */
	public static Bitmap getBitmapFromDisk(String imagePath, Options opts) {
		try {
			return BitmapFactory.decodeFile(imagePath, opts);
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
			return null;
		} catch (Exception e) {
			new File(imagePath).delete();
			return null;
		}
	}

	/**
	 * 从磁盘中获取图像
	 */
	public static Bitmap getBitmapFromDisk(String imagePath) {
		return getBitmapFromDisk(imagePath, 0, 0);
	}

	/**
	 * 从磁盘中获取图像
	 * 
	 * @param imagePath
	 * @param outWidth
	 * @param outHeight
	 * @return
	 */
	public static Bitmap getBitmapFromDisk(String imagePath, float outWidth,
			float outHeight) {
		// 验证参数
		if (TextUtils.isEmpty(imagePath)) {
			return null;
		}

		try {
			if (outWidth <= 0 || outHeight <= 0) {
				return BitmapFactory.decodeFile(imagePath);
			}
			return BitmapFactory.decodeFile(imagePath,
					getOptions(imagePath, outWidth, outHeight));
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
			return null;
		} catch (Exception e) {
			new File(imagePath).delete();
			return null;
		}
	}

	public static Bitmap getBitmapFromUrl(String url) {
		String imagePath = downloadBitmap(url);
		if (TextUtils.isEmpty(imagePath)) {
			return null;
		}
		return getBitmapFromDisk(imagePath, 0, 0);
	}

	/**
	 * 
	 * @param imageFile
	 * @param outWidth
	 *            单位是dp
	 * @param outHeight
	 *            单位是dp
	 * @return
	 */
	public static BitmapFactory.Options getOptions(String imageFile,
			float outWidth, float outHeight) {
		// 获取图像的大小

		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFile, bitmapFactoryOptions);

		double yRatio = (double) (bitmapFactoryOptions.outHeight / outHeight);
		double xRatio = (double) (bitmapFactoryOptions.outWidth / outWidth);

		if (yRatio > 1 || xRatio > 1) {
			if (yRatio > xRatio) {
				bitmapFactoryOptions.inSampleSize = (int) Math.round(yRatio);
			} else {
				bitmapFactoryOptions.inSampleSize = (int) Math.round(xRatio);
			}
		} else {
			bitmapFactoryOptions.inSampleSize = 1;
		}
		if (outHeight == 1) {
			bitmapFactoryOptions.inSampleSize = (int) Math.round(xRatio);
		}
		if (outWidth == 1) {
			bitmapFactoryOptions.inSampleSize = (int) Math.round(yRatio);
		}

		bitmapFactoryOptions.inJustDecodeBounds = false;

		return bitmapFactoryOptions;
	}

	/**
	 * 根据Url获取对应的唯一键值
	 * 
	 * @param url
	 * @return 如果url为空，只返回的数据为null.
	 */
	public static String getKeyByUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			return String.valueOf(url.replaceAll("[.:/,%?&=]", "+")
					.replaceAll("[+]+", "+").hashCode());			
		}
		return null;
	}
}
