#include <string.h>
#include <jni.h>

#include "config.h"
#include <inttypes.h>
#include <math.h>
#include <limits.h>
#include <signal.h>
#include <stdint.h>

#include "libavutil/avstring.h"
#include "libavutil/colorspace.h"
#include "libavutil/mathematics.h"
#include "libavutil/pixdesc.h"
#include "libavutil/imgutils.h"
#include "libavutil/dict.h"
#include "libavutil/parseutils.h"
#include "libavutil/samplefmt.h"
#include "libavutil/avassert.h"
#include "libavutil/time.h"
#include "libavformat/avformat.h"
#include "libavdevice/avdevice.h"
#include "libswscale/swscale.h"
#include "libavutil/opt.h"
#include "libavcodec/avfft.h"
#include "libswresample/swresample.h"

#if CONFIG_AVFILTER
# include "libavcodec/avcodec.h"
# include "libavfilter/avfilter.h"
# include "libavfilter/buffersink.h"
# include "libavfilter/buffersrc.h"
#endif

#include "SDL.h"
#include "SDL_revision.h"

jstring Java_org_libsdl_app_SDLActivity_stringFromJNI( JNIEnv* env, jobject thiz)
{
	SDL_version linked;
	unsigned ver = avutil_version();
	char buf[512] = "", *ptr = buf;
	ptr += sprintf(ptr, "avutil %d.%d.%d ", ver>>16, (ver>>8)&0xff, ver&0xff); 

	SDL_GetVersion(&linked);
    ptr += sprintf(ptr, "sdl: %d.%d.%d.%d (%s)",
           linked.major, linked.minor, linked.patch,
           SDL_GetRevisionNumber(), SDL_GetRevision());

    return (*env)->NewStringUTF(env, buf);
}

jint Java_org_libsdl_app_SDLActivity_ffInit(JNIEnv* env, jobject thiz )
{
#if CONFIG_AVDEVICE
    avdevice_register_all();
#endif
#if CONFIG_AVFILTER
    avfilter_register_all();
#endif
    av_register_all();
    avformat_network_init();
}

