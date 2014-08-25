package com.example.gridsample4;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyOpenHelper2 extends SQLiteOpenHelper {

	public MyOpenHelper2(Context context) {
		super(context, "recordDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v("MyTest", "onCreate of MyOpenHelper is called");
		String sql = "";
        sql += "create table recordTable (";
        sql += " date text not null";
        sql += ",correct text";
        sql += ",miss  text";
        sql += ",name";
        sql += ")";
        db.execSQL(sql);

        String sql2 = "";
        sql2 += " create table userList (";
        sql2 += " name";
        sql2 += ")";
        db.execSQL(sql2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("MyTest", "onUpgrade of MyOpenHelper is called");

	}

}
