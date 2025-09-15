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
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "VehicleEqualizerApp"

    private var equalizerService: IEqualizerService? = null
    private val vehicleSensorSimulator = VehicleSensorSimulator("Velocidade")
    private val vehicleCanBusSimulator = VehicleCanBusSimulator()
    private val activityScope = CoroutineScope(Dispatchers.Main)

    // Otimização aplicada para evitar alocações excessivas de memória
    private val equalizerServiceIntent by lazy { Intent(this, EqualizerService::class.java) }

    // Otimização aplicada com o uso de WeakReference para evitar MemoryLeaks (Vazamento de memória devido a perda de referência para um objeto)
    private val serviceConnection: ServiceConnection by lazy {
        val activityRef = WeakReference(this)
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val activity = activityRef.get() ?: return
                activity.equalizerService = IEqualizerService.Stub.asInterface(service)
                Log.i(TAG, "Conectado ao EqualizerService.")
                activity.equalizerService?.setEqualizerEnabled(activity.binding.switch1.isChecked)
                activity.equalizerService?.setBassLevel(activity.binding.seekBar.progress)
                activity.equalizerService?.setMidLevel(activity.binding.seekBar2.progress)
                activity.equalizerService?.setTrebleLevel(activity.binding.seekBar3.progress)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                val activity = activityRef.get()
                activity?.equalizerService = null
                Log.w(TAG, "Desconectado do EqualizerService.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nativeStatusTextView.text = stringFromJNI()
        setEqualizerControlsEnabled(binding.switch1.isChecked)

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            setEqualizerControlsEnabled(isChecked)
            equalizerService?.setEqualizerEnabled(isChecked)
            setEqualizerEnabledNative(isChecked)
        }

        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                when (seekBar?.id) {
                    binding.seekBar.id -> {
                        equalizerService?.setBassLevel(seekBar.progress)
                        setBassLevelNative(seekBar.progress)
                    }
                    binding.seekBar2.id -> {
                        equalizerService?.setMidLevel(seekBar.id)
                        setMidLevelNative(seekBar.progress)
                    }
                    binding.seekBar3.id -> {
                        equalizerService?.setTrebleLevel(seekBar.id)
                        setTrebleLevelNative(seekBar.progress)
                    }
                }
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar2.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar3.setOnSeekBarChangeListener(seekBarChangeListener)

        binding.readSpeedButton.setOnClickListener {
            val currentSpeed = vehicleSensorSimulator.readSensorData()
            binding.speedLabel.text = "Velocidade Atual: $currentSpeed km/h"
            Log.d(TAG, "Velocidade lida: $currentSpeed km/h")
        }

        binding.sendCanVolumeButton.setOnClickListener {
            val randomVolume = (0..100).random()
            val message = CanMessage(id = 0x123, data = byteArrayOf(randomVolume.toByte()))
            vehicleCanBusSimulator.sendMessage(message)
        }

        activityScope.launch {
            vehicleCanBusSimulator.canMessageFlow.collect { message ->
                if (message.id == 0x123 && message.data.isNotEmpty()) {
                    val volume = message.data[0].toInt() and 0xFF
                    binding.canVolumeLabel.text = "Volume CAN: $volume"
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(equalizerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (equalizerService != null) {
            unbindService(serviceConnection)
            equalizerService = null
            Log.i(TAG, "Desvinculado do EqualizerService.")
        }
        vehicleCanBusSimulator.stopSimulator()
        activityScope.cancel()
    }

    private fun setEqualizerControlsEnabled(enabled: Boolean) {
        binding.seekBar.isEnabled = enabled
        binding.seekBar2.isEnabled = enabled
        binding.seekBar3.isEnabled = enabled
    }

    external fun stringFromJNI(): String
    external fun setEqualizerEnabledNative(enabled: Boolean)
    external fun setBassLevelNative(level: Int)
    external fun setMidLevelNative(level: Int)
    external fun setTrebleLevelNative(level: Int)

    companion object {
        init {
            System.loadLibrary("vehicleequalizernative")
        }
    }
}
