package com.example.vehicleequalizernative

import android.util.Log
import java.util.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VehicleSensorSimulator(private val sensorName: String) {
    private val TAG = "VehicleSensorSimulator"
    private val random = Random()

    fun readSensorData(): Int {
        val data = when (sensorName) {
            "Velocidade" -> random.nextInt(201) // 0-200 km/h
            "Temperatura Externa" -> random.nextInt(50) - 10 // -10 a 39°C
            "Nível Combustível" -> random.nextInt(101) // 0-100 %
            else -> random.nextInt(101)
        }
        Log.d(TAG, "[$sensorName] Lendo dados do sensor: $data")
        return data
    }

    suspend fun calibrateSensor() {
        Log.i(TAG, "[$sensorName] Calibrando sensor...")
        delay(1000)
        Log.i(TAG, "[$sensorName] Sensor calibrado.")
    }

    fun calibrateSensorAsync(scope: CoroutineScope) {
        scope.launch {
            calibrateSensor()
        }
    }
}
