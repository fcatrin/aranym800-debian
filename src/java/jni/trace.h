#ifndef __JAVA_TRACE__
#define __JAVA_TRACE__

#ifdef JAVA_TRACE
	#ifdef __ANDROID__
		#ifdef ALL_TRACE
			#define LOGV(...) sprintf(trace_buffer, __VA_ARGS__); strcat(trace_log, trace_buffer); strcat(trace_log, "\n");
		#else
			#include <android/log.h>
			#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "Native", __VA_ARGS__)
		#endif
	#else
		#define LOGV(...)   printf(__VA_ARGS__); printf("\n")
	#endif
#else
	#define LOGV(...)
#endif

#endif
