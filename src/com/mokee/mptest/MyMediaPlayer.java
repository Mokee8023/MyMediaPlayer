package com.mokee.mptest;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
/**
 * 两种播放模式(1.从头播放	 2.从特定位置播放)
 * 不使用该控件,需要释放控件(release())
 * @author Mokee_Work
 *
 */
public class MyMediaPlayer extends LinearLayout implements Callback,
		OnCompletionListener, OnErrorListener, OnPreparedListener,
		OnSeekBarChangeListener, OnInfoListener {
	private static final String tag = "MyMediaPlayer";
	
	private Context mContext;
	private View mView;
	private SurfaceView mSurfaceView;
	private RelativeLayout mRelativeLayout;
	private ImageButton mControlPlay;
	private SeekBar mControlProcess;
	
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	private Handler delayHandler;
	private boolean isLooping;// 是否循环
	private boolean isExit = false;// 是否结束
	private int seekPosition = 0;// 从特定位置播放
	private onEndPlayingListener mEndPlayingListener = null;
	private onErrorListener mErrorListener = null;
	private onInfoListener mInfoListener = null;
	
	public MyMediaPlayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	public MyMediaPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	public MyMediaPlayer(Context context) {
		super(context);
		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.my_media_player, this, true);
		initView();
	}

	/**
	 * 初始化View,即控件初始化
	 */
	private void initView() {
		delayHandler = new Handler();
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
		
		displayMenu();
	}

	/**
	 * 设置MediaPlayer的监听器(OnCompletionListener、OnPreparedListener、OnErrorListener、setLooping)
	 */
	private void setPlayerLinstener() {
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this); 
		mMediaPlayer.setOnErrorListener(this);  
		mMediaPlayer.setLooping(isLooping);
		mMediaPlayer.setOnInfoListener(this);
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
		if(!isExit && mMediaPlayer != null){
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
		
		if (mMediaPlayer != null) {
			seekPosition = mMediaPlayer.getCurrentPosition();
			mMediaPlayer.setDisplay(null);
			mMediaPlayer.reset();
		}		
	}
	/***********************************************************************/
	
	
	/************************** MediaPlayer的监听 ***************************/
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(tag, "MyMediaPlayer.onPrepared");
		setLayoutParam(mp);
		mMediaPlayer.start();
		if(seekPosition > 1000){
			mMediaPlayer.seekTo(seekPosition - 1000);
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
		if(mEndPlayingListener != null){
			mEndPlayingListener.endPlaying(true);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		String error = "";
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_IO:
			error = "MEDIA_ERROR_IO";
			break;
		case MediaPlayer.MEDIA_ERROR_MALFORMED:
			error = "MEDIA_ERROR_MALFORMED";
			break;
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			error = "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
			break;
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			error = "MEDIA_ERROR_SERVER_DIED";
			break;
		case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
			error = "MEDIA_ERROR_TIMED_OUT";
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			error = "MEDIA_ERROR_UNKNOWN";
			break;
		case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
			error = "MEDIA_ERROR_UNSUPPORTED";
			break;

		default:
			break;
		}
		if(mErrorListener != null){
			mErrorListener.error(error, what);
			// release();
		}
		Log.i(tag, "MyMediaPlayer.onError：" + error);
		return false;
	}
	
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		String info = "";
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			info = "MEDIA_INFO_BAD_INTERLEAVING";
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			info = "MEDIA_INFO_BUFFERING_END";
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			info = "MEDIA_INFO_BUFFERING_START";
			break;
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			info = "MEDIA_INFO_METADATA_UPDATE";
			break;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			info = "MEDIA_INFO_NOT_SEEKABLE";
			break;
		case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
			info = "MEDIA_INFO_SUBTITLE_TIMED_OUT";
			break;
		case MediaPlayer.MEDIA_INFO_UNKNOWN:
			info = "MEDIA_INFO_UNKNOWN";
			break;
		case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
			info = "MEDIA_INFO_UNSUPPORTED_SUBTITLE";
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			info = "MEDIA_INFO_VIDEO_RENDERING_START";
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			info = "MEDIA_INFO_VIDEO_TRACK_LAGGING";
			break;

		default:
			break;
		}
		if(mInfoListener != null){
			mInfoListener.info(info, what);
		}
		Log.i(tag, "MyMediaPlayer.onInfo：" + info);
		return false;
	}
	
	/***********************************************************************/
	
	/**
	 * 底部菜单栏显示和隐藏效果
	 */
	TranslateAnimation mShowAction = new TranslateAnimation(
			Animation.ZORDER_BOTTOM, 0.0f, Animation.ZORDER_BOTTOM, 0.0f,
			Animation.ZORDER_BOTTOM, -1.0f, Animation.ZORDER_BOTTOM, 0.0f);

	TranslateAnimation mHiddenAction = new TranslateAnimation(
			Animation.ZORDER_BOTTOM, 0.0f, Animation.ZORDER_BOTTOM, 0.0f,
			Animation.ZORDER_BOTTOM, 0.0f, Animation.ZORDER_BOTTOM, -1.0f);
	
	/**
	 * 用于隐藏控制按钮
	 */
	private Runnable hideRunnable = new Runnable() {
		
		@Override
		public void run() {
			mRelativeLayout.setVisibility(View.GONE);
			mHiddenAction.setDuration(1500);
			mRelativeLayout.setAnimation(mHiddenAction);
		}
	};
	
	/** 隐藏与显示底部控制按钮    */
	private void displayMenu() {
		if (mRelativeLayout.getVisibility() == View.VISIBLE) {
			mRelativeLayout.setVisibility(View.GONE);
			mHiddenAction.setDuration(1500);
			mRelativeLayout.setAnimation(mHiddenAction);
		} else if (mRelativeLayout.getVisibility() == View.GONE) {
			mRelativeLayout.setVisibility(View.VISIBLE);
			mShowAction.setDuration(1500);
			mRelativeLayout.setAnimation(mShowAction);
		}
	}

	/**
	 * 自定义OnClickListener,实现控制SurfaceView、Play、SeekBar的点击事件
	 */
	private OnClickListener myClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.wSurfaceView:
				displayMenu();
				break;
			case R.id.wControlPlay:
				changePlayState();
				break;
			case R.id.wControlProcess:
				break;

			default:
				break;
			}
			delayHandler.removeCallbacks(hideRunnable);
			delayHandler.postDelayed(hideRunnable, 5 * 1000);
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
	
	/** 播放完毕回调接口  */
	public interface onEndPlayingListener{
		public void endPlaying(boolean isEnd);
	}
	/** 播放错误回调接口  */
	public interface onErrorListener{
		public void error(String error, int what);
	}
	/** 播放信息回调接口  */
	public interface onInfoListener{
		public void info(String info, int what);
	}
	
	/**
	 * 如果设置了循环播放,则设置该监听器无用
	 * @param lisenter	onEndPlayingLisenter
	 */
	public void setOnEndPlayingLisenter(onEndPlayingListener lisenter){
		this.mEndPlayingListener = lisenter;
	}
	public void setOnErrorListener(onErrorListener lisenter){
		this.mErrorListener = lisenter; 
	}
	public void setOnInfoListener(onInfoListener lisenter){
		this.mInfoListener = lisenter; 
	}
	
	public void release() {
		isExit = true;
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
	}
}
