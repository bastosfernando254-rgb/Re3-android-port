#include <jni.h>
#include <android/log.h>
#include <atomic>
#include <cstdint>

#define LOG_TAG "RE3Android"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static std::atomic<uint16_t> g_button_mask{0};

extern "C" JNIEXPORT jstring JNICALL
Java_com_re3_android_MainActivity_nativeStatus(JNIEnv* env, jobject) {
    return env->NewStringUTF(
        "Android ARM64 shell ativo. Runtime PSXRecomp ainda nao integrado."
    );
}

extern "C" JNIEXPORT void JNICALL
Java_com_re3_android_MainActivity_nativeSetButtons(JNIEnv*, jobject, jint mask) {
    g_button_mask.store(static_cast<uint16_t>(mask & 0xFFFF), std::memory_order_relaxed);
}

// O loop do jogo deverá consultar este estado ao integrar o runtime.
// Mantido exportado para facilitar a ligação inicial com o motor.
extern "C" uint16_t re3_android_get_button_mask() {
    return g_button_mask.load(std::memory_order_relaxed);
}
