LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := main 
LOCAL_SRC_FILES := jni.c SDL_android_main.c ffplay2.c

#depends
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../SDL/include

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../ffmpeg \
					$(LOCAL_PATH)/../ffmpeg/android/$(TARGET_ARCH_ABI)

LOCAL_SHARED_LIBRARIES += avcodec \
						  avdevice \
						  avfilter \
						  avformat \
						  avresample \
						  avutil \
						  swresample \
						  swscale SDL2 

LOCAL_LDLIBS += -lGLESv1_CM -lGLESv2 -llog

#finally
include $(BUILD_SHARED_LIBRARY)
