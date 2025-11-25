package com.speedometer.app

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class SpeedViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed

    private val _movement = MutableStateFlow("Unknown")
    val movement: StateFlow<String> = _movement

    private var lastTime = 0L
    private var velocity = 0f
    private val gravity = FloatArray(3)
    private val linear = FloatArray(3)

    init {
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        gravity[0] = gravity[0] * 0.9f + x * 0.1f
        gravity[1] = gravity[1] * 0.9f + y * 0.1f
        gravity[2] = gravity[2] * 0.9f + z * 0.1f

        linear[0] = x - gravity[0]
        linear[1] = y - gravity[1]
        linear[2] = z - gravity[2]

        val magnitude = sqrt(
            linear[0] * linear[0] +
            linear[1] * linear[1] +
            linear[2] * linear[2]
        )

        val now = event.timestamp
        if (lastTime != 0L) {
            val dt = (now - lastTime) / 1_000_000_000f
            velocity += magnitude * dt
        }
        lastTime = now

        if (velocity < 0f) velocity = 0f

        val mps = velocity
        val mph = (mps * 3600f).toInt()

        _speed.value = mph

        if (mps < 2f && magnitude > 0.3f) _movement.value = "On Foot"
        else if (mps >= 2f || magnitude < 0.2f) _movement.value = "In Car"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}