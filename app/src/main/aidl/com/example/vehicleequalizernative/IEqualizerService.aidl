// IEqualizerService.aidl
package com.example.vehicleequalizernative;

// Declare any non-default types here with import statements

interface IEqualizerService {
    void setEqualizerEnabled(boolean enabled);
    void setBassLevel(int level);
    void setMidLevel(int level);
    void setTrebleLevel(int level);
// Você pode adicionar métodos para obter os valores atuais, senecessário
// boolean isEqualizerEnabled();
// int getBassLevel();
}