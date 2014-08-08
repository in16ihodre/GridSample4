package com.example.gridsample4;


import java.util.ArrayList;


import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowDataBase extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_database);

		Log.v("MyTest", "ShowDataBase onCreate is called");

		MyOpenHelper2 helper = new MyOpenHelper2(this);
		SQLiteDatabase db = helper.getReadableDatabase();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		ListView listView = (ListView) findViewById(R.id.listview);
		//Log.d("MyTest", alist.get(0));

		// queryメソッドの実行例
		Cursor c = db.query("recordTable", new String[] { "date", "correct", "miss" ,"name"}, null,null, null, null, null);

		boolean mov = c.moveToFirst();
		while (mov) {
			adapter.add(String.format("%s \n\t\t %s : %s ： %s", c.getString(0),c.getString(1),c.getString(2),c.getString(3)));
			mov = c.moveToNext();
		}
		c.close();

		// アダプターを設定します
		listView.setAdapter(adapter);
		db.close();

	}
}
