package com.example.blueprintproapps.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View

class ParallaxEffect(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var targetView: View? = null
    private val intensiveness = 5f // Reduced from 30f to prevent huge gaps

    fun attach(view: View) {
        targetView = view
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun detach() {
        sensorManager.unregisterListener(this)
        targetView = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            // Subtle parallax move with gravity offset (centering for ~45 degree hold)
            targetView?.let { v ->
                v.translationX = -x * intensiveness
                v.translationY = (y - 8f) * intensiveness
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
