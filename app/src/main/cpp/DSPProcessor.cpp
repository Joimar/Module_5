#include "DSPProcessor.h"
#include <algorithm>
#include <cstring>

DSPProcessor::DSPProcessor() 
    : m_equalizerEnabled(false)
    , m_bassLevel(50)
    , m_midLevel(50)
    , m_trebleLevel(50)
    , m_sampleRate(44100)
    , m_filterType(0) {
    
    // Initialize filters with default parameters
    resetFilters();
}

DSPProcessor::~DSPProcessor() {
    // Cleanup if needed
}

void DSPProcessor::setEqualizerEnabled(bool enabled) {
    m_equalizerEnabled = enabled;
    if (!enabled) {
        // Reset all filters when disabled
        resetFilters();
    }
}

void DSPProcessor::setBassLevel(int level) {
    m_bassLevel = std::clamp(level, 0, 100);
    if (m_equalizerEnabled) {
        float gain = levelToGain(m_bassLevel);
        designLowShelfFilter(m_bassFilter, BASS_FREQUENCY, gain, FILTER_Q, m_sampleRate);
    }
}

void DSPProcessor::setMidLevel(int level) {
    m_midLevel = std::clamp(level, 0, 100);
    if (m_equalizerEnabled) {
        float gain = levelToGain(m_midLevel);
        designPeakFilter(m_midFilter, MID_FREQUENCY, gain, FILTER_Q, m_sampleRate);
    }
}

void DSPProcessor::setTrebleLevel(int level) {
    m_trebleLevel = std::clamp(level, 0, 100);
    if (m_equalizerEnabled) {
        float gain = levelToGain(m_trebleLevel);
        designHighShelfFilter(m_trebleFilter, TREBLE_FREQUENCY, gain, FILTER_Q, m_sampleRate);
    }
}

void DSPProcessor::processAudioBuffer(float* inputBuffer, float* outputBuffer, int numSamples, int sampleRate) {
    if (!inputBuffer || !outputBuffer || numSamples <= 0) {
        return;
    }
    
    // Update sample rate if changed
    if (sampleRate != m_sampleRate) {
        setSampleRate(sampleRate);
    }
    
    if (!m_equalizerEnabled) {
        // If equalizer is disabled, just copy input to output
        std::memcpy(outputBuffer, inputBuffer, numSamples * sizeof(float));
        return;
    }
    
    // Process each sample through the filter chain
    for (int i = 0; i < numSamples; ++i) {
        float sample = inputBuffer[i];
        
        // Apply bass filter (low shelf)
        sample = processBiquadFilter(m_bassFilter, sample);
        
        // Apply mid filter (peak)
        sample = processBiquadFilter(m_midFilter, sample);
        
        // Apply treble filter (high shelf)
        sample = processBiquadFilter(m_trebleFilter, sample);
        
        // Apply soft limiting to prevent clipping
        sample = std::clamp(sample, -1.0f, 1.0f);
        
        outputBuffer[i] = sample;
    }
}

void DSPProcessor::processAudioBufferStereo(float* leftChannel, float* rightChannel, int numSamples, int sampleRate) {
    if (!leftChannel || !rightChannel || numSamples <= 0) {
        return;
    }
    
    // Process left and right channels separately
    processAudioBuffer(leftChannel, leftChannel, numSamples, sampleRate);
    processAudioBuffer(rightChannel, rightChannel, numSamples, sampleRate);
}

void DSPProcessor::setSampleRate(int sampleRate) {
    if (sampleRate > 0 && sampleRate != m_sampleRate) {
        m_sampleRate = sampleRate;
        
        // Redesign all filters with new sample rate
        if (m_equalizerEnabled) {
            setBassLevel(m_bassLevel);
            setMidLevel(m_midLevel);
            setTrebleLevel(m_trebleLevel);
        }
    }
}

void DSPProcessor::setFilterType(int filterType) {
    m_filterType = std::clamp(filterType, 0, 2);
    
    // Redesign filters with new type
    if (m_equalizerEnabled) {
        setBassLevel(m_bassLevel);
        setMidLevel(m_midLevel);
        setTrebleLevel(m_trebleLevel);
    }
}

bool DSPProcessor::isEqualizerEnabled() const {
    return m_equalizerEnabled;
}

int DSPProcessor::getBassLevel() const {
    return m_bassLevel;
}

int DSPProcessor::getMidLevel() const {
    return m_midLevel;
}

int DSPProcessor::getTrebleLevel() const {
    return m_trebleLevel;
}

void DSPProcessor::resetFilters() {
    m_bassFilter.reset();
    m_midFilter.reset();
    m_trebleFilter.reset();
    
    // Set filters to unity gain (no effect)
    m_bassFilter.b0 = 1.0f; m_bassFilter.b1 = 0.0f; m_bassFilter.b2 = 0.0f;
    m_bassFilter.a1 = 0.0f; m_bassFilter.a2 = 0.0f;
    
    m_midFilter.b0 = 1.0f; m_midFilter.b1 = 0.0f; m_midFilter.b2 = 0.0f;
    m_midFilter.a1 = 0.0f; m_midFilter.a2 = 0.0f;
    
    m_trebleFilter.b0 = 1.0f; m_trebleFilter.b1 = 0.0f; m_trebleFilter.b2 = 0.0f;
    m_trebleFilter.a1 = 0.0f; m_trebleFilter.a2 = 0.0f;
}

void DSPProcessor::clearBuffers() {
    m_bassFilter.reset();
    m_midFilter.reset();
    m_trebleFilter.reset();
}

void DSPProcessor::designLowShelfFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate) {
    float w = 2.0f * M_PI * frequency / sampleRate;
    float cosw = std::cos(w);
    float sinw = std::sin(w);
    float A = dBToLinear(gain);
    float S = 1.0f;
    float alpha = sinw / 2.0f * std::sqrt((A + 1.0f / A) * (1.0f / S - 1.0f) + 2.0f);
    
    float b0 = A * ((A + 1.0f) - (A - 1.0f) * cosw + 2.0f * std::sqrt(A) * alpha);
    float b1 = 2.0f * A * ((A - 1.0f) - (A + 1.0f) * cosw);
    float b2 = A * ((A + 1.0f) - (A - 1.0f) * cosw - 2.0f * std::sqrt(A) * alpha);
    float a0 = (A + 1.0f) + (A - 1.0f) * cosw + 2.0f * std::sqrt(A) * alpha;
    float a1 = -2.0f * ((A - 1.0f) + (A + 1.0f) * cosw);
    float a2 = (A + 1.0f) + (A - 1.0f) * cosw - 2.0f * std::sqrt(A) * alpha;
    
    // Normalize coefficients
    filter.b0 = b0 / a0;
    filter.b1 = b1 / a0;
    filter.b2 = b2 / a0;
    filter.a1 = a1 / a0;
    filter.a2 = a2 / a0;
}

void DSPProcessor::designPeakFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate) {
    float w = 2.0f * M_PI * frequency / sampleRate;
    float cosw = std::cos(w);
    float sinw = std::sin(w);
    float A = dBToLinear(gain);
    float alpha = sinw / (2.0f * Q);
    
    float b0 = 1.0f + alpha * A;
    float b1 = -2.0f * cosw;
    float b2 = 1.0f - alpha * A;
    float a0 = 1.0f + alpha / A;
    float a1 = -2.0f * cosw;
    float a2 = 1.0f - alpha / A;
    
    // Normalize coefficients
    filter.b0 = b0 / a0;
    filter.b1 = b1 / a0;
    filter.b2 = b2 / a0;
    filter.a1 = a1 / a0;
    filter.a2 = a2 / a0;
}

void DSPProcessor::designHighShelfFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate) {
    float w = 2.0f * M_PI * frequency / sampleRate;
    float cosw = std::cos(w);
    float sinw = std::sin(w);
    float A = dBToLinear(gain);
    float S = 1.0f;
    float alpha = sinw / 2.0f * std::sqrt((A + 1.0f / A) * (1.0f / S - 1.0f) + 2.0f);
    
    float b0 = A * ((A + 1.0f) + (A - 1.0f) * cosw + 2.0f * std::sqrt(A) * alpha);
    float b1 = -2.0f * A * ((A - 1.0f) + (A + 1.0f) * cosw);
    float b2 = A * ((A + 1.0f) + (A - 1.0f) * cosw - 2.0f * std::sqrt(A) * alpha);
    float a0 = (A + 1.0f) - (A - 1.0f) * cosw + 2.0f * std::sqrt(A) * alpha;
    float a1 = 2.0f * ((A - 1.0f) - (A + 1.0f) * cosw);
    float a2 = (A + 1.0f) - (A - 1.0f) * cosw - 2.0f * std::sqrt(A) * alpha;
    
    // Normalize coefficients
    filter.b0 = b0 / a0;
    filter.b1 = b1 / a0;
    filter.b2 = b2 / a0;
    filter.a1 = a1 / a0;
    filter.a2 = a2 / a0;
}

float DSPProcessor::processBiquadFilter(BiquadFilter& filter, float input) {
    float output = filter.b0 * input + filter.b1 * filter.x1 + filter.b2 * filter.x2
                   - filter.a1 * filter.y1 - filter.a2 * filter.y2;
    
    // Update history
    filter.x2 = filter.x1;
    filter.x1 = input;
    filter.y2 = filter.y1;
    filter.y1 = output;
    
    return output;
}

float DSPProcessor::levelToGain(int level) {
    // Convert 0-100 level to dB gain (-12dB to +12dB)
    float normalizedLevel = (level - 50.0f) / 50.0f; // -1.0 to 1.0
    return normalizedLevel * MAX_GAIN_DB;
}

float DSPProcessor::dBToLinear(float dB) {
    return std::pow(10.0f, dB / 20.0f);
}

float DSPProcessor::linearToDb(float linear) {
    return 20.0f * std::log10(std::max(linear, 1e-6f));
}
