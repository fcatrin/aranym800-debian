#ifndef __NATIVE_CLASS__
#define __NATIVE_CLASS__

class NativeClass {
private:
	jclass javaClass;
	JNIEnv *env;
	char *className;

	jmethodID findStaticMethod(const char *methodName, const char *signature);
	jmethodID findMethod      (const char *methodName, const char *signature);
	jfieldID  getFieldId      (const char *fieldName,  const char *fieldType);
public:
	NativeClass(JNIEnv *env, const char *className);
	~NativeClass();

	static void dumpObject(JNIEnv *env, const char *name, jobject object);

	void     setEnv(JNIEnv *env);

	jobject  create(const char *signature, ...);
	jobject  callStaticObjectMethod(const char *methodName, const char *signature, ...);
	jobject  callObjectMethod (jobject object, const char *methodName, const char *signature, ...);
	jboolean callBooleanMethod(jobject object, const char *methodName, const char *signature, ...);
	jint     callIntMethod    (jobject object, const char *methodName, const char *signature, ...);
	void     callVoidMethod   (jobject object, const char *methodName, const char *signature, ...);

	jobject  getObjectField(jobject object, const char *fieldName, const char *fieldType);

};

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

	void registerMethods(JNIEnv *env, JavaObjectMethod methods[], int len);
};

#endif
