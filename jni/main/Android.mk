LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := main 
LOCAL_SRC_FILES := jni.c SDL_android_main.c ffplay2.c cmdutils.c

#depends
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../SDL/include

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../ffmpeg \
					$(LOCAL_PATH)/../ffmpeg/android/$(TARGET_ARCH_ABI)

LOCAL_STATIC_LIBRARIES += avdevice \
						  avfilter \
						  avformat \
						  avcodec \
						  avresample \
						  avutil \
						  swresample \
						  swscale SDL2 

LOCAL_LDLIBS += -lz -lGLESv1_CM -lGLESv2 -llog

#finally
include $(BUILD_SHARED_LIBRARY)
