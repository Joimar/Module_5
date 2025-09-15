package com.example.vehicleequalizernative
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class EqualizerService: Service() {

    private val TAG = "EqualizerService"

    // Implementação da interface AIDL
    private val equalizerServiceBinder = object :
        IEqualizerService.Stub(){

        override fun setEqualizerEnabled(enabled: Boolean) {
            Log.d(TAG, "[Serviço] Equalizador ${if (enabled) "ativado"
            else "desativado"}")
            // Em um cenário real, aqui você chamaria a HAL de áudio ou o código nativo
        }
        override fun setBassLevel(level: Int) {
            Log.d(TAG, "[Serviço] Nível de Graves: $level")
            // Em um cenário real, aqui você chamaria a HAL de áudio ou o código nativo
        }

        override fun setMidLevel(level: Int) {
            Log.d(TAG, "[Serviço] Nível de Médios: $level")
        // Em um cenário real, aqui você chamaria a HAL de áudio ou o código nativo
        }
        override fun setTrebleLevel(level: Int) {
            Log.d(TAG, "[Serviço] Nível de Agudos: $level") // Em um cenário real, aqui você chamaria a HAL de áudio ou o código nativo
        }
        }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Serviço de Equalizador criado.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Serviço de Equalizador vinculado.")
        return equalizerServiceBinder // Retorna a implementação do Binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Serviço de Equalizador desvinculado.")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Serviço de Equalizador destruído.")
    }

}