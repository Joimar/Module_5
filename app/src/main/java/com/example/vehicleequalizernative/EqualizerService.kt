package com.example.vehicleequalizernative

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class EqualizerService : Service() {

    private val TAG = "EqualizerService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[Serviço] EqualizerService criado")
        }
    }

    fun enableEqualizer(enabled: Boolean) {
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[Serviço] Equalizador ${if (enabled) "ativado" else "desativado"}")
        }
    }

    fun setBassLevel(level: Int) {
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[Serviço] Bass ajustado para: $level")
        }
    }

    fun setMidLevel(level: Int) {
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[Serviço] Mid ajustado para: $level")
        }
    }

    fun setTrebleLevel(level: Int) {
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[Serviço] Treble ajustado para: $level")
        }
    }
}
