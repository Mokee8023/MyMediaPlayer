package com.mokee.mptest;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class MyMediaPlayer_1 extends SurfaceView implements Callback, OnCompletionListener, OnErrorListener, OnInfoListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener {
	private static final String tag = "MyMediaPlayer_1";
	
	public MyMediaPlayer_1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MyMediaPlayer_1(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyMediaPlayer_1(Context context) {
		super(context);
		init();
	}
	
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	
	private void init() {
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		mMediaPlayer = new MediaPlayer();
		setPlayerLinstener();
	}

	private void setPlayerLinstener() {
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);  
		mMediaPlayer.setOnInfoListener(this);
		mMediaPlayer.setOnPreparedListener(this);  
		mMediaPlayer.setOnSeekCompleteListener(this);  
		mMediaPlayer.setOnVideoSizeChangedListener(this);  	
	}
	public void setMediaDataResource(String path){
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(tag, "MyMediaPlayer.surfaceCreated");
		mMediaPlayer.setDisplay(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(tag, "MyMediaPlayer.surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(tag, "MyMediaPlayer.surfaceDestroyed");
	}
	
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.i(tag, "MyMediaPlayer.onInfo");
		return false;
	}
	
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.i(tag, "MyMediaPlayer.onVideoSizeChanged");
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onPrepared");
		setLayoutParam();
		mMediaPlayer.start();
	}

	private void setLayoutParam() {
		int videoWidth, videoHeight, screenWidth, screenHeight;
		videoWidth = mMediaPlayer.getVideoWidth();
		videoHeight = mMediaPlayer.getVideoHeight();
		screenWidth = this.getWidth();
		screenHeight = this.getHeight();
		
		Log.i(tag, "Video Width:" + videoWidth + ",	Height" + videoHeight);
		Log.i(tag, "Screen Width:" + screenWidth + ",	Height()" + screenHeight);
		
		if(videoWidth > screenWidth || videoHeight > screenHeight){
			float wRatio = (float)videoWidth / (float)screenWidth;
			float hRatio = (float)videoHeight / (float)screenHeight;
			
			float ratio = Math.max(wRatio, hRatio);
			Log.i(tag, "ratio:" + ratio);
			
			videoWidth = (int) Math.ceil((float) videoWidth / ratio);
			videoHeight = (int) Math.ceil((float) videoHeight / ratio);
			
			Log.i(tag, "After:	videoWidth:" + videoWidth + ",	videoHeight" + videoHeight);
			
			this.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
		} else {
			this.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, videoHeight));
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.i(tag, "MyMediaPlayer.onError");
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onCompletion");
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onSeekComplete");
	}
	
	public void changState(){
		if(mMediaPlayer.isPlaying()){
			mMediaPlayer.pause();
		} else {
			mMediaPlayer.start();
		}
	}
}
