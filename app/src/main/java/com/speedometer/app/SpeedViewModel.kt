package com.speedometer.app

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class SpeedViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val fused = LocationServices.getFusedLocationProviderClient(app)
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

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        500
    ).build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location? = result.locations.lastOrNull()
            if (location != null) {
                val kmh = (location.speed * 3.6).toInt()
                if (kmh > _speed.value) {
                    _speed.value = kmh
                }
            }
        }
    }

    init {
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        fused.requestLocationUpdates(request, callback, null)
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
        val kmh = (velocity * 3.6f).toInt()
        _speed.value = kmh

        when {
            kmh < 8 && magnitude > 0.3f -> _movement.value = "On Foot"
            kmh >= 8 || magnitude < 0.2f -> _movement.value = "In Car"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}