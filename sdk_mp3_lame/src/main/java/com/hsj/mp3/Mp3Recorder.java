package com.hsj.mp3;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.hsj.mp3.core.Mp3EncodeThread;
import com.hsj.mp3.core.Mp3Encoder;
import com.hsj.mp3.core.PCMFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Author:hsj
 * @Date:2020-06-19
 * @Class:MP3Recorder
 * @Desc:
 */
public final class Mp3Recorder {

//================================AudioRecord Default Settings======================================

    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;

//=====================================Encoder Default Settings=====================================

    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;

    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

//==================================================================================================

    /**
     * 最大录制时间 5min
     */
    private static final int RECORDER_MAX_TIME = 300000;
    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;
    private static final int MAX_VOLUME = 2000;
    private Handler mp3RecorderHandler;
    private AudioRecord mAudioRecord = null;
    private int mBufferSize;
    private short[] mPCMBuffer;
    private Mp3EncodeThread mEncodeThread;
    private volatile boolean isRecording;
    private File recordFile;
    private Mp3RecordCallback mp3RecordCallback;

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param context
     */
    public Mp3Recorder(@NonNull Context context, @NonNull Mp3RecordCallback mp3RecordCallback) {
        this.mp3RecordCallback = mp3RecordCallback;
        File dir = context.getExternalFilesDir("mp3");
        if (dir == null) {
            dir = context.getFilesDir();
            dir = new File(dir, "mp3");
        }
        if (!dir.exists() && !dir.mkdir()) {
            mp3RecordCallback.onStatus(-1);
            return;
        }
        this.recordFile = new File(dir, String.format("%s.mp3", System.currentTimeMillis()));
        HandlerThread mp3RecorderThread = new HandlerThread("mp3_recorder_thread");
        this.mp3RecorderHandler = new Handler(mp3RecorderThread.getLooper());
    }

    public interface Mp3RecordCallback {

        /**
         * 状态：-1创建文件失败
         *
         * @param status
         */
        @WorkerThread
        void onStatus(int status);

        /**
         * 录音中
         *
         * @param volume     当前声音分贝
         * @param recordTime 录音时长
         */
        @WorkerThread
        void onUpdate(int volume, long recordTime);

        /**
         * 录制成功
         *
         * @param recordFile
         */
        @WorkerThread
        void onSuccess(File recordFile);
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     */
    public boolean start() {
        if (isRecording) return false;
        // 提早，防止init或startRecording被多次调用
        isRecording = true;
        boolean isInit = initAudioRecorder();
        mAudioRecord.startRecording();
        mp3RecorderHandler.post(this::onRecord);
        return isInit;
    }

    /**
     * Initialize audio recorder
     */
    private boolean initAudioRecorder() {
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());

        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
        /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
         * 使能被整除，方便下面的周期性通知
         */
        int frameSize = mBufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            mBufferSize = frameSize * bytesPerFrame;
        }

        /* Setup audio recorder */
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT.getAudioFormat(), mBufferSize);

        mPCMBuffer = new short[mBufferSize];
        /*
         * Initialize lame buffer
         * mp3 sampling rate is the same as the recorded pcm sampling rate
         * The bit rate is 32kbps
         */
        Mp3Encoder.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE,
                DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        // Create and run thread used to encode data
        // The thread will
        try {
            mEncodeThread = new Mp3EncodeThread(recordFile, mBufferSize);
        } catch (FileNotFoundException e) {
            return false;
        }
        mEncodeThread.start();
        mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        return true;
    }

    /**
     * recording
     */
    private void onRecord() {
        //设置线程权限
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        long startTime = System.currentTimeMillis();
        while (isRecording) {
            int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
            if (readSize > 0) {
                mEncodeThread.addTask(mPCMBuffer, readSize);
                int volume = calculateRealVolume(mPCMBuffer, readSize);
                long currentTime = System.currentTimeMillis();
                mp3RecordCallback.onUpdate(volume, currentTime - startTime);
                if (currentTime >= RECORDER_MAX_TIME) {
                    isRecording = false;
                    mp3RecordCallback.onSuccess(recordFile);
                    break;
                }
            }
        }
        // release and finalize audioRecord
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        // stop the encoding thread and try to wait
        // until the thread finishes its job
        mEncodeThread.sendStopMessage();
        if (mp3RecorderHandler != null) {
            mp3RecorderHandler.getLooper().quitSafely();
        }
    }

    /**
     * 录制状态
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 结束
     */
    public void stop() {
        isRecording = false;
        mp3RecordCallback.onSuccess(recordFile);
    }

    /**
     * 计算音量
     *
     * @param buffer
     * @param readSize
     */
    private int calculateRealVolume(short[] buffer, int readSize) {
        int mVolume = 0;
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) Math.sqrt(amplitude);
        }
        return Math.min(mVolume, MAX_VOLUME);
    }

}