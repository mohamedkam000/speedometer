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
import kotlin.math.exp

class SpeedViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed

    private val _movement = MutableStateFlow("Unknown")
    val movement: StateFlow<String> = _movement

    private var lastTime = 0L
    private val velocity = FloatArray(3)
    private val bias = FloatArray(3)
    private var biasCount = 0
    private var stillTime = 0L

    init {
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val ax = event.values[0] - bias[0]
        val ay = event.values[1] - bias[1]
        val az = event.values[2] - bias[2]

        val magnitude = sqrt(ax*ax + ay*ay + az*az)

        val now = event.timestamp
        val dt = if (lastTime == 0L) 0f else (now - lastTime) / 1_000_000_000f
        lastTime = now
        if (dt <= 0f) return

        if (magnitude < 0.1f) {
            stillTime += (dt * 1000).toLong()
            if (stillTime > 500) {
                velocity[0] = 0f
                velocity[1] = 0f
                velocity[2] = 0f
                bias[0] = bias[0] * 0.98f + event.values[0] * 0.02f
                bias[1] = bias[1] * 0.98f + event.values[1] * 0.02f
                bias[2] = bias[2] * 0.98f + event.values[2] * 0.02f
                biasCount++
                return
            }
        } else {
            stillTime = 0
        }

        velocity[0] += ax * dt
        velocity[1] += ay * dt
        velocity[2] += az * dt

        val decay = exp(-0.4f * dt)
        velocity[0] *= decay
        velocity[1] *= decay
        velocity[2] *= decay

        val horizontal = sqrt(velocity[0]*velocity[0] + velocity[1]*velocity[1])
        val mph = (horizontal * 3600f).toInt()

        _speed.value = mph

        if (mph < 1500 && magnitude > 0.4f) _movement.value = "On Foot"
        else _movement.value = "In Car"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}