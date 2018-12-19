#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <jni.h>
#include "config.h"
#include "trace.h"
#include "nativeobject.h"

#define OCALLOBJECT(X, M) va_list args; va_start(args, methodId); X o = env->M(object, method, args);               va_end(args); return o
#define OCALLOBJECTVOID() va_list args; va_start(args, methodId);       env->CallVoidMethodV(object, method, args); va_end(args)
#define OCALLCLASS(X, M)  va_list args; va_start(args, methodId); X o = env->M(this->javaClass, method, args);      va_end(args); return o


JavaObject::JavaObject(jobject object) {
	this->object = object;
}

jmethodID JavaObject::findMethod(JNIEnv *env, jclass _class, const char *methodName, const char *signature) {
	jmethodID methodId = env->GetMethodID(_class, methodName, signature);
	if (env->ExceptionCheck()) {
		LOGV("method not found %s %s", methodName, signature);
		return 0;
	}
	LOGV("findMethod method %s %s",methodName, signature);
	return methodId;
}

void JavaObject::registerMethods(JNIEnv *env, JavaObjectMethod methods[], int len) {
	jclass _class = env->GetObjectClass(this->object);
	for(int i=0; i<len; i++) {
		JavaObjectMethod method = methods[i];
		this->methods[method.id] = this->findMethod(env, _class, method.name, method.signature);
	}
}

jboolean JavaObject::callBooleanMethod(JNIEnv *env,int methodId, ...) {
	jmethodID method = this->methods[methodId];
	if (!method) return false;

	OCALLOBJECT(jboolean, CallBooleanMethodV);
}

jint JavaObject::callIntMethod(JNIEnv *env, int methodId, ...) {
	jmethodID method = this->methods[methodId];
	if (!method) {
		return 0;
	}

	OCALLOBJECT(jint, CallIntMethodV);
}

void JavaObject::callVoidMethod(JNIEnv *env, int methodId, ...) {
	jmethodID method = this->methods[methodId];
	if (!method) return;

	OCALLOBJECTVOID();
}

jobject JavaObject::callObjectMethod(JNIEnv *env, int methodId, ...) {
	jmethodID method = this->methods[methodId];
	if (!method) return NULL;

	OCALLOBJECT(jobject, CallObjectMethodV);
}


