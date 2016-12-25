package org.lance.util;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 单例+装饰模式 不需要关联UI,保证每次播放一首歌曲
 * 
 * @author chengkai.gan
 * 
 */
public class MPMediaPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener,
        OnSeekCompleteListener, OnBufferingUpdateListener {
    public static final String TAG = "MPMediaPlayer";

    private static MPMediaPlayer instance = null;

    private MPMediaPlayer() {
    }

    public static MPMediaPlayer getInstance() {
        instance = getInstance(null);
        return instance;
    }

    public static MPMediaPlayer getInstance(OnMPMediaPlayerListener listener) {
        if (instance == null) {
            instance = new MPMediaPlayer();
        }

        instance.mListener = listener;
        return instance;
    }

    private MediaPlayer mPlayer = null;

    private OnMPMediaPlayerListener mListener;

    private boolean isIniting;

    private final int UPDATE_PROGRESS = 1;

    private final int update_time = 250;

    private boolean isOnline;// 是否在线播放

    private String playPath;// 播放地址---本地或者在线

    private int bufferPercent;// 缓存百分比 0~100

    private Handler handler = new Handler() {// 循环检测播放进度

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case UPDATE_PROGRESS:
                if (mPlayer != null) {

                    int duration = mPlayer.getDuration();
                    int position = mPlayer.getCurrentPosition();
                    float percent = 0;
                    if (duration <= 0) {
                        percent = 0;
                    } else {
                        percent = position * 1f / duration;
                    }
                    if (mListener != null) {
                        if (isOnline) {
                            if (percent >= 0 && percent <= bufferPercent / 100f) {
                                mListener.onProgress(mPlayer, percent);
                            }
                        } else {
                            mListener.onProgress(mPlayer, percent);
                        }
                    }
                    if (mPlayer.isPlaying()) {
                        sendEmptyMessageDelayed(UPDATE_PROGRESS, update_time);
                    }
                }

                break;
            }
        }
    };

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }

        return false;
    }

    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.getDuration() <= 0) {
            return;
        }

        mPlayer.seekTo(msec);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mPlayer == null) {
            return;
        }

        mPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setOnMPMediaPlayerListener(OnMPMediaPlayerListener listener) {
        this.mListener = listener;
    }

    /**
     * 播放音乐
     * 
     * @param url
     *            音乐地址
     * @param isOnline
     *            是否是网络地址
     */
    public synchronized void play(String path, boolean isOnline) {
        Log.i(TAG, "play url:" + path);
        if (isIniting) {
            return;
        }
        isIniting = true;

        this.isOnline = isOnline;
        this.playPath = path;

        try {
            stopPlay();
            
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(path);

            mPlayer.setVolume(1, 1);

            mPlayer.setLooping(true);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnSeekCompleteListener(this);
            mPlayer.setOnBufferingUpdateListener(this);

            if (isOnline) {
                mPlayer.prepareAsync();// 异步缓冲
            } else {
                mPlayer.prepare();// 播放本地地址---当前线程中缓冲
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isIniting = false;
        }
    }

    // 停止播放器---定时器继续
    private synchronized void stopMediaPlayer() {
    	Log.i(TAG, "stopMediaPlayer");
        isIniting = false;
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public synchronized void seekToPercent(float percent) {
        if (mPlayer != null) {
            int duration = mPlayer.getDuration();
            mPlayer.seekTo((int) (duration * percent));
            mPlayer.start();
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    /**
     * 开始播放
     */
    public synchronized void startPlay() {
        Log.i(TAG, "startPlay");
        if (mPlayer != null) {
            try {
                mPlayer.start();
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 多片段---此时定时器也应该取消---但不释放资源
     */
    public synchronized void pausePlay() {
        Log.i(TAG, "pausePlay");
        if (mPlayer != null) {
            try {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放相关资源---并停止定时器
     */
    public synchronized void stopPlay() {
        Log.i(TAG, "stopPlay");
        stopMediaPlayer();
    }

    public static interface OnMPMediaPlayerListener {

        /**
         * 回调播放进度---0~1
         * 
         * @param progress
         */
        public void onProgress(MediaPlayer mp, float progress);

        /**
         * 回调在线缓冲进度
         * 
         * @param mp
         * @param percent
         */
        public void onBufferingUpdate(MediaPlayer mp, int percent);

        /**
         * 播放完成回调
         * 
         * @param mp
         */
        public void onComplete(MediaPlayer mp);
        
        /**
         * 播放出错---重置一些状态
         * @param mp
         */
        public void onError(MediaPlayer mp);

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.i(TAG, "onSeekComplete:" + mp.getDuration());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        System.out.println("onError:" + mp.getDuration() + ":" + what + "  | " + extra);
        Log.i(TAG, "onError:" + mp.getDuration() + ":" + what + "  | " + extra);
        if(mListener!=null){
            mListener.onError(mp);
        }

        if (mp != null) {
            mp.reset();
        }
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        try {
            mp.setDataSource(playPath);

            if (isOnline) {
                mp.prepareAsync();// 异步缓冲
            } else {
                mp.prepare();// 播放本地地址---当前线程中缓冲
            }
            isIniting = true;
        }catch (Exception e) {
            e.printStackTrace();
            isIniting = false;
        }

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "onPerpared:" + mp.getDuration());
        try {
            if (isOnline == false) {
                if (mListener != null) {
                    mListener.onBufferingUpdate(mPlayer, 100);
                }
            }
            if (mPlayer != null && !mPlayer.isPlaying()) {
                mPlayer.seekTo(0);
                mPlayer.start();
                handler.sendEmptyMessage(UPDATE_PROGRESS);
                isIniting = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isIniting = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion:" + mp.getDuration());
        if (mListener != null) {
            mListener.onComplete(mPlayer);
        }
    }

    @Override
    // 远程音乐进度缓冲
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.i(TAG, "onBufferingUpdate:" + mp.getDuration() + "---" + percent);
        bufferPercent = percent;
        if (mListener != null) {
            mListener.onBufferingUpdate(mp, percent);
        }
    }

}
