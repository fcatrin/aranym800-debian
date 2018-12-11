#include <jni.h>
#include "nativeclass.h"
#include "java.h"
#include "atari800_NativeInterface.h"

#define ATARI_800_NATIVE_CLIENT_CLASS "atari800/NativeClient"

JNIEnv *vm;
jobject nativeClient;
NativeClass *nativeClientClass;

JNIEXPORT void JNICALL Java_atari800_NativeInterface_init
  (JNIEnv *env, jclass _class, jobject client) {
	vm = env;
	nativeClient = client;
	nativeClientClass = new NativeClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);
}

static jintArray newIntArray(int src[], int size) {
	jintArray array = vm->NewIntArray(size);
	if (array == NULL) {
		return NULL; /* out of memory error thrown */
	}

	jint fill[size];
	for (int i = 0; i < size; i++) {
		fill[i] = src[i];
	}

	vm->SetIntArrayRegion(array, 0, size, fill);
	return array;
}

extern "C" void JAVA_InitPalette(int colors[], int size) {
	jintArray array = newIntArray(colors, size);
	if (array != NULL) {
		nativeClientClass->callVoidMethod(nativeClient, "initPalette", "([I)V", array);
	}
}

extern "C" void JAVA_DisplayScreen(unsigned int screen[], int size) {
	jintArray array = newIntArray((int *)screen, size);
	if (array != NULL) {
		nativeClientClass->callVoidMethod(nativeClient, "displayScreen", "([I)V", array);
	}
}

extern "C" int JAVA_Kbhits(int key, int loc) {
	return nativeClientClass->callIntMethod(nativeClient, "getKbhits", "(II)I", key, loc);
}

extern "C" int JAVA_PollKeyEvent(int atari_event[]) {
	jintArray array = newIntArray(atari_event, 4);
	if (array != NULL) {
		int result = nativeClientClass->callIntMethod(nativeClient, "pollKeyEvent", "([I)I", atari_event);

		jint *event = (jint *)vm->GetIntArrayElements(array, NULL);
		for(int i=0; i<4; i++) {
			atari_event[i] = event[i];
		}
		vm->ReleaseIntArrayElements(array, event, 0 );
		return result;
	}
	return 0;
}

extern "C" int JAVA_GetWindowClosed() {
	return nativeClientClass->callBooleanMethod(nativeClient, "getWindowClosed", "()Z");
}

extern "C" void JAVA_Sleep(long msec) {
	nativeClientClass->callVoidMethod(nativeClient, "sleep", "(L)V");
}

extern "C" void JAVA_InitGraphics(
		int scaleh, int scalew,
		int atari_width, int atari_height,
		int atari_visible_width,
		int atari_left_margin) {

	nativeClientClass->callVoidMethod(nativeClient, "initGraphics", "(IIIIII)V",
			scaleh, scalew,
			atari_width, atari_height,
			atari_visible_width,
			atari_left_margin
	);
}

extern "C" int JAVA_InitSound(
		int sampleRate, int bitsPerSample, int channels,
		int isSigned, int bigEndian,
		int bufferSize) {
	return nativeClientClass->callIntMethod(nativeClient, "initSound", "(IIIZZI)I",
			sampleRate, bitsPerSample, channels,
			isSigned, bigEndian,
			bufferSize
	);
}

extern "C" void JAVA_SoundExit() {
	nativeClientClass->callVoidMethod(nativeClient, "SoundExit", "()V");
}
