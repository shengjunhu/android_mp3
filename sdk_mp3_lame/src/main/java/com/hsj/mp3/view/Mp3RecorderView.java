package com.hsj.mp3.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.WorkerThread;
import com.hsj.mp3.Mp3Recorder;
import com.hsj.mp3.R;
import java.io.File;

/**
 * @Author:hsj
 * @Date:2020-06-24 16:42
 * @Class:Mp3RecorderView
 * @Desc:
 */
public final class Mp3RecorderView extends LinearLayout implements Mp3Recorder.Mp3RecordCallback{

    private ImageView iv_record_mic;
    private TextView tv_record_time;
    private Mp3Recorder mp3Recorder;
    private Handler UIHandler;

    public Mp3RecorderView(Context context) {
        this(context, null);
    }

    public Mp3RecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Mp3RecorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_mp3_recorder, this);
        iv_record_mic = findViewById(R.id.iv_record_mic);
        tv_record_time = findViewById(R.id.tv_record_time);
        iv_record_mic.setOnClickListener(v -> {
            if (mp3Recorder.isRecording()) {
                mp3Recorder.start();
            } else {
                mp3Recorder.stop();
            }
        });

        this.UIHandler = new Handler(Looper.getMainLooper());
        this.mp3Recorder = new Mp3Recorder(context,this);
    }

    public void close() {
        if (mp3Recorder != null) {
            mp3Recorder.stop();
        }
    }

    private String toSecondFormat(long time) {
        long min = time / 60;    //分钟
        time = time % 60;        //秒
        if (min < 10) {         //分钟补0
            if (time < 10) {    //秒补0
                return "0" + min + ":0" + time;
            } else {
                return "0" + min + ":" + time;
            }
        } else {
            if (time < 10) {    //秒补0
                return min + ":0" + time;
            } else {
                return min + ":" + time;
            }
        }
    }

    @Override
    public void onStatus(int status) {

    }

    @WorkerThread
    @Override
    public void onUpdate(int volume, long recordTime) {
        String time = toSecondFormat(recordTime);
        UIHandler.post(()->{
            tv_record_time.setText(time);
            iv_record_mic.getDrawable().setLevel(10 * volume);
        });
    }

    @WorkerThread
    @Override
    public void onSuccess(File recordFile) {

    }
}