package com.example.gridsample4;

import java.util.ArrayList;
import java.util.List;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.GridLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private static final long mSEC = 2500;
	private static final int TrialTime = 10 * 1000;
	private  int right_button_id = 0;
	private String right_kanji;
	private String right_kana;
	private int right_id;

	int[] list = {1, 2, 3, 4, 5, 6, 7, 8, 9};

	private List<ImageButton> buttons;
	private SQLiteDatabase db;
	private AlphaAnimation feedout;
	private int num_ok;
	private int num_miss;
	private int width;
	private int x;
	private int y;
	private Bitmap bitmap;
	private MediaPlayer se;
	private SQLiteDatabase db2;
	private Time time;
	private String[] kanji = new String[10];

	private String miss_strings;
	protected boolean confirm_dialog;
	private boolean count = true;
	private Builder field_alertDlg;
	protected boolean reach_time;
	private Builder field_alertDlg2;
	protected String selected_hint = "音声のみ";

	private int oto		= 0xff000000;
	private int kana		= 0x00000000;
	private int kanji2	= 0x00000000;
	private boolean oto_button = true;

	private TextView hint_message;
	private TextView hint_message2;
	private Button soundbutton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.v("MyTest", "onCreate is called");

		generate_continue_dialog();

		generate_hintselect_dialog();

		field_alertDlg2.create().show();

		buttons = new ArrayList<ImageButton>();

		//絵データベース
		MyOpenHelper helper = new MyOpenHelper(this);
		db = helper.getReadableDatabase();

		//成績データベース
		MyOpenHelper2 helper2 = new MyOpenHelper2(this);
		db2 = helper2.getReadableDatabase();

		time = new Time("Asia/Tokyo");

		//正解画像透明
		findViewById(R.id.ImageView1).setVisibility(ImageView.INVISIBLE);

		feedout = new AlphaAnimation( 1, 0 );
		feedout.setDuration(mSEC);

		Resources resources = getResources();
		Configuration config = resources.getConfiguration();

		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);

		switch(config.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			width = size.x/3;
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			width = (int) ((size.y-(size.y*0.2))/3);
			break;
		default :
		}

		Log.v("MyTest", String.valueOf(size.y));

	}


	protected void onStart(){
		super.onStart();

		Log.v("MyTest", "onStart is called");

		if(count){
			new Handler().postDelayed(new Runnable() {
				public void run() {
					reach_time = true;
				}
			}, TrialTime );
			count = false;
		}
		if(reach_time){
			field_alertDlg.create().show();
		}

		buttons.clear();
		shuffle(list);

		Cursor c = db.rawQuery("Select * from TableTest order by random() limit 1;", null);
		c.moveToFirst();
		Cursor c2 = db.rawQuery("Select * from TableTest where kanji <> ? order by random() limit 8;", new String[]{c.getString(3)});
		c2.moveToFirst();

		right_id = c.getInt(0);
		right_kanji = c.getString(3);
		right_kana = c.getString(4);
		num_ok = c.getInt(5);
		num_miss = c.getInt(6);

		Resources res = getResources();
		for(int i=0;i<=c2.getCount();i++){
			if(i==c2.getCount()){
				//正解ボタンの画像設定
				right_button_id = 0x7f090001+list[i];
				se = MediaPlayer.create(getBaseContext(), 0x7f040000 + c.getInt(0) -1 );
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c.getInt(0));
			}else{
				//まわりのボタンの画像設定
				int num = list[i];
				kanji[num] =c2.getString(3);
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c2.getInt(0));
				c2.moveToNext();
			}

			ImageButton Button = (ImageButton) findViewById(0x7f090001 + list[i]);
			Button.setOnClickListener(this);
			GridLayout.LayoutParams params1 = new GridLayout.LayoutParams();
			params1.width = width;
			params1.height = width;
			ColumnRowNum(list[i]);
			params1.columnSpec = GridLayout.spec(x);
			params1.rowSpec = GridLayout.spec(y);

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width-10, width-10, false);
			Button.setImageBitmap(bitmap2);
			Button.setLayoutParams(params1);
			buttons.add(Button);
		}

		//ボタン有効化
		allbuttonEnable(true);

		hint_message = (TextView)this.findViewById(R.id.textView1);
		hint_message.setText(right_kanji + " は？");

		hint_message2 = (TextView)this.findViewById(R.id.textView4);
		hint_message2.setText(right_kana + "は？");

		TextView message = (TextView)this.findViewById(R.id.textView2);
		message.setText("id:"+ right_id + "   "+"name:" + right_kanji);

		message = (TextView)this.findViewById(R.id.textView3);
		message.setText(list[0] + "  "+  list[1] + "  " + list[2] + "  " + list[3] + "  " +list[4] + "  "  + list[5] + "  " + list[6] + "  " + list[7] + "  " + list[8]);

		//message = (TextView)this.findViewById(R.id.textView4);
		//message.setText(kanji[1] + "  " + kanji[2] + "  " + kanji[3]);

		soundbutton = (Button) findViewById(R.id.soundbutton);
		soundbutton.setBackgroundColor(0xffffffff);
		soundbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				se.start();
			}
		});
	}

	//ボタンが押されたら
	@Override
	public void onClick(View v) {
		//ボタン無効化
		allbuttonEnable(false);
		ContentValues updateValues = new ContentValues();
		//正解
		if(v.getId() ==right_button_id ){
			//Toast.makeText(MainActivity.this, "正解！", Toast.LENGTH_SHORT).show();
			ImageView img = (ImageView)findViewById(R.id.ImageView1);

			updateValues.put("ok",num_ok + 1 );

			miss_strings = right_kanji;
			img.setImageResource(R.drawable.circle);
			img.startAnimation( feedout );
		}
		//不正解
		else{
			updateValues.put("miss",num_miss + 1 );
			for(int i = 0;i<8;i++){
				ImageButton button = buttons.get(i);
				button.startAnimation(feedout);

				miss_strings = kanji[(v.getId()-0x7f090001)];
			}
		}
		db.update("TableTest", updateValues, "kanji=?", new String[]{right_kanji});
		//buttons.clear();

		ContentValues insertValues = new ContentValues();
		time.setToNow();
		String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日" +time.hour + "時" + time.minute + "分";
		insertValues.put("date",(String)date);
		insertValues.put("correct", (String)right_kanji);
		insertValues.put("miss", (String)miss_strings);
		db2.insert("recordTable", date, insertValues);

		//フェードアウト分の時間待ち
		new Handler().postDelayed(new Runnable() {
			public void run() {
				onStart();
			}
		}, mSEC);
	}

	private void shuffle(int[] arr) {
		for(int i=arr.length-1; i>0; i--){
			int t = (int)(Math.random() * i);  //0～i-1の中から適当に選ぶ
			//選ばれた値と交換する
			int tmp = arr[i];
			arr[i]  = arr[t];
			arr[t]  = tmp;
		}
	}

	private void allbuttonEnable(boolean b) {
		for(int i = 0;i<buttons.size();i++){
			ImageButton button = buttons.get(i);
			button.setEnabled(b);
		}
	}

	private void ColumnRowNum(int i) {
		if(i==1){
			x = 0;
			y = 0;
		}else if(i==2){
			x = 1;
			y = 0;
		}else if(i==3){
			x = 2;
			y = 0;
		}else if(i==4){
			x = 0;
			y = 1;
		}else if(i==5){
			x = 1;
			y = 1;
		}else if(i==6){
			x = 2;
			y = 1;
		}else if(i==7){
			x = 0;
			y = 2;
		}else if(i==8){

			x = 1;
			y = 2;
		}else if(i==9){
			x = 2;
			y = 2;
		}
	}

	private void generate_continue_dialog() {
		// 確認ダイアログの生成
		final AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle(TrialTime/60000 + "分経過しました！");
		alertDlg.setMessage("まだつづけますか？");
		alertDlg.setPositiveButton(
				"はい",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						count = true;
						reach_time = false;
					}
				});
		alertDlg.setNegativeButton(
				"いいえ",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

		field_alertDlg = alertDlg;

	}

	private void generate_hintselect_dialog() {
		final String item_list[] = new String[] {
				"音声のみ",
				"仮名のみ",
				"漢字のみ",
				"音声と仮名",
				"音声と漢字",
				"音声と仮名と漢字"
		};

		final AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle("ヒントの種類を選んでください");
		alertDlg.setSingleChoiceItems(item_list, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				selected_hint = item_list[whichButton];
				show_hint();
				//⇒アイテムを選択した時のイベント処理
				//Toast.makeText(MainActivity.this,item_list[whichButton] + "⇒アイテムを選択した時のイベント処理",Toast.LENGTH_SHORT).show();
			}
		});
		alertDlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				//⇒OKボタンを押下した時のイベント処理
				//【NOTE】
				//whichButtonには選択したアイテムのインデックスが入っているわけでは
				//ないので注意
				//Toast.makeText(MainActivity.this,"OKボタンを押下しました。" + Integer.toString(whichButton),Toast.LENGTH_SHORT).show();
				hint_message.setTextColor(kanji2);
				hint_message2.setTextColor(kana);
				soundbutton.setTextColor(oto);
				soundbutton.setEnabled(oto_button);
			}
		});
		field_alertDlg2 = alertDlg;
	}

	private void show_hint() {
		if(selected_hint.equals("音声のみ")){
			oto =		0xff000000;
			kana =		0x00000000;
			kanji2 =	0x00000000;
			oto_button = true;
		}else if(selected_hint.equals("仮名のみ")){
			oto = 		0x00000000;
			kana = 		0xff000000;
			kanji2 = 	0x00000000;
			oto_button = false;
		}else if(selected_hint.equals("漢字のみ")){
			oto = 		0x00000000;
			kana = 		0x00000000;
			kanji2 = 	0xff000000;
			oto_button = false;
		}else if(selected_hint.equals("音声と仮名")){
			oto = 		0xff000000;
			kana = 		0xff000000;
			kanji2 = 	0x00000000;
			oto_button = true;
		}else if(selected_hint.equals("音声と漢字")){
			oto = 		0xff000000;
			kana = 		0x00000000;
			kanji2 = 	0xff000000;
			oto_button = true;
		}else if(selected_hint.equals("音声と仮名と漢字")){
			oto = 		0xff000000;
			kana = 		0xff000000;
			kanji2 = 	0xff000000;
			oto_button = true;
		}
	}


	protected void onResume(){
		super.onResume();
		Log.v("MyTest", "onResume is called");
	}

	protected void onPause(){
		super.onPause();
		Log.v("MyTest", "onPause is called");
	}

	protected void onStop(){
		super.onStop();
		Log.v("MyTest", "onStop is called");
	}

	protected void onDestroy(){
		super.onDestroy();
		Log.v("MyTest", "onDestroy is called");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);

		// メニューの要素を追加
		menu.add("結果表示");
		// メニューの要素を追加して取得
		MenuItem actionItem = menu.add("Action Button");

		// SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
		actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		// アイコンを設定
		actionItem.setIcon(android.R.drawable.ic_menu_share);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("結果表示")){
			// インテントのインスタンス生成
			Intent intent = new Intent(this, ShowDataBase.class);
			// 次画面のアクティビティ起動
			startActivity(intent);
			//Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_LONG).show();
		}else{

		}
		return true;
	}

}
