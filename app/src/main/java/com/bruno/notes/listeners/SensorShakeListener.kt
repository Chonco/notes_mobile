package com.bruno.notes.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class SensorShakeListener(private val handleShake: () -> Unit) : SensorEventListener {
    private object Constants {
        const val MINIMUM_ACCELERATION = 16
    }

    private var acceleration = 10f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    override fun onSensorChanged(event: SensorEvent) {
        calculateAcceleration(event)
        if (acceleration > Constants.MINIMUM_ACCELERATION)
            handleShake()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    private fun calculateAcceleration(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        lastAcceleration = currentAcceleration

        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta: Float = currentAcceleration - lastAcceleration
        acceleration = abs(acceleration * 0.9f + delta)
    }
}