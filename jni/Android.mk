#FFMPEG_COMPILE_STATIC := yes
# You cannot compile both shared and static libraries.
#FFMPEG_COMPILE_SHARED := no
# Enables GPL code in FFmpeg, your application must also be GPL-licensed.
#FFMPEG_COMPILE_GPL := yes
include $(call all-subdir-makefiles)
