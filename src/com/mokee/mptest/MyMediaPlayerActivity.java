package com.mokee.mptest;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 该类播放SD卡的文件时需要权限:android.permission.READ_EXTERNAL_STORAGE
 * @author Mokee_Work
 *
 */
public class MyMediaPlayerActivity extends Activity implements
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, OnClickListener {
	
	private static final String tag = "MyMediaPlayerActivity";
	
	private Display curDisplay;
	private SurfaceView sv_MediaPlayer;
	private MediaPlayer mediaPlayer;  
	private String videoPath = "/storage/ext_sd/test_video.mp4";
	private SurfaceHolder holder;
	private int videoWidth, videoHeight;
	private Button btn_MediaPlayerControl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mediaplayer);
		
		initView();
		mediaPlayer = new MediaPlayer();
		holder = sv_MediaPlayer.getHolder();
		holder.addCallback(this);
		setPlayerListener();
		setVideoDataSource(videoPath);
		
		curDisplay = this.getWindowManager().getDefaultDisplay();
	}

	private void initView() {
		sv_MediaPlayer = (SurfaceView) findViewById(R.id.sv_MediaPlayer);
		btn_MediaPlayerControl = (Button) findViewById(R.id.btn_MediaPlayerControl);
		btn_MediaPlayerControl.setOnClickListener(this);
	}

	private void setPlayerListener() {
		mediaPlayer.setOnCompletionListener(this);  
		mediaPlayer.setOnErrorListener(this);  
		mediaPlayer.setOnInfoListener(this);  // 当一些特定信息出现或者警告时触发
		mediaPlayer.setOnPreparedListener(this);  
		mediaPlayer.setOnSeekCompleteListener(this);  
		mediaPlayer.setOnVideoSizeChangedListener(this);  	
	}
	
	private void setVideoDataSource(String videoPath) {
		try {
			mediaPlayer.setDataSource(videoPath);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log.e(tag, "setVideoDataSource.IllegalArgumentException:" + e.toString());
		} catch (SecurityException e) {
			e.printStackTrace();
			Log.e(tag, "setVideoDataSource.SecurityException:" + e.toString());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Log.e(tag, "setVideoDataSource.IllegalStateException:" + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(tag, "setVideoDataSource.IOException:" + e.toString());
		}  
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 当SurfaceView中的Surface被创建的时候被调用
		Log.i(tag, "SurfaceCreated called");  
		
		mediaPlayer.setDisplay(holder); // 指定MediaPlayer在当前的Surface中进行播放
		mediaPlayer.prepareAsync(); // 使用prepare或者prepareAsync准备播放
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// 当Surface尺寸等参数改变时触发
		Log.i(tag, "surfaceChanged called");  
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(tag, "surfaceDestroyed called");  
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		 // 当video大小改变时触发 ，该方法在设置MediaPlayer的source后至少触发一次
		Log.i(tag, "onVideoSizeChanged called");
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// seek操作完成时触发
		Log.i(tag, "onSeekComplete called");
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		// 当prepare完成后，该方法触发，在这里播放视频
		
		videoWidth = mediaPlayer.getVideoWidth();
		videoHeight = mediaPlayer.getVideoHeight();
		
		Log.i(tag, "First:	videoWidth:" + videoWidth + ",	videoHeight" + videoHeight);
		Log.i(tag, "curDisplay.getWidth():" + curDisplay.getWidth() + ",	curDisplay.getHeight()" + curDisplay.getHeight());
		
		if(videoWidth > curDisplay.getWidth() || videoHeight > curDisplay.getHeight()){
			float wRatio = (float)videoWidth / (float)curDisplay.getWidth();
			float hRatio = (float)videoHeight / (float)curDisplay.getHeight();
			
			float ratio = Math.max(wRatio, hRatio);
			Log.i(tag, "ratio:" + ratio);
			
			videoWidth = (int) Math.ceil((float) videoWidth * ratio);
			videoHeight = (int) Math.ceil((float) videoHeight * ratio);
			
			Log.i(tag, "After:	videoWidth:" + videoWidth + ",	videoHeight" + videoHeight);
			
			// 设置surfaceView的布局参数
			sv_MediaPlayer.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight));
			
		} else {
			sv_MediaPlayer.setLayoutParams(new LinearLayout.LayoutParams(curDisplay.getWidth(), videoHeight));
		}
		
		player.start();// 开始播放视频
	}

	@Override
	public boolean onInfo(MediaPlayer player, int what, int extra) {
		// 当一些特定信息出现或者警告时触发
		
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			Log.i(tag, "onInfo.MEDIA_INFO_BAD_INTERLEAVING");  
			break;
			
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			Log.i(tag, "onInfo.MEDIA_INFO_METADATA_UPDATE");  
			break;
			
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			Log.i(tag, "onInfo.MEDIA_INFO_VIDEO_TRACK_LAGGING");  
			break;
			
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.i(tag, "onInfo.MEDIA_INFO_NOT_SEEKABLE");  
			break;

		default:
			break;
		}
		
		return false;
	}

	@Override
	public boolean onError(MediaPlayer player, int what, int extra) {
		
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Log.e(tag, "onError.MEDIA_ERROR_SERVER_DIED");  
			break;
			
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Log.e(tag, "onError.MEDIA_ERROR_UNKNOWN");  
			break;

		default:
			break;
		}
		
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		// 播放完成后触发
		Log.i(tag, "onCompletion called"); 

		player.seekTo(0);
		btn_MediaPlayerControl.setText("Play");
		// player.release();// 释放
		// this.finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_MediaPlayerControl:
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();
				btn_MediaPlayerControl.setText("Play");
			} else {
				mediaPlayer.start();
				btn_MediaPlayerControl.setText("Pause");
			}
			break;

		default:
			break;
}		
	}
}
