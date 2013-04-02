package com.crazyapk.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.crazyapk.util.CSQLiteOpenHelper;

public class CrazyDataBaseHelper extends CSQLiteOpenHelper {
	private static final String DB_NAME = "DATA";
	private static final int VERSION = 2;

	public CrazyDataBaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == newVersion){
			return;
		}
		createDatabase(db);
	}
	
	private void createDatabase(SQLiteDatabase db){
		CacheDataSqlHelper.create(db);
		DownloadSqliteHelper.create(db);
	}
}
