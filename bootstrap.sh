#!/bin/sh
#usage: ffmpeg_src ffmpeg_ver sdl_src

VERSION:=

mkdir -p jni
ln -s $ffmpegdir jni/ffmpeg
ln -s makefiles/Android_configure.mk jni/ffmpeg/
ln -s makefiles/Android_.mk jni/ffmpeg/
ln -s makefiles/Android.mk jni/ffmpeg/
ln -s $sdldir  jni/SDL
rm -rf jni/ffmpeg/android
echo 'include $(call all-subdir-makefiles)' > jni/Android.mk
LIBS=""
for LIB in avcodec avdevice  avfilter  avformat  avresample  avutil  swresample  swscale;
 do LIBS="$LIBS $LIB$VERSION" done

echo "APP_MODULES := $LIBS" > jni/Application.mk
echo "APP_ABI := armeabi-v7a " >> jni/Application.mk
echo "APP_PLATFORM := android-9" >> jni/Application.mk
