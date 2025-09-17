#ifndef DSPPROCESSOR_H
#define DSPPROCESSOR_H

#include <vector>
#include <memory>
#include <cmath>

/**
 * Digital Signal Processing class for audio equalization
 * Implements biquad filters for bass, mid, and treble control
 */
class DSPProcessor {
public:
    // Constructor and destructor
    DSPProcessor();
    ~DSPProcessor();

    // Equalizer control methods
    void setEqualizerEnabled(bool enabled);
    void setBassLevel(int level);      // 0-100 range
    void setMidLevel(int level);       // 0-100 range  
    void setTrebleLevel(int level);    // 0-100 range
    
    // Audio processing methods
    void processAudioBuffer(float* inputBuffer, float* outputBuffer, int numSamples, int sampleRate);
    void processAudioBufferStereo(float* leftChannel, float* rightChannel, int numSamples, int sampleRate);
    
    // Filter configuration methods
    void setSampleRate(int sampleRate);
    void setFilterType(int filterType); // 0=LowShelf, 1=Peak, 2=HighShelf
    
    // Utility methods
    bool isEqualizerEnabled() const;
    int getBassLevel() const;
    int getMidLevel() const;
    int getTrebleLevel() const;
    
    // Reset methods
    void resetFilters();
    void clearBuffers();

private:
    // Biquad filter structure
    struct BiquadFilter {
        float b0, b1, b2;  // Numerator coefficients
        float a1, a2;      // Denominator coefficients
        float x1, x2;      // Input history
        float y1, y2;      // Output history
        
        BiquadFilter() : b0(1.0f), b1(0.0f), b2(0.0f), 
                        a1(0.0f), a2(0.0f), x1(0.0f), x2(0.0f), y1(0.0f), y2(0.0f) {}
        
        void reset() {
            x1 = x2 = y1 = y2 = 0.0f;
        }
    };

    // Filter design methods
    void designLowShelfFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate);
    void designPeakFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate);
    void designHighShelfFilter(BiquadFilter& filter, float frequency, float gain, float Q, int sampleRate);
    
    // Filter processing
    float processBiquadFilter(BiquadFilter& filter, float input);
    
    // Utility functions
    float levelToGain(int level);  // Convert 0-100 level to dB gain
    float dBToLinear(float dB);    // Convert dB to linear gain
    float linearToDb(float linear); // Convert linear to dB
    
    // Member variables
    bool m_equalizerEnabled;
    int m_bassLevel;      // 0-100
    int m_midLevel;       // 0-100
    int m_trebleLevel;    // 0-100
    int m_sampleRate;
    int m_filterType;
    
    // Filter instances
    BiquadFilter m_bassFilter;
    BiquadFilter m_midFilter;
    BiquadFilter m_trebleFilter;
    
    // Filter parameters
    static constexpr float BASS_FREQUENCY = 100.0f;    // Hz
    static constexpr float MID_FREQUENCY = 1000.0f;    // Hz
    static constexpr float TREBLE_FREQUENCY = 8000.0f; // Hz
    static constexpr float FILTER_Q = 0.707f;          // Butterworth Q
    static constexpr float MAX_GAIN_DB = 12.0f;        // Maximum gain in dB
    static constexpr float MIN_GAIN_DB = -12.0f;       // Minimum gain in dB
};

#endif // DSPPROCESSOR_H
