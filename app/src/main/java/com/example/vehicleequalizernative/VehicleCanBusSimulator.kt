package com.example.vehicleequalizernative
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class VehicleCanBusSimulator {
    private val TAG = "VehicleCanBusSimulator"
    private val messageChannel = Channel<CanMessage>()

    // Otimização através de Coroutine para delegar long-running tasks para outra Thread
    private val scope = CoroutineScope(Dispatchers.Default)

    // Flow para notificar a UI sobre mensagens CAN recebidas
    private val _canMessageFlow = MutableSharedFlow<CanMessage>()
    val canMessageFlow = _canMessageFlow.asSharedFlow()

    init {
        // Inicia um consumidor de mensagens CAN simulado
        scope.launch{
            for (message in messageChannel)
            {
                Log.d(TAG, "Mensagem CAN recebida (ID: 0x%X, Dados: %s)".
                format(message.id, message.data.joinToString { "%02X".format(it) }))
                // Publica a mensagem para qualquer ouvinte (ex: MainActivity)
                _canMessageFlow.emit(message)

                // Simula o processamento da mensagem CAN
                when (message.id)
                {
                    0x123 -> {
                        // Exemplo: ID para mensagem de volume mestre
                        if (message.data.isNotEmpty())
                        {
                            val volume = message.data[0].toInt() and 0xFF
                            // Converte byte para int (0-255)
                            Log.i(TAG, "Processando mensagem CAN: Novo Volume Mestre: $volume")
                            // Em um cenário real, isso seria enviado para a HAL de áudio

                        }

                    }
                    // Adicione outros IDs de mensagem CAN para simular diferentes dados do veículo
                }
            }
        }
    }

    /**
     * Simula o envio de uma mensagem CAN para o barramento.
     * @param message A mensagem CAN a ser enviada.
     */

    fun sendMessage(message: CanMessage) {
        scope.launch {
            Log.i(TAG, "Enviando mensagem CAN (ID: 0x%X, Dados: %s)".
            format(message.id, message.data.joinToString { "%02X".format(it) }))
            messageChannel.send(message)
        }
    }

    /**
     * Para o simulador CAN e libera os recursos.
     */
    fun stopSimulator(){
        scope.cancel()
        messageChannel.close()
        Log.d(TAG, "Simulador CAN parado.")
    }
}