package com.example.gridsample4;

import java.util.ArrayList;
import java.util.Collections;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private static long mSEC = 2500;
	private static final int TrialTime = 300 * 1000;
	private  int right_button_id = 0;

	int[] list = {1, 2, 3, 4, 5, 6, 7, 8, 9};

	private List<ImageButton> buttons;
	private List<TextView> kanji_TextView;
	private List<TextView> kana_TextView;

	private List<Integer> correct_id;
	private List<String> correct_kanji;
	private List<String> correct_kana;
	private List<Integer> ok;
	private List<Integer> miss;

	private List<MediaPlayer> se;

	private SQLiteDatabase db;
	private AlphaAnimation feedout;
	private int num_ok;
	private int num_miss;
	private int width;
	private int x;
	private int y;
	private Bitmap bitmap;
	private SQLiteDatabase db2;
	private Time time;
	private String[] kanji = new String[10];

	private String select_strings;
	protected boolean confirm_dialog;
	private boolean count = true;
	private Builder field_alertDlg;
	protected boolean reach_time;
	private Builder field_alertDlg2;
	protected String selected_hint = "音声のみ";

	private Button soundbutton;

	private Builder field_adduserDlg;
	private Builder field_selectuserDlg;
	private Builder field_correctnumDlg;

	protected String user_name = null;
	String[] item_list;
	private MediaPlayer seikaisound;
	private MediaPlayer syutudaisound;

	private int invisible = View.INVISIBLE;
	private int visible = View.VISIBLE;
	private int hint_kana = 0;
	private int hint_kanji = 0;

	private int runnable_count = 0;

	private long change_SEC = 1000;

	private Integer correct_num = 4;

	private Integer[] textview_id = new Integer[8];

	private boolean oto_hint = true;

	/*
	private final Handler hint_handler = new Handler();
	private final Runnable invisibleHint = new Runnable() {
		@Override
		public void run() {
			hint_message.setVisibility(View.INVISIBLE);
			hint_message2.setVisibility(View.INVISIBLE);
		}
	};
	 */

	private final Handler showhint_handler = new Handler();
	private final Runnable showHint = new Runnable() {

		@Override
		public void run() {
			if(runnable_count == correct_num-1 ){
				kana_TextView.get(correct_num-1).setVisibility(invisible);
				kanji_TextView.get(correct_num-1).setVisibility(invisible);
				soundbutton.setEnabled(true);
				runnable_count = 0;
				Log.v("MyTest", runnable_count+"");
			}
			else{
				if(oto_hint){
					se.get(runnable_count+1).start();
				}else{
				}

				kanji_TextView.get(runnable_count).setVisibility(invisible);
				kanji_TextView.get(runnable_count+1).setVisibility(hint_kanji);

				kana_TextView.get(runnable_count).setVisibility(invisible);
				kana_TextView.get(runnable_count+1).setVisibility(hint_kana);
				Log.v("MyTest", runnable_count+"");
				++runnable_count;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		kanji_TextView = new ArrayList<TextView>();
		kana_TextView = new ArrayList<TextView>();
		buttons = new ArrayList<ImageButton>();
		//データベースから正解パネルを入れとくリスト
		correct_id = new ArrayList<Integer>();
		correct_kana = new ArrayList<String>();
		correct_kanji = new ArrayList<String>();
		ok = new ArrayList<Integer>();
		miss = new ArrayList<Integer>();
		se = new ArrayList<MediaPlayer>();
		//

		seikaisound = MediaPlayer.create(getBaseContext(),R.raw.sou0_seikai2);
		syutudaisound = MediaPlayer.create(getBaseContext(),R.raw.sou0_syutudai1);

		//絵データベース
		MyOpenHelper helper = new MyOpenHelper(this);
		db = helper.getReadableDatabase();

		//成績データベース
		MyOpenHelper2 helper2 = new MyOpenHelper2(this);
		db2 = helper2.getReadableDatabase();


		generate_continue_dialog();

		generate_hintselect_dialog();

		generate_userselect_dialog();

		generate_adduser_dialog();

		generate_correctnum_dialog();


		Cursor c = db2 .rawQuery("Select * from userList", null);
		boolean mov = c.moveToFirst();
		item_list = new String[c.getCount()];

		for(int i=0;i<c.getCount();i++){
			item_list[i] = c.getString(0);
			Log.v("MyTest", c.getString(0));
			mov = c.moveToNext();
		}
		c.close();

		field_selectuserDlg.setSingleChoiceItems(item_list, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//リストを選択した時の処理
				user_name = item_list[whichButton];
				Log.v("MyTest", "select = " +user_name);
			}
		});
		//エラー
		user_name = item_list[0];

		field_selectuserDlg.create().show();

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
			width = (int) ((size.y-(size.y*0.13))/3);
			break;
		default :
		}
		textview_id[0] = R.id.textView1;
		textview_id[1] = R.id.textView2;
		textview_id[2] = R.id.textView3;
		textview_id[3] = R.id.textView4;
		textview_id[4] = R.id.textView5;
		textview_id[5] = R.id.textView6;
		textview_id[6] = R.id.textView7;
		textview_id[7] = R.id.textView8;
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
		correct_id.clear();
		correct_kanji.clear();
		correct_kana.clear();
		se.clear();
		shuffle(list);

		String sql = "";
		sql += "Select * from TableTest order by random() limit ";
		sql += correct_num +";";
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();

		for(int j=0;j<c.getCount();j++){
			correct_id.add(c.getInt(0));
			correct_kanji.add(c.getString(3));
			correct_kana.add(c.getString(4));
			ok.add(c.getInt(5));
			miss.add(c.getInt(6));
			c.moveToNext();
		}

		int a1 = 0;
		int b1 = 0;
		int c1 = 0;
		if(correct_num == 1){
			a1 = 0;
			b1 = 0;
			c1 = 0;
		}else if(correct_num == 2){
			a1 = 1;
			b1 = 0;
			c1 = 0;
		}else if(correct_num == 3){
			a1 = 1;
			b1 = 2;
			c1 = 0;
		}else if(correct_num == 4){
			a1 = 1;
			b1 = 2;
			c1 = 3;
		}

		String sql2 = "";
		sql2 += "Select * from TableTest where kanji!= ? and kanji!=? and kanji!= ? and kanji!=? order by random() limit ";
		sql2 += 9-correct_num + ";";
		Cursor c2 = db.rawQuery(sql2, new String[]{correct_kanji.get(0),correct_kanji.get(a1),correct_kanji.get(b1),correct_kanji.get(c1)});
		c2.moveToFirst();

		Resources res = getResources();
		for(int i=0;i<=8;i++){
			Log.v("MyTest", i +"");
			if(i>=c2.getCount()){
				//正解ボタンの画像設定
				right_button_id = 0x7f090001+list[i];
				se.add(MediaPlayer.create(getBaseContext(), 0x7f040000 + correct_id.get(9-i-1) +1));
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + correct_id.get(i-(9-correct_num)));
			}else{
				//まわりのボタンの画像設定
				int num = list[i];
				kanji[num] =c2.getString(3);
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c2.getInt(0));
			}
			c2.moveToNext();

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
		Collections.reverse(se);

		for(int i = 0;i<c.getCount();i++){
			TextView hint_message1 = (TextView)this.findViewById(textview_id[i*2]);
			hint_message1.setText(correct_kanji.get(i) + " ");
			hint_message1.setTextSize(width/3);
			hint_message1.setVisibility(View.INVISIBLE);

			TextView hint_message2 = (TextView)this.findViewById(textview_id[i*2+1]);
			hint_message2.setText(correct_kana.get(i) + " ");
			hint_message2.setTextSize(width/3);
			hint_message2.setVisibility(View.INVISIBLE);

			kanji_TextView.add(hint_message1);
			kana_TextView.add(hint_message2);
		}

		Log.v("MyTest", list[0] + "  "+  list[1] + "  " + list[2] + "  " + list[3] + "  " +list[4] + "  "  + list[5] + "  " + list[6] + "  " + list[7] + "  " + list[8]);
		for(int i = 0;i<correct_kanji.size();i++){
			Log.v("MyTest", correct_kanji.get(i));
		}

		soundbutton = (Button) findViewById(R.id.soundbutton);
		//soundbutton.setBackgroundColor(0xffffffff);
		soundbutton.setTextSize(width/3);
		soundbutton.setOnClickListener(new OnClickListener() {
			//ヒントボタンが押されたら
			@Override
			public void onClick(View v) {
				soundbutton.setEnabled(false);
				show_hint();
			}
		});
		//ボタン有効化
		allbuttonEnable(true);
		soundbutton.setEnabled(true);
		for(int i = 0;i<=8;i++){
			buttons.get(i).setVisibility(ImageView.VISIBLE);
		}
	}
	//ボタンが押されたら
	@Override
	public void onClick(View v) {
		//ボタン無効化
		allbuttonEnable(false);
		soundbutton.setEnabled(false);
		ContentValues updateValues = new ContentValues();
		//正解
		if(v.getId() ==right_button_id ){
			seikaisound.start();
			ImageView img = (ImageView)findViewById(R.id.ImageView1);

			updateValues.put("ok",num_ok + 1 );

			select_strings = correct_kanji.get(0);
			img.setImageResource(R.drawable.circle);
			//img.setScaleType(ImageView.ScaleType.CENTER_CROP );

			img.setScaleX(3);
			img.setScaleY(3);
			img.startAnimation( feedout );

		}
		//不正解
		else{
			updateValues.put("miss",num_miss + 1 );
			for(int i = 0;i<8;i++){
				ImageButton button = buttons.get(i);
				button.startAnimation(feedout);
				buttons.get(i).setVisibility(ImageView.INVISIBLE);
				select_strings = kanji[(v.getId()-0x7f090001)];
				mSEC = 4500;
			}
		}
		Log.d("MyTest","user = " + user_name);
		db.update(user_name, updateValues, "kanji=?", new String[]{correct_kanji.get(0)});

		ContentValues insertValues = new ContentValues();
		time.setToNow();
		String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日" +time.hour + "時" + time.minute + "分";
		insertValues.put("date",(String)date);
		insertValues.put("correct", (String)correct_kanji.get(0));
		insertValues.put("miss", (String)select_strings);
		insertValues.put("name", user_name);
		db2.insert("recordTable", date, insertValues);

		//フェードアウト分の時間待ち
		new Handler().postDelayed(new Runnable() {
			public void run() {
				mSEC = 2500;
				//syutudaisound.start();
				onStart();
			}
		}, mSEC);//ボタンを押してから、次のセッションへ移るまでの時間
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
				//⇒アイテムを選択した時のイベント処理
				//Toast.makeText(MainActivity.this,item_list[whichButton] + "⇒アイテムを選択した時のイベント処理",Toast.LENGTH_SHORT).show();
			}
		});
		alertDlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				//show_hint();
				//⇒OKボタンを押下した時のイベント処理
				//【NOTE】
				//whichButtonには選択したアイテムのインデックスが入っているわけでは
				//ないので注意
				//hint_message.setVisibility(View.INVISIBLE);
				//hint_message2.setVisibility(View.INVISIBLE);
				for(int i = 0;i<correct_num;i++){
					kanji_TextView.get(i).setVisibility(View.INVISIBLE);
					kana_TextView.get(i).setVisibility(View.INVISIBLE);
				}
			}
		});
		field_alertDlg2 = alertDlg;
	}

	private void generate_adduser_dialog() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View view = inflater.inflate(R.layout.dialog5, null);
		final EditText editText = (EditText)view.findViewById(R.id.editText1);
		final AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle("ユーザ名を入力してください");
		alertDlg.setView(view);
		alertDlg.setPositiveButton(
				"OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						user_name = editText.getText().toString();
						Log.d("MyTest","input text = " + user_name);
						ContentValues insertValues = new ContentValues();
						insertValues.put("name",user_name);
						db2.insert("userList", null, insertValues);
						//db.close();
						//field_selectuserDlg.create().show();
						String sql = "";
						sql += "create table ";
						sql += user_name;
						sql += " as select * from";
						sql += " TableTest";
						db.execSQL(sql);
						//db.close();
						//field_selectuserDlg.create().show();
					}
				});
		field_adduserDlg = alertDlg;
	}

	private void generate_userselect_dialog() {
		final AlertDialog.Builder alertDlg2 = new AlertDialog.Builder(this);
		alertDlg2.setTitle("ユーザを選んでください");
		alertDlg2.setNeutralButton("追加", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				field_adduserDlg.create().show();
			}
		});
		alertDlg2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				ShowDataBase.send_username(user_name);
				//
				//user_name = item_list[whichButton];
				//Log.v("MyTest", user_name);
			}
		});
		field_selectuserDlg = alertDlg2;
	}

	private void generate_correctnum_dialog() {
		final String item_list[] = new String[] {
				"1",
				"2",
				"3",
				"4"
		};

		final AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle("正解数を選んでください");
		alertDlg.setSingleChoiceItems(item_list, 0, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				correct_num = Integer.valueOf(item_list[whichButton]);
				//⇒アイテムを選択した時のイベント処理
				//Toast.makeText(MainActivity.this,item_list[whichButton] + "⇒アイテムを選択した時のイベント処理",Toast.LENGTH_SHORT).show()
				Log.v("MyTest", ""+Integer.valueOf(item_list[whichButton]));
			}
		});
		alertDlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				onStart();
			}
		});
		field_correctnumDlg = alertDlg;
	}

	private void show_hint() {
		if(selected_hint.equals("音声のみ")){
			hint_kana = invisible;
			hint_kanji = invisible;
			oto_hint = true;

		}else if(selected_hint.equals("仮名のみ")){
			hint_kana = visible;
			hint_kanji = invisible;
			oto_hint = false;

		}else if(selected_hint.equals("漢字のみ")){
			hint_kana = invisible;
			hint_kanji = visible;
			oto_hint = false;

		}else if(selected_hint.equals("音声と仮名")){
			hint_kana = visible;
			hint_kanji = invisible;
			oto_hint = true;

		}else if(selected_hint.equals("音声と漢字")){
			hint_kana = invisible;
			hint_kanji = visible;
			oto_hint = true;

		}else if(selected_hint.equals("音声と仮名と漢字")){
			hint_kana = visible;
			hint_kanji = visible;
			oto_hint = true;
		}
		kana_TextView.get(0).setVisibility(hint_kana);
		kanji_TextView.get(0).setVisibility(hint_kanji);

		if(oto_hint){
			se.get(0).start();
		}else{
		}
		for(int i = 1;i<=correct_num;i++){
			showhint_handler.postDelayed(showHint, change_SEC*i);
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
		menu.add("ヒント選択");
		menu.add("正解数選択");
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
		}else if(item.getTitle().equals("ヒント選択")){
			selected_hint = "音声のみ";
			field_alertDlg2.create().show();
		}else if(item.getTitle().equals("正解数選択")){
			correct_num = 1;
			field_correctnumDlg.create().show();
		}
		return true;
	}

}
