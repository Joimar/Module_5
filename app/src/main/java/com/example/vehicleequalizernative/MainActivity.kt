package com.example.vehicleequalizernative

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import com.example.vehicleequalizernative.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "VehicleEqualizerApp"
    private var equalizerService: IEqualizerService? = null

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
                setEqualizerEnabledNative(isChecked) // Chama a função nativa
            }
        )

        // Listener genérico para os SeekBars
        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            // Opcional: exibir valor em tempo real na UI
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                when (seekBar?.id) {
                    binding.seekBar.id ->
                        setBassLevelNative(seekBar.progress)
                    binding.seekBar2.id ->
                        setMidLevelNative(seekBar.progress)
                    binding.seekBar3.id ->
                        setTrebleLevelNative(seekBar.progress)
                }
            }
        }

        // Atribui o listener a cada SeekBar
        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar2.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.seekBar3.setOnSeekBarChangeListener(seekBarChangeListener)

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