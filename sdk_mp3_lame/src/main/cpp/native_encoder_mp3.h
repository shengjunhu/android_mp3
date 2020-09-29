//
// Created by Hsj on 2020/9/27.
//

#ifndef ANDROID_SAMPLE_AUDIO_NATIVE_ENCODER_MP3_H
#define ANDROID_SAMPLE_AUDIO_NATIVE_ENCODER_MP3_H

#include <jni.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

//Java类名
#define CLASS_NAME    "com/hsj/mp3/core/Mp3Encoder"

//定义JNI状态码
#define STATUS_SUCCESS              0
#define STATUS_FAILED              -1

//定义JNI日志
#define LOG_TAG "JNI"
#ifdef LOG_TAG
    #define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
    #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...) NULL
    #define LOGE(...) NULL
#endif

//编译器优化
#ifdef __GNUC__
//LIKELY    判断为真的可能性更大
    #define LIKELY(X)   __builtin_expect(!!(X), 1)
    //UNLIKELY  判断为假的可能性更大
    #define UNLIKELY(X) __builtin_expect(!!(X), 0)
#else
    #define LIKELY(X)   (X)
    #define UNLIKELY(X) (X)
#endif

#ifdef __cplusplus
}
#endif

#endif //ANDROID_SAMPLE_AUDIO_NATIVE_ENCODER_MP3_H
