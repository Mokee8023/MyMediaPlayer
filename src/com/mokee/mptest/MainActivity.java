package com.mokee.mptest;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView tv_GooglePath;
	private EditText et_MediaPath;
	private MyMediaPlayer myMediaPlayer;
	private String[] videoPaths = new String[]{"video_1_55M.mp4", "video_2_143M.mp4"};

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
	}
	
	private void initEvent() {
		// getPath();
		// getAllPath();
		et_MediaPath.setText(new StorageList(this).getVolumePaths()[1] + "/" + videoPaths[0]);
		myMediaPlayer.setMediaDataResource(et_MediaPath.getText().toString().trim());
	}

	private void getAllPath(){
		StorageList sl = new StorageList(this);
		StringBuilder sb = new StringBuilder("Path List:\n");
		String[] paths = sl.getVolumePaths();
		int i = 0;
		for(String path : paths){
			sb.append(String.valueOf(++i)).append(":").append(path).append("\n");
		}
		tv_GooglePath.setText(sb.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

		case R.id.menu_MediaPlayer:
			Intent mediaPlayerIntent = new Intent(this, MyMediaPlayerActivity.class);
			startActivity(mediaPlayerIntent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private void getPath() {
		StringBuilder sb = new StringBuilder("Path List:\n");
		sb.append("Root.AbsolutePath:").append(Environment.getRootDirectory().getAbsolutePath()).append("\n");
		try {
			sb.append("Root.CanonicalPath:").append(Environment.getRootDirectory().getCanonicalPath()).append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb.append("Root.Path:").append(Environment.getRootDirectory().getPath()).append("\n");
		sb.append("External.AbsolutePath:").append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("\n");
		sb.append("External.Path:").append(Environment.getExternalStorageDirectory().getPath()).append("\n");
		sb.append("Data.AbsolutePath:").append(Environment.getDataDirectory().getAbsolutePath()).append("\n");
		sb.append("Data.Path:").append(Environment.getDataDirectory().getPath()).append("\n");
		
		tv_GooglePath.setText(sb.toString());
	}
}
