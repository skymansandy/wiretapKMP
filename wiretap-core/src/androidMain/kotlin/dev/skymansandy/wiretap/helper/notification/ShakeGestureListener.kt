package dev.skymansandy.wiretap.helper.notification

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import kotlin.math.sqrt

internal class ShakeGestureListener : DefaultLifecycleObserver {

    private val sensorManager = WiretapContextProvider.context.getSystemService(
        Context.SENSOR_SERVICE
    ) as? SensorManager
    private var activeAcceleration = 10f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private val thresholdForAcceleration = 12f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt(x * x + y * y + z * z)
            val delta = currentAcceleration - lastAcceleration
            activeAcceleration = activeAcceleration * 0.9f + delta

            if (activeAcceleration > thresholdForAcceleration) {
                startWiretap()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume(owner: LifecycleOwner) {
        sensorManager?.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager?.unregisterListener(sensorListener)
    }
}
