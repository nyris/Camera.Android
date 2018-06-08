/*Read me
 * This file can't not be removed, it's required for builds */

#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_io_nyris_myapplication_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
