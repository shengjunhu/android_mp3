//
// Created by Hsj on 2020/6/23.
//

#include <jni.h>
#include <string>
#include <stdio.h>
#include <android/log.h>
#include "libmp3lame/lame.h"

static lame_global_flags *lame = NULL;

extern "C"
JNIEXPORT void JNICALL
Java_com_hsj_mp3_core_Mp3Encoder_init(JNIEnv *env, jclass clazz, jint in_sample_rate, jint in_channel,
                                 jint out_sample_rate, jint out_bitrate, jint quality) {
    if (lame != NULL) {
        lame_close(lame);
        lame = NULL;
    }
    lame = lame_init();
    lame_set_in_samplerate(lame, in_sample_rate);
    //输入流的声道
    lame_set_num_channels(lame, in_channel);
    lame_set_out_samplerate(lame, out_sample_rate);
    lame_set_brate(lame, out_bitrate);
    lame_set_quality(lame, quality);
    lame_init_params(lame);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_hsj_mp3_core_Mp3Encoder_encode(JNIEnv *env, jclass clazz, jshortArray buffer_left,
                                   jshortArray buffer_right, jint samples, jbyteArray mp3buf) {

    jshort *j_buffer_l = env->GetShortArrayElements(buffer_left, NULL);
    jshort *j_buffer_r = env->GetShortArrayElements(buffer_right, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf,NULL);

    int result = lame_encode_buffer(lame, j_buffer_l, j_buffer_r, samples,
            (unsigned char*)j_mp3buf, mp3buf_size);

    env->ReleaseShortArrayElements( buffer_left, j_buffer_l, 0);
    env->ReleaseShortArrayElements( buffer_right, j_buffer_r, 0);
    env->ReleaseByteArrayElements( mp3buf, j_mp3buf, 0);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_hsj_mp3_core_Mp3Encoder_flush(JNIEnv *env, jclass clazz, jbyteArray mp3buf) {

    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, NULL);

    int result = lame_encode_flush(lame, (unsigned char*)j_mp3buf, mp3buf_size);

    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hsj_mp3_core_Mp3Encoder_close(JNIEnv *env, jclass clazz) {
    lame_close(lame);
    lame = NULL;
}


