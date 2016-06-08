#include <jni.h>

const int size = 1;

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_emilia_mouthdetection_NeuralNetwork_classify(JNIEnv *env, jobject instance, jshortArray bitmap) {
    float result[size];

    result[0] = 9;

    jfloatArray jArray = env->NewFloatArray(size);
    env->SetFloatArrayRegion(jArray, 0, size, result);
    return jArray;
}