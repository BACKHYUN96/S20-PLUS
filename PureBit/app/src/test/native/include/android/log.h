#pragma once
// Host-only Android log shim. Never included in the Android APK build.
#define ANDROID_LOG_INFO 4
inline int __android_log_print(int,const char*,const char*,...){return 0;}
