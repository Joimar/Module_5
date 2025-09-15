package com.example.vehicleequalizernative

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vehicleequalizernative.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var equalizerService: IEqualizerService? = null
    private val TAG = "MainActivity"

    private val vehicleSensorSimulator = VehicleSensorSimulator("Velocidade")
    private var vehicleCanBusSimulator: VehicleCanBusSimulator? = null

    // Intent do serviço
    private val equalizerServiceIntent by lazy { Intent(this, EqualizerService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar2.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar3.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            when (seekBar?.id) {
                binding.seekBar.id -> {
                    equalizerService?.setBassLevel(seekBar.progress)
                    setBassLevelNative(seekBar.progress)
                }
                binding.seekBar2.id -> {
                    equalizerService?.setMidLevel(seekBar.progress) // corrigido
                    setMidLevelNative(seekBar.progress)
                }
                binding.seekBar3.id -> {
                    equalizerService?.setTrebleLevel(seekBar.progress) // corrigido
                    setTrebleLevelNative(seekBar.progress)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(equalizerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        vehicleCanBusSimulator = VehicleCanBusSimulator()
        lifecycleScope.launch(Dispatchers.Default) {
            vehicleCanBusSimulator?.canMessageFlow?.collect { message ->
                if (message.id == 0x123 && message.data.isNotEmpty()) {
                    val volume = message.data[0].toInt() and 0xFF
                    withContext(Dispatchers.Main) {
                        binding.canVolumeLabel.text = "Volume CAN: $volume"
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (equalizerService != null) {
            unbindService(serviceConnection)
            equalizerService = null
            Log.i(TAG, "Desvinculado do EqualizerService.")
        }
        vehicleCanBusSimulator?.stopSimulator()
        vehicleCanBusSimulator = null
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            equalizerService = IEqualizerService.Stub.asInterface(service)
            Log.i(TAG, "Conectado ao EqualizerService.")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            equalizerService = null
            Log.i(TAG, "EqualizerService desconectado.")
        }
    }

    private external fun setBassLevelNative(level: Int)
    private external fun setMidLevelNative(level: Int)
    private external fun setTrebleLevelNative(level: Int)

    companion object {
        init {
            System.loadLibrary("vehicleequalizernative")
        }
    }
}
