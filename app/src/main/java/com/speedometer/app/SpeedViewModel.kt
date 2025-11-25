package com.speedometer.app

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeedViewModel(app: Application) : AndroidViewModel(app) {
    private val fused = LocationServices.getFusedLocationProviderClient(app)

    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed

    private val _movement = MutableStateFlow("Unknown")
    val movement: StateFlow<String> = _movement

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        500
    ).build()

    init {
        fused.requestLocationUpdates(
            request,
            { result ->
                val location: Location? = result.lastLocation
                if (location != null) {
                    val kmh = (location.speed * 3.6).toInt()
                    _speed.value = kmh
                    _movement.value = classify(kmh)
                }
            },
            null
        )
    }

    private fun classify(kmh: Int): String {
        return if (kmh < 10) "On Foot" else "In Car"
    }
}