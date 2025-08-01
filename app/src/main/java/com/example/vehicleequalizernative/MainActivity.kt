package com.example.vehicleequalizernative

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import com.example.vehicleequalizernative.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "VehicleEqualizerApp"

    private var equalizerService: IEqualizerService? = null
    private val vehicleSensorSimulator = VehicleSensorSimulator("Velocidade") // Instância do simulador de sensorde velocidade
    private val vehicleCanBusSimulator = VehicleCanBusSimulator() //Instância do simulador CAN
    private val activityScope = CoroutineScope(Dispatchers.Main) //Escopo para corrotinas da Activity

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service:
        IBinder?) {
            equalizerService =
                IEqualizerService.Stub.asInterface(service)
            Log.i(TAG, "Conectado ao EqualizerService.")
            equalizerService?.setEqualizerEnabled(binding.switch1.isChecked)
            equalizerService?.setBassLevel(binding.seekBar3.progress)
            equalizerService?.setMidLevel(binding.seekBar2.progress)
            equalizerService?.setTrebleLevel(binding.seekBar.progress)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            equalizerService = null
            Log.w(TAG, "Desconectado do EqualizerService.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method

        binding.nativeStatusTextView.text = stringFromJNI()
        // Define o estado inicial dos SeekBars com base no estado do Switch
        setEqualizerControlsEnabled(binding.switch1.isChecked)

        // Listener para o Switch do equalizador
        binding.switch1.setOnCheckedChangeListener(
            {_, isChecked ->
                setEqualizerControlsEnabled(isChecked)
                equalizerService?.setEqualizerEnabled(isChecked)
                setEqualizerEnabledNative(isChecked) // C// Chama a função nativa do Módulo 04
            }
        )

        // Listener genérico para os SeekBars
        val seekBarChangeListener = object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            // Opcional: exibir valor em tempo real na UI
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                when (seekBar?.id) {
                    binding.seekBar.id ->{
                        equalizerService?.setBassLevel(seekBar.progress)
                        setBassLevelNative(seekBar.progress)
                    }
                    binding.seekBar2.id ->{
                        equalizerService?.setMidLevel(seekBar.id)
                        setMidLevelNative(seekBar.progress)
                    }
                    binding.seekBar3.id ->{
                        equalizerService?.setTrebleLevel(seekBar.id)
                        setTrebleLevelNative(seekBar.progress)
                    }
                }
            }
        }

        // Atribui o listener a cada SeekBar
        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar2.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar3.setOnSeekBarChangeListener(seekBarChangeListener)

        // Listener para o botão de leitura de velocidade
        binding.readSpeedButton.setOnClickListener{
            val currentSpeed = vehicleSensorSimulator.readSensorData()
            binding.speedLabel.text = "Velocidade Atual: $currentSpeed km/h"
            Log.d(TAG, "Velocidade lida: $currentSpeed km/h")
        }

        // Listener para o botão de envio de volume CAN
        binding.sendCanVolumeButton.setOnClickListener{
            // Simula o envio de uma mensagem CAN de volume (ID 0x123, valor aleatório 0-100)
            val randomVolume = (0..100).random()
            val message = CanMessage(id = 0x123, data =
                byteArrayOf(randomVolume.toByte()))
            vehicleCanBusSimulator.sendMessage(message)
        }

        // Coleta mensagens CAN recebidas e atualiza a UI
        activityScope.launch{
            vehicleCanBusSimulator.canMessageFlow.collect{ message ->
                if (message.id == 0x123 && message.data.isNotEmpty())
                {
                    val volume = message.data[0].toInt() and 0xFF
                    binding.canVolumeLabel.text = "Volume CAN: $volume"
                    // Em um cenário real, você poderia usar este volume para ajustar o áudio
                    // equalizerService?.setMasterVolume(volume)
                    // Exemplo de chamada ao serviço
                }
            }
        }
    }

    override fun onStart()
    {
        super.onStart()
        val intent = Intent(this, EqualizerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop()
    {
        super.onStop()
        if (equalizerService != null) {
            unbindService(serviceConnection)
            equalizerService = null
            Log.i(TAG, "Desvinculado do EqualizerService.")
        }
        // Importante: parar o simulador CAN quando a atividade for destruída ou não for mais necessária
        vehicleCanBusSimulator.stopSimulator()
        activityScope.cancel() // Cancela as corrotinas do escopo da atividade
    }
    /**
     * Habilita ou desabilita os controles do equalizador (SeekBars).
     * @param enabled true para habilitar, false para desabilitar.
     */
    private fun setEqualizerControlsEnabled(enabled: Boolean) {
        binding.seekBar.isEnabled = enabled
        binding.seekBar2.isEnabled = enabled
        binding.seekBar3.isEnabled = enabled


    }

    /**
     * A native method that is implemented by the 'vehicleequalizernative' native library,
     * which is packaged with this application.
     */

    /**
     * Declaração dos métodos nativos que serão implementados pela
    biblioteca C++.
     * A palavra-chave 'external' indica que a implementação está em
    código nativo.
     */

    external fun stringFromJNI(): String
    external fun setEqualizerEnabledNative(enabled: Boolean)
    external fun setBassLevelNative(level: Int)
    external fun setMidLevelNative(level: Int)
    external fun setTrebleLevelNative(level: Int)

    companion object {
        // Used to load the 'vehicleequalizernative' library on application startup.
        init {
            System.loadLibrary("vehicleequalizernative")
        }
    }
}