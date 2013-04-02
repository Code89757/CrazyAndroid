package com.crazyapk.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * 数据池，用于缓存数据
 * 
 * @author wen.yugang </br> 2012-7-10
 */
public class CacheDataSqlHelper {

	static final String TABLE_NAME = "cache_data";
	static final String FIELD_SET_NAME = "set_name";
	static final String FIELD_TAG = "tag";
	static final String FIELD_VALUE = "value";

	static void create(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("CREATE TABLE [cache_data] ( " + " [set_name] TEXT,  " + " [value] TEXT,  "
				+ " [tag] TEXT);");
	}

	public static Cursor searchByValue(SQLiteDatabase db, String key, String value) {
		return db.query(TABLE_NAME, null, FIELD_SET_NAME + "= ?," + FIELD_VALUE + "=?",
				new String[]{key, value}, null, null, null);
	}

	public static boolean insert(SQLiteDatabase db, String key, String value, String tag) {
		ContentValues values = new ContentValues();

		values.put(FIELD_SET_NAME, key);
		values.put(FIELD_VALUE, value);

		if (!TextUtils.isEmpty(tag))
			values.put(FIELD_TAG, tag);

		return db.insert(TABLE_NAME, null, values) == -1 ? false : true;
	}

	public static boolean delete(SQLiteDatabase db, String key, String value) {
		return db.delete(TABLE_NAME, FIELD_SET_NAME + "= ?," + FIELD_VALUE + "=?", new String[]{
				key, value}) == 0 ? false : true;
	}

	public static int update(SQLiteDatabase db, String key, String value, String tag) {
		ContentValues values = new ContentValues();

		values.put(FIELD_SET_NAME, key);
		values.put(FIELD_VALUE, value);
		if (!TextUtils.isEmpty(tag))
			values.put(FIELD_TAG, tag);
		
		return db.update(TABLE_NAME, values, FIELD_SET_NAME + "= ? AND " + FIELD_VALUE + "=?", new String[]{
				key, value});
	}


}
