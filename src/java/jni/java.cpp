#include <jni.h>
#include "nativeclass.h"
#include "java.h"
#include "atari800_NativeInterface.h"

JNIEnv *vm;
jobject nativeClient;

JNIEXPORT void JNICALL Java_atari800_NativeInterface_setNativeClient
  (JNIEnv *env, jclass _class, jobject client) {
	vm = env;
	nativeClient = client;
}

extern "C" void JAVA_InitPalette(int colors[]) {
	NativeClass nativeClientClass(vm, "atari800/NativeClient");

	int size = 256;

	jintArray array = vm->NewIntArray(size);
	if (array == NULL) {
		return; /* out of memory error thrown */
	}

	jint fill[size];
	for (int i = 0; i < size; i++) {
		fill[i] = colors[i];
	}

	vm->SetIntArrayRegion( array, 0, size, fill);

	nativeClientClass.callVoidMethod(nativeClient, "initPalette", "([I)V", array);
}
