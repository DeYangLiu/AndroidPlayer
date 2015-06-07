# Android AV Player using ffmpeg and SDL2

history:
v2.6 -- 
 * ffmpeg and sdl2 : sync to newest source code.
 * History.java : add external editable source "/sdcard/ffplay/history.txt"
 * ffplay2.c : force set to screen size when video size too large (e.g. Cammera Photos).

## depends
ffmpeg-2.5+, SDL2.0.3+, android 2.3.3+(sdk>=10).

# patch and patches under jni/

# general instructions
for convience, put this line in your .bashrc:
export PATH=$PATH:/mnt/android-ndk-r10d:/mnt/android-sdk-linux/tools:/mnt/android-sdk-linux/platform-tools

do symbol links at first time or configs are changed:
./bootstrap.sh /mnt/OpenSource/ffmpeg /mnt/OpenSource/SDL
ndk-build clean
rm -rf jni/ffmpeg/android
ant clean

compile native code:
ndk-build -j8 2>&1 | tee build.log

prepare project at first time:
android update project --path . --target android-21

generate apk:
ant debug

install to phone:
ant debug install

# ndk debug
* ndk-build NDK_DEBUG=1
* ant debug install
* (optional) adb push ./gdbserver
* ndk-gdb --start
* b SDL_main (choose yes on shared library)
* continue (means run on arm)

# debug gateway
adb logcat -s gw

# jdk debug
Using jdb with adb (no ADT)
