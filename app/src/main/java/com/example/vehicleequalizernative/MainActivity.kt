package com.example.vehicleequalizernative

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.vehicleequalizernative.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()

        // Define o estado inicial dos SeekBars com base no estado do Switch
        setEqualizerControlsEnabled(binding.switch1.isChecked)
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