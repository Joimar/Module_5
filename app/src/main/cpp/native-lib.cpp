#include <jni.h>
#include <string>
#include <android/log.h>
'#include <memory>
#include "DSPProcessor.h"

// Definindo TAGs para o Logcat, facilitando a filtragem
#define TAG_NATIVE_AUDIO "NativeAudioProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG_NATIVE_AUDIO, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG_NATIVE_AUDIO, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG_NATIVE_AUDIO, __VA_ARGS__)

// Instância global do processador DSP
// Em um sistema real, este seria usado para processar buffers de áudio em tempo real
static std::unique_ptr<DSPProcessor> s_dspProcessor = nullptr;

// Função para inicializar o processador DSP se necessário
static void ensureDSPProcessor() {
    if (!s_dspProcessor) {
        s_dspProcessor = std::make_unique<DSPProcessor>();
        LOGI("DSP Processor inicializado");
    }
}

// Função JNI para ativar/desativar o equalizador
extern "C" JNIEXPORT void JNICALL
        Java_com_example_vehicleequalizernative_MainActivity_setEqualizerEnabledNative(
        JNIEnv* env, jobject /* this */, jboolean enabled) {
    ensureDSPProcessor();
    s_dspProcessor->setEqualizerEnabled(enabled);
    LOGI("Equalizador nativo %s", enabled ? "ativado" : "desativado");
    // Em um cenário real, aqui você chamaria a API da HAL de áudio
    // para habilitar/desabilitar o processamento de equalização no hardware.
}

// Função JNI para definir o nível de graves
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_setBassLevelNative(
        JNIEnv* env, jobject /* this */, jint level) {
    ensureDSPProcessor();
    s_dspProcessor->setBassLevel(level);
    LOGI("Nível de Graves nativo: %d", level);
    // Em um cenário real, aqui você passaria este valor para o algoritmo DSP
    // ou para o hardware de áudio para ajustar a banda de graves.
}

// Função JNI para definir o nível de médios
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_setMidLevelNative(
        JNIEnv* env, jobject /* this */, jint level) {
    ensureDSPProcessor();
    s_dspProcessor->setMidLevel(level);
    LOGI("Nível de Médios nativo: %d", level);
    // Em um cenário real, aqui você passaria este valor para o algoritmo DSP
    // ou para o hardware de áudio para ajustar a banda de médios.
}

// Função JNI para definir o nível de agudos
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_setTrebleLevelNative(JNIEnv* env, jobject /* this */, jint level) {
    ensureDSPProcessor();
    s_dspProcessor->setTrebleLevel(level);
    LOGI("Nível de Agudos nativo: %d", level);
    // Em um cenário real, aqui você passaria este valor para o algoritmo DSP
    // ou para o hardware de áudio para ajustar a banda de agudos.
}

// Função de exemplo original, pode ser mantida ou removida
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_vehicleequalizernative_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string hello = "Hello from C++ Native Audio Processor!";
    return env->NewStringUTF(hello.c_str());
}

// Função JNI para processar buffer de áudio mono
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_processAudioBufferNative(
        JNIEnv* env, jobject /* this */, jfloatArray inputBuffer, jfloatArray outputBuffer, jint numSamples, jint sampleRate) {
    ensureDSPProcessor();
    
    jfloat* input = env->GetFloatArrayElements(inputBuffer, nullptr);
    jfloat* output = env->GetFloatArrayElements(outputBuffer, nullptr);
    
    if (input && output) {
        s_dspProcessor->processAudioBuffer(input, output, numSamples, sampleRate);
    }
    
    env->ReleaseFloatArrayElements(inputBuffer, input, JNI_ABORT);
    env->ReleaseFloatArrayElements(outputBuffer, output, 0);
}

// Função JNI para processar buffer de áudio estéreo
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_processAudioBufferStereoNative(
        JNIEnv* env, jobject /* this */, jfloatArray leftChannel, jfloatArray rightChannel, jint numSamples, jint sampleRate) {
    ensureDSPProcessor();
    
    jfloat* left = env->GetFloatArrayElements(leftChannel, nullptr);
    jfloat* right = env->GetFloatArrayElements(rightChannel, nullptr);
    
    if (left && right) {
        s_dspProcessor->processAudioBufferStereo(left, right, numSamples, sampleRate);
    }
    
    env->ReleaseFloatArrayElements(leftChannel, left, 0);
    env->ReleaseFloatArrayElements(rightChannel, right, 0);
}

// Função JNI para obter status do equalizador
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_vehicleequalizernative_MainActivity_isEqualizerEnabledNative(JNIEnv* env, jobject /* this */) {
    ensureDSPProcessor();
    return s_dspProcessor->isEqualizerEnabled();
}

// Função JNI para obter níveis atuais
extern "C" JNIEXPORT jint JNICALL
Java_com_example_vehicleequalizernative_MainActivity_getBassLevelNative(JNIEnv* env, jobject /* this */) {
    ensureDSPProcessor();
    return s_dspProcessor->getBassLevel();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_vehicleequalizernative_MainActivity_getMidLevelNative(JNIEnv* env, jobject /* this */) {
    ensureDSPProcessor();
    return s_dspProcessor->getMidLevel();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_vehicleequalizernative_MainActivity_getTrebleLevelNative(JNIEnv* env, jobject /* this */) {
    ensureDSPProcessor();
    return s_dspProcessor->getTrebleLevel();
}

// Função JNI para resetar filtros
extern "C" JNIEXPORT void JNICALL
Java_com_example_vehicleequalizernative_MainActivity_resetFiltersNative(JNIEnv* env, jobject /* this */) {
    ensureDSPProcessor();
    s_dspProcessor->resetFilters();
    LOGI("Filtros DSP resetados");
}



