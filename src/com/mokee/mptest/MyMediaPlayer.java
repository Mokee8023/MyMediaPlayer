package com.mokee.mptest;

import java.io.IOException;

import android.annotation.SuppressLint;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
/**
 * 在Pause时,如果Resume会抛出异常,暂时未解决,可以通过在Pause时获取播放位置,Resume时,从特定位置播放
 * @author Mokee_Work
 *
 */
public class MyMediaPlayer extends LinearLayout implements Callback,
		OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekBarChangeListener {
	private static final String tag = "MyMediaPlayer";
	
	private View mView;
	private SurfaceView mSurfaceView;
	private RelativeLayout mRelativeLayout;
	private ImageButton mControlPlay;
	private SeekBar mControlProcess;
	
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	private Handler mHandler;
	private boolean isLooping;// 是否循环
	private boolean isExit = false;// 是否结束
	private int seekPosition = 0;// 从特定位置播放
	private onEndPlayingLisenter mEndPlayingLisenter = null;
	
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

	/**
	 * 初始化View,即控件初始化
	 */
	private void initView() {
		mHandler = new Handler();
		time = new TimeService();
		time.start();
		isLooping = false;
		
		mSurfaceView = (SurfaceView) mView.findViewById(R.id.wSurfaceView);
		mRelativeLayout = (RelativeLayout) mView.findViewById(R.id.wRelativeLayout);
		mControlPlay = (ImageButton) mView.findViewById(R.id.wControlPlay);
		mControlProcess = (SeekBar) mView.findViewById(R.id.wControlProcess);
		
		mControlPlay.setImageResource(R.drawable.pause);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mMediaPlayer = new MediaPlayer();
		setPlayerLinstener();
		
		mControlPlay.setOnClickListener(myClickListener);
		mSurfaceView.setOnClickListener(myClickListener);
		mControlProcess.setOnSeekBarChangeListener(this);
	}

	/**
	 * 设置MediaPlayer的监听器(OnCompletionListener、OnPreparedListener、OnErrorListener、setLooping)
	 */
	private void setPlayerLinstener() {
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this); 
		mMediaPlayer.setOnErrorListener(this);  
		mMediaPlayer.setLooping(isLooping);
		// mMediaPlayer.setOnInfoListener(this);
		// mMediaPlayer.setOnSeekCompleteListener(this);
		// mMediaPlayer.setOnVideoSizeChangedListener(this);	
	}
	
	/**
	 * 设置播放器的DataSource,异步Prepare
	 * @param path	File路径
	 */
	public void setMediaDataResource(String path){
		try {
			mMediaPlayer.reset();
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
	/**
	 * 设置播放器的DataSource,异步Prepare
	 * @param path	File路径
	 */
	public void setMediaDataResource(String path, int positon){
		try {
			this.seekPosition = positon;
			mMediaPlayer.reset();
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
	
	/**
	 * 获取MediaPlayer是否循环播放
	 * @return	true:循环播放,false:不循环播放
	 */
	public boolean getMediaPlayerLooping(){
		return isLooping;
	}

	/**
	 * 设置是否循环播放
	 * @param isLooping 	true:循环播放,false:不循环播放
	 */
	public void setMediaPlayerLooping(boolean isLooping){
		this.isLooping = isLooping;
		mMediaPlayer.setLooping(this.isLooping);
	}
	
	/**
	 * 获取当前播放器中的视频长度和当前播放长度(以毫秒为单位)
	 * @return		int[0] 当前播放长度	int[1]：视频总长度
	 */
	public int[] getMediaProcess(){
		int[] duration = new int[2];
		if(!isExit && mMediaPlayer != null && mMediaPlayer.isPlaying()){
			duration[0] = mMediaPlayer.getCurrentPosition();
			duration[1] = mMediaPlayer.getDuration();
		}
		return duration;
	}
	
	/************************ SurfaceHolder的回调接口 ************************/
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
		isExit = true;
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}		
	}
	/***********************************************************************/
	
	
	/************************** MediaPlayer的监听 ***************************/
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onPrepared");
		setLayoutParam(mp);
		mMediaPlayer.start();
		if(seekPosition != 0){
			mMediaPlayer.seekTo(seekPosition);
			seekPosition = 0;
		}
		mMediaPlayer.setLooping(this.isLooping);
		mControlProcess.setMax(mMediaPlayer.getDuration());
		mControlPlay.setImageResource(R.drawable.pause);
		Log.i(tag, "Length:" + mMediaPlayer.getDuration());	
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onCompletion");
		mControlPlay.setImageResource(R.drawable.play);
		if(mEndPlayingLisenter != null){
			mEndPlayingLisenter.endPlaying(true);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.i(tag, "MyMediaPlayer.onError");
		return false;
	}
	
	/***********************************************************************/
	
	/**
	 * 用于控制点击SurfaceView显示或者隐藏控制按钮
	 */
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
	
	/**
	 * 自定义OnClickListener,实现控制SurfaceView、Play、SeekBar的点击事件
	 */
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
	
	/**
	 * 更改播放器的播放状态,暂停和播放
	 */
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
	
	/**
	 * 根据视频大小缩放设置播放器的宽高
	 * @param mp	播放器
	 */
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
	
	/*************************时间监听,用于每隔1秒改变播放进度************************/
	private TimeService time;
	private static final int TIMESERVICE = 1;
	@SuppressLint("HandlerLeak") 
	private Handler mTimeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TIMESERVICE){
				if(!isExit && mMediaPlayer.isPlaying()){
					mControlProcess.setProgress(mMediaPlayer.getCurrentPosition());
				}				
			}
		}
	};
	
	private class TimeService extends Thread {
		@Override
		public void run() {
			super.run();
			while (!isExit) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.i(tag, e.toString());
				}
				mTimeHandler.sendEmptyMessage(TIMESERVICE);
			}
		}
	}
	
	/******************************************************************************/
	
	/****************************** SeekBar的监听回调 ******************************/

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.i(tag, "Start Tracking Touch:" + mMediaPlayer.getCurrentPosition());		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.i(tag, "Stop Tracking Touch:" + seekBar.getProgress());
		mMediaPlayer.seekTo(seekBar.getProgress());
	}
	
	/******************************************************************************/
	
	/**
	 * 播放完毕回调接口
	 */
	public interface onEndPlayingLisenter{
		public void endPlaying(boolean isEnd);
	}
	
	/**
	 * 如果设置了循环播放,则设置该监听器无用
	 * @param lisenter	onEndPlayingLisenter
	 */
	public void setOnEndPlayingLisenter(onEndPlayingLisenter lisenter){
		this.mEndPlayingLisenter = lisenter;
	}
}
