package org.lance.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

/** 
 * 音乐播放服务
 * @author ganchengkai
 *
 */
public class AudioService extends Service implements
			MediaPlayer.OnCompletionListener,
			MediaPlayer.OnErrorListener,
			MediaPlayer.OnSeekCompleteListener,
			MediaPlayer.OnPreparedListener {
	private final String TAG = "AudioService";

	private MediaPlayer player;

	private Entity mEntity;

	private Timer mTimer = null;

	private AudioTimerTask mTimerTask = null;

	private final int REFRESH_PLAY_STATE_TIME = 250;

	private boolean isIniting = false;// 是否处于初始化中

	private class AudioTimerTask extends TimerTask {
		@Override
		public void run() {
			try {
				if (player != null && player.isPlaying()) {
					int playPos = player.getCurrentPosition();
					int end_time = mEntity.end_time;
					int offsetPlayPos = playPos + REFRESH_PLAY_STATE_TIME + 10;
					if (offsetPlayPos >= end_time) {
						player.seekTo(mEntity.start_time);
					}
				} else {
				    if(isIniting){
				        return;
				    }
					int res = initPlayer(mEntity);
				}
			} catch (Exception e) {
			}
		}
	};

	private final IBinder binder = new AudioBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopTimerTask();
		return super.onUnbind(intent);
	}

	@Override
	public void onCompletion(MediaPlayer player) {
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		isIniting = false;
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		try {
			if (player != null && !player.isPlaying()) {
				if (mEntity != null) {
					player.seekTo(mEntity.start_time);
				}
				player.start();
				isIniting = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			isIniting = false;
		}

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	public void setmEntity(Entity entity){
		this.mEntity=entity;
	}

	public synchronized boolean isPlaying() {
		try {
			return player.isPlaying();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public synchronized void stopTimerTask() {
		isIniting = false;
		if (mTimer != null) {
			mTimer.purge();
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	private synchronized void stopMediaPlayer() {
		isIniting = false;
		if (player != null) {
			mEntity = null;
			player.stop();
			player.release();
			player = null;
		}
	}

	public synchronized void startPlay() {
		if (mEntity == null) {
			return;
		}
		stopTimerTask();
		mTimer = new Timer(true);
		mTimerTask = new AudioTimerTask();
		mTimer.schedule(mTimerTask, 0, REFRESH_PLAY_STATE_TIME);
	}

	public synchronized void pausePlay() {
		stopTimerTask();
		if (player != null) {
			try {
				if (player.isPlaying()) {
					player.pause();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void stopPlay() {
		stopTimerTask();
		stopMediaPlayer();
	}

	@Override
	public synchronized void onDestroy() {
		stopPlay();
	}

	private synchronized int initPlayer(Entity entity) {
		if (isIniting) {
			return 0;
		}
		isIniting = true;
		try {
			if (player != null) {
				try {
					player.stop();
					player.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				player = null;
			}
			player = new MediaPlayer();
			player.reset();// 把各项参数恢复到最初始的状态
			player.setDataSource(entity.path);
			player.setVolume(0.5f,0.5f);
			mEntity = entity;

			player.setLooping(entity.isLoop);
			player.setOnCompletionListener(this);
			player.setOnPreparedListener(this);
			player.setOnErrorListener(this);
			player.setOnSeekCompleteListener(this);

			player.prepare();// 进行数据缓冲

		} catch (Exception ex) {
			ex.printStackTrace();
			isIniting = false;
			return 0;
		}

		return 1;
	}

	/**
	 * 设置音量---native 抛出IllegalStateException
	 * 
	 * @param leftVolume
	 * @param rightVolume
	 * @return
	 */
	public boolean setVolume(float leftVolume, float rightVolume) {
		if (player != null) {
			try {
				if (leftVolume < 0) {
					leftVolume = 0;
				} else if (leftVolume > 1) {
					leftVolume = 1;
				}
				if (rightVolume < 0) {
					rightVolume = 0;
				} else if (rightVolume > 1) {
					rightVolume = 1;
				}
				player.setVolume(leftVolume, rightVolume);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public class AudioBinder extends Binder {

		public AudioService getService() {
			return AudioService.this;
		}

	}

	public class Entity {
		
		public String path;//音乐路径
		public boolean isLoop;//是否循环播放
		public int start_time;
		public int end_time;
	}
}
