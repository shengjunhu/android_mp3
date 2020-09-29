-keep class com.hsj.mp3.* {*;}
-dontwarn com.hsj.mp3.**

-keepclasseswithmembernames class * {
    native <methods>;
}