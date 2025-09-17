# DSP Implementation Guide

## Overview

This project now includes a comprehensive C++ Digital Signal Processing (DSP) class that implements professional-grade audio equalization algorithms. The `DSPProcessor` class provides real-time audio processing capabilities with biquad filters for bass, mid, and treble control.

## Architecture

### DSPProcessor Class

The `DSPProcessor` class is located in:
- Header: `app/src/main/cpp/DSPProcessor.h`
- Implementation: `app/src/main/cpp/DSPProcessor.cpp`

### Key Features

1. **Biquad Filter Implementation**
   - Low-shelf filter for bass control (100Hz)
   - Peak filter for mid control (1000Hz)
   - High-shelf filter for treble control (8000Hz)

2. **Real-time Audio Processing**
   - Mono and stereo audio buffer processing
   - Configurable sample rates
   - Soft limiting to prevent clipping

3. **Professional Audio Algorithms**
   - Butterworth filter design
   - Proper coefficient normalization
   - Gain control from -12dB to +12dB

## Usage

### From Kotlin/Java

The DSP functionality is exposed through JNI methods in `MainActivity.kt`:

```kotlin
// Basic equalizer control
setEqualizerEnabledNative(true)
setBassLevelNative(75)      // 0-100 range
setMidLevelNative(50)       // 0-100 range
setTrebleLevelNative(25)    // 0-100 range

// Audio processing
val inputBuffer = FloatArray(1024)
val outputBuffer = FloatArray(1024)
processAudioBufferNative(inputBuffer, outputBuffer, 1024, 44100)

// Status queries
val isEnabled = isEqualizerEnabledNative()
val bassLevel = getBassLevelNative()
```

### From C++

```cpp
#include "DSPProcessor.h"

// Create DSP processor instance
DSPProcessor dsp;

// Configure equalizer
dsp.setEqualizerEnabled(true);
dsp.setBassLevel(75);
dsp.setMidLevel(50);
dsp.setTrebleLevel(25);

// Process audio
float input[1024];
float output[1024];
dsp.processAudioBuffer(input, output, 1024, 44100);
```

## Filter Specifications

### Bass Filter (Low-Shelf)
- **Frequency**: 100Hz
- **Type**: Low-shelf
- **Q Factor**: 0.707 (Butterworth)
- **Gain Range**: -12dB to +12dB

### Mid Filter (Peak)
- **Frequency**: 1000Hz
- **Type**: Peak/Parametric
- **Q Factor**: 0.707 (Butterworth)
- **Gain Range**: -12dB to +12dB

### Treble Filter (High-Shelf)
- **Frequency**: 8000Hz
- **Type**: High-shelf
- **Q Factor**: 0.707 (Butterworth)
- **Gain Range**: -12dB to +12dB

## Technical Details

### Biquad Filter Structure

Each filter uses a standard biquad structure:
```
y[n] = b0*x[n] + b1*x[n-1] + b2*x[n-2] - a1*y[n-1] - a2*y[n-2]
```

### Coefficient Calculation

The filters use industry-standard coefficient calculation methods:
- **Low-shelf**: Based on RBJ Audio EQ Cookbook
- **Peak**: Standard parametric EQ design
- **High-shelf**: Based on RBJ Audio EQ Cookbook

### Performance Considerations

- **Memory Usage**: Minimal - only stores filter coefficients and history
- **CPU Usage**: Optimized for real-time processing
- **Latency**: Zero-latency processing (no buffering delays)

## Integration with Android Audio

### AudioManager Integration

To integrate with Android's audio system:

```kotlin
// In your audio processing thread
val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toInt() ?: 44100

// Process audio buffers
processAudioBufferNative(inputBuffer, outputBuffer, bufferSize, sampleRate)
```

### AudioTrack Integration

```kotlin
val audioTrack = AudioTrack.Builder()
    .setAudioAttributes(AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build())
    .setAudioFormat(AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setSampleRate(44100)
        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
        .build())
    .build()

// In your audio processing loop
audioTrack.write(inputBuffer, 0, bufferSize, AudioTrack.WRITE_BLOCKING)
```

## Testing and Validation

### Demo Functionality

The app includes a demonstration button that:
1. Generates a 440Hz sine wave
2. Processes it through the DSP
3. Logs the results including RMS values
4. Shows current equalizer settings

### Validation Methods

```kotlin
// Test with known signals
private fun testDSPWithSineWave() {
    val frequency = 440.0
    val sampleRate = 44100
    val duration = 1.0
    val numSamples = (sampleRate * duration).toInt()
    
    val input = FloatArray(numSamples)
    val output = FloatArray(numSamples)
    
    // Generate test signal
    for (i in 0 until numSamples) {
        input[i] = (0.3 * Math.sin(2 * Math.PI * frequency * i / sampleRate)).toFloat()
    }
    
    // Process and analyze
    processAudioBufferNative(input, output, numSamples, sampleRate)
    
    // Calculate and compare RMS
    val inputRMS = calculateRMS(input)
    val outputRMS = calculateRMS(output)
    
    Log.d("DSP_TEST", "Input RMS: $inputRMS, Output RMS: $outputRMS")
}
```

## Advanced Usage

### Custom Filter Types

You can extend the DSP class to support additional filter types:

```cpp
// Add to DSPProcessor.h
enum FilterType {
    LOW_SHELF = 0,
    PEAK = 1,
    HIGH_SHELF = 2,
    LOW_PASS = 3,
    HIGH_PASS = 4,
    BAND_PASS = 5
};

void setFilterType(FilterType type);
```

### Multi-band Equalizer

For more sophisticated equalization:

```cpp
class MultiBandEqualizer {
private:
    std::vector<DSPProcessor> bands;
    
public:
    void addBand(float frequency, float gain, float Q);
    void processAudio(float* input, float* output, int numSamples);
};
```

## Performance Optimization

### SIMD Instructions

For maximum performance, consider using SIMD instructions:

```cpp
#include <arm_neon.h>

// Process 4 samples at once
void processAudioBufferSIMD(float* input, float* output, int numSamples) {
    for (int i = 0; i < numSamples; i += 4) {
        float32x4_t input_vec = vld1q_f32(&input[i]);
        // Process with NEON instructions
        vst1q_f32(&output[i], processed_vec);
    }
}
```

### Memory Alignment

Ensure proper memory alignment for optimal performance:

```cpp
// Align buffers to 16-byte boundaries
float* alignedBuffer = (float*)aligned_alloc(16, numSamples * sizeof(float));
```

## Troubleshooting

### Common Issues

1. **Audio Artifacts**: Check for clipping and ensure proper gain staging
2. **Performance Issues**: Profile with Android Studio Profiler
3. **Memory Leaks**: Use AddressSanitizer for debugging

### Debug Logging

Enable detailed logging:

```cpp
#define DSP_DEBUG 1

#if DSP_DEBUG
    LOGD("Processing %d samples at %d Hz", numSamples, sampleRate);
    LOGD("Bass: %d, Mid: %d, Treble: %d", bassLevel, midLevel, trebleLevel);
#endif
```

## Future Enhancements

1. **Dynamic Range Compression**
2. **Reverb and Delay Effects**
3. **Spectral Analysis**
4. **Machine Learning-based EQ**
5. **Real-time Frequency Response Display**

## References

- RBJ Audio EQ Cookbook
- Digital Signal Processing by Oppenheim & Schafer
- Real-Time Digital Signal Processing by Kehtarnavaz
- Android Audio API Documentation
