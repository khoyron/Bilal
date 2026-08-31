package org.khoyron.bilal.ui.qiblah

interface QiblahSensorManager {
    fun start(
        onBearingChanged: (Float) -> Unit,
        onLocationChanged: (Double, Double) -> Unit,
        onError: (String) -> Unit
    )
    fun stop()
}
