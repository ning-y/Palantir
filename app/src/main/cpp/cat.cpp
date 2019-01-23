#include <fstream>
#include <jni.h>
#include <string>

std::string get_file_contents(std::string);
std::string jstring2string(JNIEnv *, jstring );

JNIEXPORT jstring JNICALL java_io_ningyuan_palantir_utils_Cpp_cat(JNIEnv *env, jobject obj, jstring filepath) {
    std::string file_content = get_file_contents(jstring2string(env, filepath));
    return env->NewStringUTF(file_content.c_str());
}

std::string get_file_contents(std::string abs_path) {
    std::ifstream inFile;
    inFile.open(abs_path);

    if (!inFile) {
        exit(1);
    }

    std::string contents;
    std::string buffer;
    while (std::getline(inFile, buffer)) {
        contents = contents + buffer + "\n";
    }

    inFile.close();
    return contents;
}

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

