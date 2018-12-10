#include "jni/nativeclass.h"

#include <stdlib.h>
#include <stdarg.h>
#include <jni.h>

#ifdef MT_TRACE
	#ifdef __ANDROID__
		#ifdef ALL_TRACE
			#define LOGV(...) sprintf(trace_buffer, __VA_ARGS__); strcat(trace_log, trace_buffer); strcat(trace_log, "\n");
		#else
			#include <android/log.h>
			#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "NativeClass", __VA_ARGS__)
		#endif
	#else
		#define LOGV(...)   printf(__VA_ARGS__)
	#endif
#else
	#define LOGV(...)
#endif

#define CALLOBJECT(X, M) va_list args; va_start(args, signature); X o = this->env->M(object, method, args);               va_end(args); return o
#define CALLOBJECTVOID() va_list args; va_start(args, signature);       this->env->CallVoidMethodV(object, method, args); va_end(args)
#define CALLCLASS(X, M)  va_list args; va_start(args, signature); X o = this->env->M(this->javaClass, method, args);      va_end(args); return o


NativeClass::NativeClass(JNIEnv *env, const char *className) {
	this->env    = env;
	this->javaClass = env->FindClass(className);
	this->className = strdup(className);
}

NativeClass::~NativeClass() {
	free(this->className);
	env->DeleteLocalRef(this->javaClass);
}

jmethodID NativeClass::findStaticMethod(const char *methodName, const char *signature) {
	jmethodID methodId = this->env->GetStaticMethodID(this->javaClass, methodName, signature);
	if (env->ExceptionCheck()) {
		LOGV("static method not found %s::%s %s", this->className, methodName, signature);
		return 0;
	}
	LOGV("call method %s::%s %s", this->className, methodName, signature);
	return methodId;
}

jmethodID NativeClass::findMethod(const char *methodName, const char *signature) {
	jmethodID methodId = this->env->GetMethodID(this->javaClass, methodName, signature);
	if (env->ExceptionCheck()) {
		LOGV("method not found %s::%s %s", this->className, methodName, signature);
		return 0;
	}
	LOGV("call method %s::%s %s", this->className, methodName, signature);
	return methodId;
}

jobject NativeClass::create(const char *signature, ...) {
	jmethodID method = this->findMethod("<init>", signature);
	if (!method) return NULL;

	CALLCLASS(jobject, NewObjectV);
}

jobject NativeClass::callStaticObjectMethod(const char *methodName, const char *signature, ...) {
	jmethodID method = this->findStaticMethod(methodName, signature);
	if (!method) return NULL;

	CALLCLASS(jobject, CallStaticObjectMethodV);
}

jobject NativeClass::callObjectMethod(jobject object, const char *methodName, const char *signature, ...) {
	jmethodID method = this->findMethod(methodName, signature);
	if (!method) return NULL;

	CALLOBJECT(jobject, CallObjectMethodV);
}

jboolean NativeClass::callBooleanMethod(jobject object, const char *methodName, const char *signature, ...) {
	jmethodID method = this->findMethod(methodName, signature);
	if (!method) return false;

	CALLOBJECT(jboolean, CallBooleanMethodV);
}

jint NativeClass::callIntMethod(jobject object, const char *methodName, const char *signature, ...) {
	jmethodID method = this->findMethod(methodName, signature);
	if (!method) return 0;

	CALLOBJECT(jint, CallIntMethodV);
}

void NativeClass::callVoidMethod(jobject object, const char *methodName, const char *signature, ...) {
	jmethodID method = this->findMethod(methodName, signature);
	if (!method) return;

	CALLOBJECTVOID();
}

jfieldID NativeClass::getFieldId(const char *fieldName, const char *fieldType) {
	jfieldID fieldID = this->env->GetFieldID(this->javaClass, fieldName, fieldType);
	if (env->ExceptionCheck()) {
		LOGV("field not found %s::%s %s", this->className, fieldName, fieldType);
		return 0;
	}
	LOGV("get field %s::%s %s", this->className, fieldName, fieldType);
	return fieldID;
}

jobject NativeClass::getObjectField(jobject object, const char *fieldName, const char *fieldType) {
	jfieldID fieldID = this->getFieldId(fieldName, fieldType);
	if (!fieldID) return NULL;

	return this->env->GetObjectField(object, fieldID);
}

void NativeClass::dumpObject(JNIEnv *env, const char *name, jobject object) {
	if (!object) {
		LOGV("DUMP object %s NULL", name);
		return;
	}

	NativeClass objectClass(env, "java/lang/Object");
	jobject oClass  = objectClass.callObjectMethod(object, "getClass", "()Ljava/lang/Class;");
	jstring oString = (jstring)objectClass.callObjectMethod(object, "toString", "()Ljava/lang/String;");

	NativeClass classClass(env, "java/lang/Class");
	jstring oClassName = (jstring)classClass.callObjectMethod(oClass, "getName", "()Ljava/lang/String;");

	const char* className = env->GetStringUTFChars(oClassName,   0);
	const char* string    = env->GetStringUTFChars(oString,   0);

	LOGV("DUMP object %s class:%s, hash:%s", name, className, string);

	env->ReleaseStringUTFChars(oClassName,   className);
	env->ReleaseStringUTFChars(oString,   string);
	env->DeleteLocalRef(oClassName);
	env->DeleteLocalRef(oString);
	env->DeleteLocalRef(oClass);

}
