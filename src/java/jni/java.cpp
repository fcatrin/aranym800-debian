#include <jni.h>
#include "nativeclass.h"
#include "java.h"
#include "atari800_NativeInterface.h"

#define ATARI_800_NATIVE_CLIENT_CLASS "atari800/NativeClient"

JNIEnv *vm;
jobject nativeClient;

JNIEXPORT void JNICALL Java_atari800_NativeInterface_setNativeClient
  (JNIEnv *env, jclass _class, jobject client) {
	vm = env;
	nativeClient = client;
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
	NativeClass nativeClientClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);

	jintArray array = newIntArray(colors, size);
	if (array != NULL) {
		nativeClientClass.callVoidMethod(nativeClient, "initPalette", "([I)V", array);
	}
}

extern "C" void JAVA_DisplayScreen(unsigned int screen[], int size) {
	NativeClass nativeClientClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);

	jintArray array = newIntArray((int *)screen, size);
	if (array != NULL) {
		nativeClientClass.callVoidMethod(nativeClient, "displayScreen", "([I)V", array);
	}
}

extern "C" int JAVA_Kbhits(int key, int loc) {
	NativeClass nativeClientClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);
	return nativeClientClass.callIntMethod(nativeClient, "getKbhits", "(II)I", key, loc);
}

extern "C" int JAVA_PollKeyEvent(int atari_event[]) {
	NativeClass nativeClientClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);
	jintArray array = newIntArray(atari_event, 4);
	if (array != NULL) {
		int result = nativeClientClass.callIntMethod(nativeClient, "pollKeyEvent", "([I)I", atari_event);

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
	NativeClass nativeClientClass(vm, ATARI_800_NATIVE_CLIENT_CLASS);
	return nativeClientClass.callBooleanMethod(nativeClient, "getWindowClosed", "()Z");
}
