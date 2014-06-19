package com.example.gridsample4;

import java.util.ArrayList;
import java.util.List;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.GridLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

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



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttons = new ArrayList<ImageButton>();

		MyOpenHelper helper = new MyOpenHelper(this);
		db = helper.getReadableDatabase();

		//正解画像透明
		findViewById(R.id.ImageView1).setVisibility(ImageView.INVISIBLE);

		feedout = new AlphaAnimation( 1, 0 );
		feedout.setDuration(1000);

		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		width = size.x/3;
	}
	protected void onStart(){
		super.onStart();
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
			if(i==8){
				//正解ボタン設定
				right_button_id = 0x7f090001+list[i];
				se = MediaPlayer.create(getBaseContext(), 0x7f040000 + c.getInt(0) -1 );
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c.getInt(0));
			}else{
				//まわりのボタン設定
				bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c2.getInt(0));
				c2.moveToNext();
			}
			//bitmap = BitmapFactory.decodeResource(res, 0x7f020001 + c2.getInt(0));

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
		TextView message = (TextView)this.findViewById(R.id.textView1);
		message.setText(right_kanji + " は？");

		message = (TextView)this.findViewById(R.id.textView4);
		message.setText(right_kana + "は？");

		message = (TextView)this.findViewById(R.id.textView2);
		message.setText("id:"+ right_id + "   "+"name:" + right_kanji);

		message = (TextView)this.findViewById(R.id.textView3);
		message.setText(list[0] + "  "+  list[1] + "  " + list[2] + "  " + list[3] + "  " +list[4] + "  "  + list[5] + "  " + list[6] + "  " + list[7] + "  " + list[8]);

		Button soundbutton = (Button) findViewById(R.id.soundbutton);
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

			img.setImageResource(R.drawable.circle);
			img.startAnimation( feedout );
		}
		else if(v.getId() == 0x7f090011){
			se.start();
		}
		//不正解
		else{
			updateValues.put("miss",num_miss + 1 );
			for(int i = 0;i<8;i++){
				ImageButton button = buttons.get(i);
				button.startAnimation(feedout);
			}
		}
		db.update("TableTest", updateValues, "kanji=?", new String[]{right_kanji});
		buttons.clear();
		//フェードアウト分の時間待ち
		new Handler().postDelayed(new Runnable() {
			public void run() {
				onStart();
			}
		}, 1000);
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


	/*protected void onResume(){
		super.onResume();
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
