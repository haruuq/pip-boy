package com.example.pipboymojave.sensors

/** Snapshot of every sensor. Null means "not available / no permission / no reading yet". */
data class SensorState(
    val accel: Triple<Float, Float, Float>? = null,
    val gyro: Triple<Float, Float, Float>? = null,
    val magnet: Triple<Float, Float, Float>? = null,
    val magnetUt: Float? = null,
    val headingDeg: Float? = null,
    val steps: Int? = null,
    val pressureHpa: Float? = null,
    val lightLux: Float? = null,
    val proximityNear: Boolean? = null,
    val soundDb: Float? = null,
    val locationAccuracyM: Float? = null,
    val speedMps: Float? = null,
    val locationGranted: Boolean = false,
    val micGranted: Boolean = false,
    val cameraGranted: Boolean = false,
    val accelHistory: List<Triple<Float, Float, Float>> = emptyList()
) {
    val hasLocationFix: Boolean get() = locationGranted && locationAccuracyM != null
}

fun headingLabel(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg % 360f) + 360f) % 360f / 45f + 0.5f).toInt() % 8
    return dirs[idx]
}
