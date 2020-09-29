-keep class org.fmod.* {*;}
-dontwarn org.fmod.**

-keepclasseswithmembernames class * {
    native <methods>;
}