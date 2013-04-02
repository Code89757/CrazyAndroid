package com.crazyapk.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
	private static String tag = FileUtil.class.getSimpleName();

	/**
	 * 创建文件
	 * 
	 * @param pathName
	 *            路径名
	 * @return void
	 */
	public static void createFile(File file) {
		// File file = new File(pathName);
		if (!file.getParentFile().exists()) {
			System.out.println("文件所在目录不存在，准备创建...");
			if (file.getParentFile().mkdirs()) {
				System.out.println("目录创建成功，准备创建文件...");
				try {
					if (file.createNewFile()) {
						System.out.println(file.getAbsolutePath() + "创建成功！");
						return;
					} else {
						System.out.println("文件创建失败，退出!");
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("目录创建失败，退出!");
				return;
			}
		} else {
			System.out.println("准备创建文件...");
			try {
				if (file.createNewFile()) {
					System.out.println(file.getAbsolutePath() + "创建成功!");
					return;
				} else {
					System.out.println("文件创建失败，退出!");
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void rename(File file, File desFile) {
		if (!file.exists()) {
			LogUtil.e(tag, "文件不存在:" + file.getAbsolutePath());
			return;
		}

		if (desFile.exists()) {
			LogUtil.e(tag, "文件已存在:" + desFile.getAbsolutePath());
			return;
		}

		if (!file.renameTo(desFile)) {
			LogUtil.e(tag, "重命名失败：" + file.getAbsolutePath());
		}
	}

	public static File createFile(String path) {
		File file = new File(path);
		
		// 寻找父目录是否存在
		File parent = new File(file.getAbsolutePath().substring(0,
				file.getAbsolutePath().lastIndexOf(File.separator)));
		// 如果父目录不存在，则递归寻找更上一层目录
		if (!parent.exists()) {
			createFile(parent.getPath());
			// 创建父目录
			parent.mkdirs();
		}
		
		return file;
	}
}
