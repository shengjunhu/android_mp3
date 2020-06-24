package com.hsj.mp3;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;

/**
 * @Author:hsj
 * @Date:2020-06-19 12:44
 * @Class:Mp3Player
 * @Desc:
 */
public final class Mp3Player implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "Mp3Player";
    private Handler playerHandler;
    private MediaPlayer mediaPlayer;
    private Mp3PlayerCallback mp3PlayerCallback;
    private boolean isPrepared, isPlay, isWait, isError;

    /**
     * 第一步：初始化播放器
     *
     * @param mp3PlayerCallback
     */
    public Mp3Player(Mp3PlayerCallback mp3PlayerCallback) {
        this.mp3PlayerCallback = mp3PlayerCallback;
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setLooping(false);
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        HandlerThread mp3PlayerThread = new HandlerThread("mp3_player_thread");
        this.playerHandler = new Handler(mp3PlayerThread.getLooper());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        if (mp3PlayerCallback != null) {
            mp3PlayerCallback.onCreate(mediaPlayer.getDuration() / 1000);
        }
        if (isWait) {
            start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isPlay = false;
        if (mp3PlayerCallback != null) {
            mp3PlayerCallback.onStop();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (isError) {
            if (mp3PlayerCallback != null) {
                mp3PlayerCallback.onError(what);
            }
        } else {
            mediaPlayer.reset();
            start();
            isError = true;
        }
        return true;
    }

    /**
     * 获取播放状态
     *
     * @return
     */
    public boolean isPlay() {
        return isPlay;
    }

    /**
     * 第二步：播放文件 Path/Url
     *
     * @param audioPath
     */
    public void setPath(String audioPath) {
        if (null == mediaPlayer) return;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 第三步：开始播放音频
     */
    public void start() {
        if (!isPrepared) {
            //Log.i(TAG, "正在缓冲...");
            if (mp3PlayerCallback != null) {
                mp3PlayerCallback.onBuffering();
            }
            isWait = true;
            return;
        }
        isWait = false;
        try {
            mediaPlayer.start();
            playerHandler.post(this::play);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第五步：停止播放音频
     */
    public void stop() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            isPlay = false;
            if (mp3PlayerCallback != null) {
                mp3PlayerCallback.onStop();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 第六步：停止播放音频
     */
    public void destroy() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (playerHandler != null) {
            playerHandler.getLooper().quitSafely();
        }
    }

    /**
     * 播放
     */
    private void play() {
        isPlay = true;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentTime = Math.round(mediaPlayer.getCurrentPosition() / 1000f);
            if (mp3PlayerCallback != null) {
                mp3PlayerCallback.onPlaying(currentTime);
            }
            playerHandler.postDelayed(this::play, 500);
        } else {
            isPlay = false;
            if (mp3PlayerCallback != null) {
                mp3PlayerCallback.onPlaying(0);
            }
        }
    }

    /**
     * 播放回调
     */
    public interface Mp3PlayerCallback {

        /**
         * 开始播放
         *
         * @param totalTime
         */
        void onCreate(int totalTime);

        /**
         * 正在缓冲
         */
        void onBuffering();

        /**
         * 播放中
         *
         * @param currentTime
         */
        void onPlaying(int currentTime);

        /**
         * 播放完成
         */
        void onStop();

        /**
         * 播放应异常
         *
         * @param what
         */
        void onError(int what);
    }

}
