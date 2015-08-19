package com.mokee.mptest;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MyMediaPlayer extends LinearLayout implements Callback, OnCompletionListener, OnErrorListener, OnPreparedListener {
	private static final String tag = "MyMediaPlayer";
	
	private View mView;
	private SurfaceView mSurfaceView;
	private RelativeLayout mRelativeLayout;
	private ImageButton mControlPlay;
	private ProgressBar mControlProcess;
	
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	private Handler mHandler;
	
	TimeService time;

	public MyMediaPlayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	public MyMediaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	public MyMediaPlayer(Context context) {
		super(context);
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	private void initView() {
		mSurfaceView = (SurfaceView) mView.findViewById(R.id.wSurfaceView);
		mRelativeLayout = (RelativeLayout) mView.findViewById(R.id.wRelativeLayout);
		mControlPlay = (ImageButton) mView.findViewById(R.id.wControlPlay);
		mControlProcess = (ProgressBar) mView.findViewById(R.id.wControlProcess);
		
		mControlPlay.setImageResource(R.drawable.pause);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mMediaPlayer = new MediaPlayer();
		setPlayerLinstener();
		
		mHandler = new Handler();
		
		mControlPlay.setOnClickListener(myClickListener);
		mControlProcess.setOnClickListener(myClickListener);
		mSurfaceView.setOnClickListener(myClickListener);
		
		time = new TimeService();
	}

	private void setPlayerLinstener() {
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this); 
		mMediaPlayer.setOnErrorListener(this);  
		// mMediaPlayer.setOnInfoListener(this);
		// mMediaPlayer.setOnSeekCompleteListener(this);
		// mMediaPlayer.setOnVideoSizeChangedListener(this);	
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
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onPrepared");
		setLayoutParam(mp);
		mMediaPlayer.start();
		mControlProcess.setMax(mMediaPlayer.getDuration());
		Log.i(tag, "Length:" + mMediaPlayer.getDuration());
		time.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onCompletion");
		time.stop();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.i(tag, "MyMediaPlayer.onError");
		return false;
	}
	
	private Runnable showRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(mRelativeLayout.getVisibility() == View.GONE){
				mRelativeLayout.setVisibility(View.VISIBLE);
			} else {
				mRelativeLayout.setVisibility(View.GONE);
			}
		}
	};
	
	private OnClickListener myClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.wSurfaceView:
				mHandler.post(showRunnable);
				break;
			case R.id.wControlPlay:
				changePlayState();
				break;
			case R.id.wControlProcess:
				break;

			default:
				break;
			}
		}
	};
	
	private void changePlayState(){
		if(mMediaPlayer.isPlaying()){
			Log.i(tag, "MyMediaPlayer.pause:" + mMediaPlayer.getCurrentPosition());
			mMediaPlayer.pause();
			mControlPlay.setImageResource(R.drawable.play);
		} else {
			Log.i(tag, "MyMediaPlayer.start");
			mMediaPlayer.start();
			mControlPlay.setImageResource(R.drawable.pause);
		}
	}
	
	private void setLayoutParam(MediaPlayer mp) {
		int videoWidth, videoHeight, screenWidth, screenHeight;
		videoWidth = mp.getVideoWidth();
		videoHeight = mp.getVideoHeight();
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
	private static final int TIMESERVICE = 1;
	private Handler mTimeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TIMESERVICE){
				Log.i(tag, "Current Length:" + mMediaPlayer.getCurrentPosition());
				mControlProcess.setProgress(mMediaPlayer.getCurrentPosition());
			}
		}
	};
	
	private class TimeService extends Thread {
		@Override
		public void run() {
			super.run();
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.i(tag, e.toString());
				}
				mTimeHandler.sendEmptyMessage(TIMESERVICE);
			}
		}
	}
}
