//
// Created by Hsj on 2020/6/23.
//

#include "native_encoder_mp3.h"
#include "libmp3lame/lame.h"

static lame_global_flags *lame = nullptr;

/*
 * init
 */
void init(JNIEnv *env, jclass clazz, jint in_sample_rate, jint in_channel,
          jint out_sample_rate, jint out_bitrate, jint quality) {
    if (lame != nullptr) {
        lame_close(lame);
        lame = nullptr;
    }
    lame = lame_init();
    lame_set_in_samplerate(lame, in_sample_rate);
    //input channel stream
    lame_set_num_channels(lame, in_channel);
    lame_set_out_samplerate(lame, out_sample_rate);
    lame_set_brate(lame, out_bitrate);
    lame_set_quality(lame, quality);
    lame_init_params(lame);
}

/*
 * encode
 */
jint encode(JNIEnv *env, jclass clazz, jshortArray buffer_left,
            jshortArray buffer_right, jint samples, jbyteArray mp3buf) {

    jshort *j_buffer_l = env->GetShortArrayElements(buffer_left, JNI_FALSE);
    jshort *j_buffer_r = env->GetShortArrayElements(buffer_right, JNI_FALSE);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, JNI_FALSE);

    int result = lame_encode_buffer(lame, j_buffer_l, j_buffer_r, samples,
                                    (unsigned char *) j_mp3buf, mp3buf_size);

    env->ReleaseShortArrayElements(buffer_left, j_buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_right, j_buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

/*
 * flush
 */
jint flush(JNIEnv *env, jclass clazz, jbyteArray mp3buf) {
    const jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, JNI_FALSE);
    int result = lame_encode_flush(lame, (unsigned char *) j_mp3buf, mp3buf_size);
    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);
    return result;
}

/*
 * close
 */
void close(JNIEnv *env, jclass clazz) {
    if (lame != nullptr) {
        lame_close(lame);
        lame = nullptr;
    }
}

//==================================================================================================

/*
 * Java<->C++ Method
 */
static JNINativeMethod METHODS[] = {
        {"init",   "(IIIII)V",   (void *) init},
        {"encode", "([S[SI[B)I", (void *) encode},
        {"flush",  "([B)I",      (void *) flush},
        {"close",  "()V",        (void *) close},
};

/*
 * JNI_OnLoad
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
    jclass clazz = env->FindClass(CLASS_NAME);
    if (clazz == nullptr) return JNI_ERR;
    jint r = env->RegisterNatives(clazz, METHODS, sizeof(METHODS) / sizeof(JNINativeMethod));
    if (r != JNI_OK) return r;
    return JNI_VERSION_1_6;
}


