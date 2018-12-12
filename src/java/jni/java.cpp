#include <jni.h>
#include "atari.h"
#include "trace.h"
#include "nativeclass.h"
#include "java.h"
#include "main.h"
#include "atari800_NativeInterface.h"

#define ATARI_800_NATIVE_CLIENT_CLASS "atari800/Atari800"

JavaVM* vm = NULL;
jobject nativeClient;
JavaObject *client;

enum Methods {
	M_INIT_PALETTE,
	M_INIT_GRAPHICS
};

JavaObjectMethod methods[] = {
		{ M_INIT_PALETTE, "initPalette", "([I)V" },
		{ M_INIT_GRAPHICS, "initGraphics", "(IIIIII)V" }
};

#define N_METHODS 2


JNIEXPORT void JNICALL Java_atari800_NativeInterface_init
  (JNIEnv *env, jclass _class, jobject javaclient) {
	env->GetJavaVM(&vm);
	nativeClient = (jobject)env->NewGlobalRef(javaclient);

	client = new JavaObject(nativeClient);
	client->registerMethods(env, methods, N_METHODS);
}

char *args[] = {"atari800"};

JNIEXPORT void JNICALL Java_atari800_NativeInterface_main
  (JNIEnv *env, jclass _class) {
	main(1, args);
}

static jintArray newIntArray(JNIEnv *env, int src[], int size) {
	jintArray array = env->NewIntArray(size);
	if (array == NULL) {
		return NULL; /* out of memory error thrown */
	}

	jint fill[size];
	for (int i = 0; i < size; i++) {
		fill[i] = src[i];
	}

	env->SetIntArrayRegion(array, 0, size, fill);
	return array;
}

static jbyteArray newByteArray(JNIEnv *env, UBYTE const src[], int size) {
	jbyteArray array = env->NewByteArray(size);
	if (array == NULL) {
		return NULL; /* out of memory error thrown */
	}

	jbyte fill[size];
	for (int i = 0; i < size; i++) {
		fill[i] = src[i];
	}

	env->SetByteArrayRegion(array, 0, size, fill);
	return array;
}

extern "C" void JAVA_InitPalette(int colors[], int size) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jintArray array = newIntArray(env, colors, size);
	if (array != NULL) {
		client->callVoidMethod(env, M_INIT_PALETTE, array);
	}
	vm->DetachCurrentThread();
}

extern "C" void JAVA_DisplayScreen(unsigned int screen[], int size) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jintArray array = newIntArray(env, (int *)screen, size);
	if (array != NULL) {
		NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
		nativeClientClass.callVoidMethod(nativeClient, "displayScreen", "([I)V", array);
	}
	vm->DetachCurrentThread();
}

extern "C" int JAVA_Kbhits(int key, int loc) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callIntMethod(nativeClient, "getKbhits", "(II)I", key, loc);

	vm->DetachCurrentThread();

	return result;
}

extern "C" int JAVA_PollKeyEvent(int atari_event[]) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = 0;

	jintArray array = newIntArray(env, atari_event, 4);
	if (array != NULL) {
		NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
		result = nativeClientClass.callIntMethod(nativeClient, "pollKeyEvent", "([I)I", atari_event);

		jint *event = (jint *)env->GetIntArrayElements(array, NULL);
		for(int i=0; i<4; i++) {
			atari_event[i] = event[i];
		}
		env->ReleaseIntArrayElements(array, event, 0 );
	}
	vm->DetachCurrentThread();
	return result;
}

extern "C" int JAVA_GetWindowClosed() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callBooleanMethod(nativeClient, "getWindowClosed", "()Z");

	vm->DetachCurrentThread();
	return result;
}

extern "C" void JAVA_Sleep(long msec) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	nativeClientClass.callVoidMethod(nativeClient, "sleep", "(J)V");

	vm->DetachCurrentThread();
}

extern "C" void JAVA_InitGraphics(
		int scaleh, int scalew,
		int atari_width, int atari_height,
		int atari_visible_width,
		int atari_left_margin) {

	JNIEnv *env;
	LOGV("JAVA_InitGraphics start");
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass::dumpObject(env, "Atari800_2", nativeClient);

	client->callVoidMethod(env, M_INIT_GRAPHICS,
			scaleh, scalew,
			atari_width, atari_height,
			atari_visible_width,
			atari_left_margin
	);
	vm->DetachCurrentThread();
	LOGV("JAVA_InitGraphics done");
}

extern "C" int JAVA_InitSound(
		int sampleRate, int bitsPerSample, int channels,
		int isSigned, int bigEndian,
		int bufferSize) {

	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callIntMethod(nativeClient, "initSound", "(IIIZZI)I",
			sampleRate, bitsPerSample, channels,
			isSigned, bigEndian,
			bufferSize
	);
	vm->DetachCurrentThread();
	return result;
}

extern "C" void JAVA_SoundExit() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	nativeClientClass.callVoidMethod(nativeClient, "soundExit", "()V");

	vm->DetachCurrentThread();
}

extern "C" int JAVA_SoundAvailable() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callIntMethod(nativeClient, "soundAvailable", "()I");

	vm->DetachCurrentThread();
	return result;
}

extern "C" int JAVA_SoundWrite(UBYTE const buffer[], unsigned int len) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jbyteArray samples = newByteArray(env, buffer, len);
	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callIntMethod(nativeClient, "soudWrite", "([BI)I", samples, len);

	vm->DetachCurrentThread();
	return result;
}

extern "C" void JAVA_SoundPause() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	nativeClientClass.callVoidMethod(nativeClient, "soundPause", "()V");

	vm->DetachCurrentThread();
}

extern "C" void JAVA_SoundContinue() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	nativeClientClass.callVoidMethod(nativeClient, "soundContinue", "()V");

	vm->DetachCurrentThread();
}

extern "C" int JAVA_CheckThreadStatus() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	NativeClass nativeClientClass = NativeClass(env, ATARI_800_NATIVE_CLIENT_CLASS);
	int result = nativeClientClass.callIntMethod(nativeClient, "checkThreadStatus", "()V");

	vm->DetachCurrentThread();
	return result;
}

jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
	return JNI_VERSION_1_2;
}


