package com.hsj.mp3.core;

import android.media.AudioFormat;

/**
 * @Author:hsj
 * @Date:2020-06-19
 * @Class:PCMFormat
 * @Desc:
 */
public enum PCMFormat {

    PCM_8BIT (1, AudioFormat.ENCODING_PCM_8BIT),
    PCM_16BIT (2, AudioFormat.ENCODING_PCM_16BIT);

    private int bytesPerFrame;
    private int audioFormat;

    PCMFormat(int bytesPerFrame, int audioFormat) {
        this.bytesPerFrame = bytesPerFrame;
        this.audioFormat = audioFormat;
    }

    public int getBytesPerFrame() {
        return bytesPerFrame;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

}
