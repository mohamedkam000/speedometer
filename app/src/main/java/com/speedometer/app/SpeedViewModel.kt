package com.speedometer.app

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class SpeedViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {
    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _speed = MutableStateFlow(0f)
    val speed = _speed.asStateFlow()

    private val _movement = MutableStateFlow("Idle")
    val movement = _movement.asStateFlow()

    private var lastUpdate = 0L
    private var currentVelocity = 0f

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        if (lastUpdate == 0L) {
            lastUpdate = now
            return
        }

        val dt = (now - lastUpdate) / 1000f
        lastUpdate = now

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val rawMagnitude = sqrt(ax * ax + ay * ay + az * az)
        var linearAccel = abs(rawMagnitude - 9.81f)

        if (linearAccel < 0.3f) {
            linearAccel = 0f
        }

        currentVelocity += linearAccel * dt * 10f

        currentVelocity *= 0.92f

        if (currentVelocity < 0.1f) {
            currentVelocity = 0f
        }

        val displayedSpeed = (currentVelocity * 3.6f)

        _speed.value = displayedSpeed
        _movement.value = if (displayedSpeed < 1f) "Idle" else "Moving"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}