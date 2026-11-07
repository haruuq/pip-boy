package com.example.pipboymojave.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Collects live readings from the phone's hardware sensors, microphone and GPS.
 * Sound level is a rough estimate (uncalibrated), not a measured dB SPL.
 */
class SensorRepository(private val context: Context) : SensorEventListener, LocationListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(SensorState())
    val state: StateFlow<SensorState> = _state

    private var micJob: Job? = null
    private var running = false

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var haveGravity = false
    private var haveMagnet = false
    private val rotation = FloatArray(9)
    private val orientation = FloatArray(3)
    private var lastHistoryMs = 0L

    // 10 Hz-ish UI updates; sensors themselves are sampled with SENSOR_DELAY_UI.
    fun start() {
        if (running) return
        running = true
        register(Sensor.TYPE_ACCELEROMETER)
        register(Sensor.TYPE_GYROSCOPE)
        register(Sensor.TYPE_MAGNETIC_FIELD)
        register(Sensor.TYPE_PRESSURE)
        register(Sensor.TYPE_LIGHT)
        register(Sensor.TYPE_PROXIMITY)
        if (granted(android.Manifest.permission.ACTIVITY_RECOGNITION)) {
            register(Sensor.TYPE_STEP_COUNTER)
        }
        refreshPermissions()
        startLocation()
        startMic()
    }

    fun stop() {
        running = false
        sensorManager.unregisterListener(this)
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
        micJob?.cancel()
        micJob = null
    }

    /** Call after the user answers a permission dialog. */
    fun onPermissionsChanged() {
        if (!running) return
        sensorManager.unregisterListener(this)
        micJob?.cancel()
        running = false
        start()
    }

    private fun granted(p: String) =
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    private fun refreshPermissions() {
        _state.value = _state.value.copy(
            locationGranted = granted(android.Manifest.permission.ACCESS_FINE_LOCATION),
            micGranted = granted(android.Manifest.permission.RECORD_AUDIO),
            cameraGranted = granted(android.Manifest.permission.CAMERA)
        )
    }

    private fun register(type: Int) {
        val s = sensorManager.getDefaultSensor(type) ?: return
        sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(e: SensorEvent) {
        val s = _state.value
        when (e.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(e.values, 0, gravity, 0, 3)
                haveGravity = true
                val t = Triple(e.values[0], e.values[1], e.values[2])
                val now = System.currentTimeMillis()
                var hist = s.accelHistory
                if (now - lastHistoryMs >= 100) { // ~10 Hz graph
                    lastHistoryMs = now
                    hist = (hist + t).takeLast(HISTORY)
                }
                _state.value = s.copy(accel = t, accelHistory = hist, headingDeg = computeHeading() ?: s.headingDeg)
            }
            Sensor.TYPE_GYROSCOPE ->
                _state.value = s.copy(gyro = Triple(e.values[0], e.values[1], e.values[2]))
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(e.values, 0, geomagnetic, 0, 3)
                haveMagnet = true
                val mag = sqrt(e.values[0] * e.values[0] + e.values[1] * e.values[1] + e.values[2] * e.values[2])
                _state.value = s.copy(
                    magnet = Triple(e.values[0], e.values[1], e.values[2]),
                    magnetUt = mag,
                    headingDeg = computeHeading() ?: s.headingDeg
                )
            }
            Sensor.TYPE_STEP_COUNTER -> _state.value = s.copy(steps = e.values[0].toInt())
            Sensor.TYPE_PRESSURE -> _state.value = s.copy(pressureHpa = e.values[0])
            Sensor.TYPE_LIGHT -> _state.value = s.copy(lightLux = e.values[0])
            Sensor.TYPE_PROXIMITY -> {
                val max = e.sensor.maximumRange
                _state.value = s.copy(proximityNear = e.values[0] < max && e.values[0] < 5f)
            }
        }
    }

    private fun computeHeading(): Float? {
        if (!haveGravity || !haveMagnet) return null
        if (!SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)) return null
        SensorManager.getOrientation(rotation, orientation)
        val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        return (deg + 360f) % 360f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ---- Location -------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startLocation() {
        if (!granted(android.Manifest.permission.ACCESS_FINE_LOCATION)) return
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000L, 1f, this, Looper.getMainLooper()
                )
            }
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { onLocationChanged(it) }
        } catch (_: Exception) {
        }
    }

    override fun onLocationChanged(location: Location) {
        _state.value = _state.value.copy(
            locationAccuracyM = if (location.hasAccuracy()) location.accuracy else null,
            speedMps = if (location.hasSpeed()) location.speed else null
        )
    }

    // ---- Microphone (rough dB estimate, uncalibrated) ----------------------------------------

    @SuppressLint("MissingPermission")
    private fun startMic() {
        if (!granted(android.Manifest.permission.RECORD_AUDIO)) return
        micJob = scope.launch {
            val rate = 44100
            val minBuf = AudioRecord.getMinBufferSize(
                rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return@launch
            val rec = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC, rate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuf
                )
            } catch (e: Exception) {
                return@launch
            }
            if (rec.state != AudioRecord.STATE_INITIALIZED) {
                rec.release(); return@launch
            }
            val buf = ShortArray(minBuf)
            try {
                rec.startRecording()
                var smoothed = 0f
                while (isActive) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        var sum = 0.0
                        for (i in 0 until n) sum += buf[i] * buf[i].toDouble()
                        val rms = sqrt(sum / n)
                        // Map digital RMS to a plausible 20-100 range. This is an estimate only.
                        val db = (20.0 * log10(rms.coerceAtLeast(1.0)) + 10.0).toFloat().coerceIn(20f, 100f)
                        smoothed = if (smoothed == 0f) db else smoothed * 0.8f + db * 0.2f
                        _state.value = _state.value.copy(soundDb = smoothed)
                    }
                    delay(100)
                }
            } finally {
                try { rec.stop() } catch (_: Exception) {}
                rec.release()
            }
        }
    }

    private companion object {
        const val HISTORY = 40
    }
}
