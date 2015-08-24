package com.mokee.mptest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mokee.mptest.MyMediaPlayer.onEndPlayingLisenter;

public class MainActivity extends Activity {
	private static final String tag = "MainActivity";
	
	private TextView tv_GooglePath;
	private Button button;
	private EditText et_MediaPath;
	private MyMediaPlayer myMediaPlayer;
	private String[] videoPaths = new String[]{"video_1_55M.mp4", "video_2_143M.mp4"};
	private int[] position = new int[2];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		initEvent();
	}

	private void initView(){
		tv_GooglePath = (TextView) findViewById(R.id.tv_GooglePath);
		et_MediaPath = (EditText) findViewById(R.id.et_MediaPath);
		myMediaPlayer = (MyMediaPlayer) findViewById(R.id.myMediaPlayer);
		button = (Button) findViewById(R.id.button);
	}
	
	private void initEvent() {
		et_MediaPath.setText(new StorageList(this).getVolumePaths()[1] + "/" + videoPaths[0]);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myMediaPlayer.setMediaDataResource(et_MediaPath.getText().toString().trim());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		position = myMediaPlayer.getMediaProcess();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		myMediaPlayer = (MyMediaPlayer) findViewById(R.id.myMediaPlayer);
		myMediaPlayer.setMediaDataResource(et_MediaPath.getText().toString().trim(), position[0]);
		super.onResume();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		Uri uri = Uri.parse("/storage/ext_sd/test_video.mp4");
		Uri uri = Uri.parse(et_MediaPath.getText().toString().trim());
		switch (item.getItemId()) {
		case R.id.menu_OuterPlayer:
			Log.i("tag", "uri:" + uri.toString());
			Intent videoIntent = new Intent(Intent.ACTION_VIEW);
			videoIntent.setDataAndType(uri, "video/mp4");
			startActivity(videoIntent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
