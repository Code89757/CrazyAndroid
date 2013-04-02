package com.crazyapk.android.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.crazyapk.download.DownloadTask;
import com.crazyapk.util.LogUtil;

public class DownloadSqliteHelper {
	private static final String tag = DownloadSqliteHelper.class
			.getSimpleName();
	private static final String TABLE_NAME = "download_info";

	static void create(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("CREATE TABLE [download_info] ("
				+ "[package_name] TEXT NOT NULL ON CONFLICT REPLACE, "
				+ "[name] TEXT, " + "[url] TEXT, "
				+ "[version_code] INTEGER DEFAULT (0), "
				+ "[size] INT64 DEFAULT (0));");
	}

	public static ArrayList<DownloadTask> search(Context context) {
		ArrayList<DownloadTask> datas = new ArrayList<DownloadTask>();
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cr = db.query(TABLE_NAME, null, null, null, null, null, null);
		while (cr.moveToNext()) {
			String url = cr.getString(cr.getColumnIndex("url"));
			if (TextUtils.isEmpty(url))
				continue;

			String name = cr.getString(cr.getColumnIndex("name"));

			DownloadTask task = new DownloadTask(context, url, name);
			datas.add(task);
		}

		cr.close();
		db.close();
		return datas;
	}

	public static void query(Context context, String name,DownloadTask task) {
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cr = db.query(TABLE_NAME, null,
				"name = ? ", new String[] {
				name }, null, null, null);
		
		if(cr.getCount() > 1 && cr.moveToFirst()){
			task.PackageName = cr.getString(cr.getColumnIndex("package_name"));
			task.VersionCode = cr.getLong(cr.getColumnIndex("version_code"));
			task.setSize(cr.getLong(cr.getColumnIndex("size")));
		}
	}

	public static boolean put(Context context, DownloadTask task) {
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", task.Name);
		values.put("url", task.DownloadUrl);
		values.put("package_name", task.PackageName);
		values.put("version_code", task.VersionCode);
		values.put("size", task.getState().getValue());
		long re = db.insert(TABLE_NAME, null, values);
		if (re < 0) {
			LogUtil.e(tag, "添加下载任务数据失败：" + task.toString());
			db.update(TABLE_NAME, values, "name = ? ", new String[]{task.Name});
			return false;
		}
		db.close();
		return true;
	}

}
