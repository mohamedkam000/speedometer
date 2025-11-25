package com.speedometer.app

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class SpeedViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {
    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _speed = MutableStateFlow(0f)
    val speed = _speed.asStateFlow()

    private val _movement = MutableStateFlow("Idle")
    val movement = _movement.asStateFlow()

    private var lastUpdate = 0L
    private var smoothedAccel = 0f

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        if (now - lastUpdate < 150) return
        lastUpdate = now

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val accel = sqrt(ax * ax + ay * ay + az * az) - 9.81f

        smoothedAccel = smoothedAccel * 0.85f + accel * 0.15f

        val displayedSpeed = (smoothedAccel * 200).coerceAtLeast(0f)
        _speed.value = displayedSpeed

        _movement.value = if (displayedSpeed < 1f) "Idle" else "Moving"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}