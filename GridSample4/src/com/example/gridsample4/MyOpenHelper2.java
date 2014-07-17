package com.example.gridsample4;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper2 extends SQLiteOpenHelper {

	public MyOpenHelper2(Context context) {
		super(context, "recordDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "";
        sql += "create table recordTable (";
        sql += " date text not null";
        sql += ",correct text";
        sql += ",miss  text";
        sql += ",name";
        sql += ")";
        db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
