package com.hsj.mp3.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hsj.mp3.R;
import java.io.IOException;

/**
 * @Author:hsj
 * @Date:2020-06-24 16:41
 * @Class:Mp3PlayerView
 * @Desc:
 */
public final class Mp3PlayerView  extends LinearLayout implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private ImageView iv_voice;
    private TextView tv_voice_time;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private boolean isWait, isPrepared, isError;
    private AnimationDrawable animation;
    private LinearLayout ll_voice;

    public Mp3PlayerView(Context context) {
        this(context, null);
    }

    public Mp3PlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Mp3PlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_mp3_player, this);
        iv_voice = findViewById(R.id.iv_voice);
        tv_voice_time = findViewById(R.id.tv_voice_time);

        ll_voice = findViewById(R.id.ll_voice);
        ll_voice.setOnClickListener(v -> {
            if (null != mediaPlayer) {
                if (mediaPlayer.isPlaying()) {
                    stopPlay();
                } else {
                    isWait = true;
                    startPlay();
                }
            }
        });
    }

    /**
     * 设置本地Path or Url
     * @param audioPath
     */
    public void setPath(@NonNull String audioPath) {
        if (TextUtils.isEmpty(audioPath)) return;
        stopPlay();
        destroy();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
     * 开始播放语音
     */
    private void startPlay() {
        if (!isPrepared) {
            Toast.makeText(mContext, " 请等待...", Toast.LENGTH_SHORT).show();
        } else {
            iv_voice.setImageResource(R.drawable.audio_voice_receive);
            animation = (AnimationDrawable) iv_voice.getDrawable();
            animation.start();
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放语音
     */
    private void stopPlay() {
        if (null != animation && animation.isRunning()) {
            animation.stop();
            animation = null;
            iv_voice.setImageResource(R.drawable.icon_voice_player_3);
        }
        try {
            if (null != mediaPlayer) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 销毁播放器
     */
    public void destroy() {
        try {
            if (null != mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        int length = mediaPlayer.getDuration() / 1000;
        tv_voice_time.setText(String.format(getContext().getString(R.string.voice_times), length));

        ViewGroup.LayoutParams para = ll_voice.getLayoutParams();
        para.width = 100 + 2 * length;

        if (isWait) {
            startPlay();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != animation && animation.isRunning()) {
            animation.stop();
            animation = null;
            iv_voice.setImageResource(R.drawable.icon_voice_player_3);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (isError) {
            Toast.makeText(mContext, "播放失败！", Toast.LENGTH_SHORT).show();
        } else {
            if (null != animation && animation.isRunning()) {
                animation.stop();
                animation = null;
                iv_voice.setImageResource(R.drawable.icon_voice_player_3);
            }
            mediaPlayer.reset();
            startPlay();
            isError = true;
        }
        return true;
    }

}