/*
 * jni/java.cpp - Java specific port code - C<->Java interface
 *
 * Copyright (c) 2001-2002 Jacek Poplawski (original atari_sdl.c)
 * Copyright (C) 2001-2008 Atari800 development team (see DOC/CREDITS)
 * Copyright (c) 2018      Franco Catrin (java port)
 *
 * This file is part of the Atari800 emulator project which emulates
 * the Atari 400, 800, 800XL, 130XE, and 5200 8-bit computers.

 * Atari800 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Atari800 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with Atari800; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#include <jni.h>
#include "atari.h"
#include "trace.h"
#include "nativeobject.h"
#include "java.h"
#include "main.h"
#include "monitor.h"
#include "atari800_NativeInterface.h"

JavaVM* vm = NULL;
jobject nativeClient;
JavaObject *client;

enum Methods {
	M_INIT_PALETTE,
	M_INIT_GRAPHICS,
	M_DISPLAY_SCREEN,
	M_GET_KB_HITS,
	M_POLL_KEY_EVENT,
	M_GET_WINDOW_CLOSED,
	M_SLEEP,
	M_INIT_SOUND,
	M_SOUND_EXIT,
	M_SOUND_AVAILABLE,
	M_SOUND_WRITE,
	M_SOUND_PAUSE,
	M_SOUND_CONTINUE,
	M_CHECK_THREAD_STATUS
};

JavaObjectMethod methods[] = {
		{ M_INIT_PALETTE, "initPalette", "([I)V" },
		{ M_INIT_GRAPHICS, "initGraphics", "(IIIIII)V" },
		{ M_DISPLAY_SCREEN, "displayScreen", "([B)V"},
		{ M_GET_KB_HITS, "getKbHits", "(II)I"},
		{ M_POLL_KEY_EVENT, "pollKeyEvent", "()[I"},
		{ M_GET_WINDOW_CLOSED, "getWindowClosed", "()Z"},
		{ M_SLEEP, "sleep", "(J)V"},
		{ M_INIT_SOUND, "initSound", "(IIIZZI)I"},
		{ M_SOUND_EXIT, "soundExit", "()V"},
		{ M_SOUND_AVAILABLE, "soundAvailable", "()I"},
		{ M_SOUND_WRITE, "soundWrite", "([BI)I"},
		{ M_SOUND_PAUSE, "soundPause", "()V"},
		{ M_SOUND_CONTINUE, "soundContinue", "()V"},
		{ M_CHECK_THREAD_STATUS, "checkThreadStatus", "()I"}
};

#define N_METHODS 14

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

JNIEXPORT void JNICALL Java_atari800_NativeInterface_init
  (JNIEnv *env, jclass _class, jobject javaclient) {
	env->GetJavaVM(&vm);
	nativeClient = (jobject)env->NewGlobalRef(javaclient);

	client = new JavaObject(nativeClient);
	client->registerMethods(env, methods, N_METHODS);
}

JNIEXPORT void JNICALL Java_atari800_NativeInterface_main
  (JNIEnv *env, jclass _class, jobjectArray args) {

	int argc = env->GetArrayLength(args);

	char *argv[argc];

	for (int i=0; i<argc; i++) {
		jstring string = (jstring) (env->GetObjectArrayElement(args, i));
		argv[i] = (char *)env->GetStringUTFChars(string, 0);
	}

	main(argc, argv);

	for (int i=0; i<argc; i++) {
		jstring string = (jstring) (env->GetObjectArrayElement(args, i));
		env->ReleaseStringUTFChars(string, argv[i]);
	}

}

JNIEXPORT jbyteArray JNICALL Java_atari800_NativeInterface_getMemory
  (JNIEnv *env, jclass _class) {

	return newByteArray(env, MONITOR_get_memory(), 0x10000);
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

extern "C" void JAVA_DisplayScreen(UBYTE screen[], int size) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jbyteArray array = newByteArray(env, screen, size);
	if (array != NULL) {
		client->callVoidMethod(env, M_DISPLAY_SCREEN, array);
		env->DeleteLocalRef(array);
	}
	vm->DetachCurrentThread();
}

extern "C" int JAVA_Kbhits(int key, int loc) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = client->callIntMethod(env, M_GET_KB_HITS, key, loc);

	vm->DetachCurrentThread();

	return result;
}

extern "C" int JAVA_PollKeyEvent(int atari_event[]) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jintArray result = (jintArray)client->callObjectMethod(env, M_POLL_KEY_EVENT);
	if (result) {
		jint *event = (jint *)env->GetIntArrayElements(result, NULL);
		for(int i=0; i<4; i++) {
			atari_event[i] = event[i];
		}
		env->ReleaseIntArrayElements(result, event, 0 );
	}

	env->DeleteLocalRef(result);

	vm->DetachCurrentThread();
	return result != NULL;
}

extern "C" int JAVA_GetWindowClosed() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = client->callBooleanMethod(env, M_GET_WINDOW_CLOSED);

	vm->DetachCurrentThread();
	return result;
}

extern "C" void JAVA_Sleep(long msec) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	client->callVoidMethod(env, M_SLEEP, msec);

	vm->DetachCurrentThread();
}

extern "C" void JAVA_InitGraphics(
		int scaleh, int scalew,
		int atari_width, int atari_height,
		int atari_visible_width,
		int atari_left_margin) {

	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	client->callVoidMethod(env, M_INIT_GRAPHICS,
			scaleh, scalew,
			atari_width, atari_height,
			atari_visible_width,
			atari_left_margin
	);
	vm->DetachCurrentThread();
}

extern "C" int JAVA_InitSound(
		int sampleRate, int bitsPerSample, int channels,
		int isSigned, int bigEndian,
		int bufferSize) {

	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = client->callIntMethod(env, M_INIT_SOUND,
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

	client->callVoidMethod(env, M_SOUND_EXIT);

	vm->DetachCurrentThread();
}

extern "C" int JAVA_SoundAvailable() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = client->callIntMethod(env, M_SOUND_AVAILABLE);

	vm->DetachCurrentThread();
	return result;
}

extern "C" int JAVA_SoundWrite(UBYTE const buffer[], unsigned int len) {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	jbyteArray samples = newByteArray(env, buffer, len);
	int result = client->callIntMethod(env, M_SOUND_WRITE, samples, len);

	env->DeleteLocalRef(samples);

	vm->DetachCurrentThread();
	return result;
}

extern "C" void JAVA_SoundPause() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	client->callVoidMethod(env, M_SOUND_PAUSE);

	vm->DetachCurrentThread();
}

extern "C" void JAVA_SoundContinue() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	client->callVoidMethod(env, M_SOUND_CONTINUE);

	vm->DetachCurrentThread();
}

extern "C" int JAVA_CheckThreadStatus() {
	JNIEnv *env;
	vm->AttachCurrentThread((void **)&env, NULL);

	int result = client->callIntMethod(env, M_CHECK_THREAD_STATUS);

	vm->DetachCurrentThread();
	return result;
}

jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
	return JNI_VERSION_1_2;
}


