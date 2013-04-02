package com.crazyapk.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.crazyapk.android.db.CacheDataSqlHelper;
import com.crazyapk.android.db.CrazyDataBaseHelper;

public class CacheDataUtil {
	public static void put(Context ctx, String key, String value) {
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(ctx);
		SQLiteDatabase db = null;

		try {
			db = helper.getWritableDatabase();
			
			if(CacheDataSqlHelper.update(db,key,value,null) > 0)
				CacheDataSqlHelper.insert(db, key, value, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				db.close();
			} catch (Exception e) {
			}
		}
	}

	public static boolean contain(Context ctx, String key, String value) {
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(ctx);

		boolean re = false;
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = helper.getReadableDatabase();
			c = CacheDataSqlHelper.searchByValue(db, key, value);

			if (c.moveToNext()) {
				re = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				c.close();
			} catch (Exception e) {
			}

			try {
				db.close();
			} catch (Exception e) {
			}
		}
		return re;
	}

	public static boolean pop(Context ctx, String key, String value) {
		if (contain(ctx, key, value)) {
			delete(ctx, key, value);
			return true;
		}
		return false;
	}

	private static void delete(Context ctx, String key, String value) {
		CrazyDataBaseHelper helper = new CrazyDataBaseHelper(ctx);
		SQLiteDatabase db = null;
		try {
			db = helper.getReadableDatabase();
			CacheDataSqlHelper.delete(db, key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				db.close();
			} catch (Exception e) {
			}
		}
	}
}
