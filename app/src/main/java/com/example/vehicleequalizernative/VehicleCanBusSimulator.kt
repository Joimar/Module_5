package com.example.vehicleequalizernative

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive

class VehicleCanBusSimulator {

    private val TAG = "VehicleCanBusSimulator"

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val messageChannel = Channel<CanMessage>(Channel.UNLIMITED)
    private val _canMessageFlow = MutableSharedFlow<CanMessage>(replay = 0)
    val canMessageFlow: SharedFlow<CanMessage> = _canMessageFlow

    fun startSimulator() {
        scope.launch {
            for (message in messageChannel) {
                if (!isActive) break
                processMessage(message)
                yield()
            }
        }

        scope.launch {
            var volume = 0
            while (isActive) {
                val data = byteArrayOf(volume.toByte())
                val message = CanMessage(0x123, data)
                messageChannel.send(message)

                volume = (volume + 5) % 100
                delay(2000)
            }
        }
    }

    private suspend fun processMessage(message: CanMessage) {
        if (AppConfig.DEBUG) {
            Log.d(TAG, "[CAN] Mensagem recebida: id=${message.id}, data=${message.data.joinToString()}")
        }
        _canMessageFlow.emit(message)
    }

    fun stopSimulator() {
        scope.cancel()
        messageChannel.close()
        if (AppConfig.DEBUG) {
            Log.i(TAG, "[CAN] Simulador parado.")
        }
    }
}
