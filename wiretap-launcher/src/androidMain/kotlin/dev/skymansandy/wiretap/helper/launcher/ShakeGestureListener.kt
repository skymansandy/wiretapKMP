/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider.context

internal class ShakeGestureListener : DefaultLifecycleObserver {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val filter = ShakeAccelerationFilter()

    private val sensorListener = object : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        override fun onSensorChanged(event: SensorEvent) {
            val isShake = filter.onSample(
                x = event.values[0],
                y = event.values[1],
                z = event.values[2],
                nowMs = System.currentTimeMillis(),
            )
            if (isShake) launchWiretapConsole()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        sensorManager?.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager?.unregisterListener(sensorListener)
    }
}
