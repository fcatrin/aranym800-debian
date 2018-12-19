#ifndef __NATIVE_CLASS__
#define __NATIVE_CLASS__

struct JavaObjectMethod {
	int id;
	const char *name;
	const char *signature;
};

class JavaObject {
	jobject object;
	jmethodID methods[50];

	jmethodID findMethod (JNIEnv *env, jclass _class, const char *methodName, const char *signature);

public:
	JavaObject(jobject object);
	~JavaObject();

	jboolean callBooleanMethod(JNIEnv *env, int methodId, ...);
	jint     callIntMethod    (JNIEnv *env, int methodId, ...);
	void     callVoidMethod   (JNIEnv *env, int methodId, ...);
	jobject  callObjectMethod(JNIEnv *env, int methodId, ...);

	void registerMethods(JNIEnv *env, JavaObjectMethod methods[], int len);
};

#endif
