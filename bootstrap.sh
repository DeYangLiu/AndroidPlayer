#!/bin/sh
if [ $# -lt 2 ] ; then
   echo "usage: ./bootstrap.sh ffmpeg_src_path sdl2_src_path"	
   exit -1
fi

ffmpeg_src=$1
sdl_src=$2

mkdir -p jni
ln -sf $ffmpegdir jni/ffmpeg
ln -f makefiles/Android_configure.mk jni/ffmpeg/
ln -f makefiles/Android_.mk jni/ffmpeg/
ln -f makefiles/Android.mk jni/ffmpeg/
ln -sf $sdldir  jni/SDL

#echo 'include $(call all-subdir-makefiles)' > jni/Android.mk
#LIBS="avcodec avdevice  avfilter  avformat  avresample  avutil  swresample  swscale"

#echo "APP_MODULES := $LIBS" > jni/Application.mk
#echo "APP_MODULES += SDL2 main" >> jni/Application.mk
#echo "APP_ABI := armeabi-v7a " >> jni/Application.mk
#echo "APP_PLATFORM := android-9" >> jni/Application.mk
