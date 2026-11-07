package com.example.pipboymojave.sensors

import com.example.pipboymojave.data.Scores
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Turns raw sensor readings into the five game-style attributes (0-100).
 * These are illustrative game scores, NOT health, fitness or safety metrics.
 */
object ScoreEngine {

    private const val G = 9.81f

    fun compute(s: SensorState): Scores = Scores(
        stamina = stamina(s),
        awareness = awareness(s),
        stealth = stealth(s),
        navigation = navigation(s),
        environment = environment(s)
    )

    private fun clamp(v: Float) = v.coerceIn(0f, 100f).toInt()

    private fun motionIntensity(s: SensorState): Float {
        val a = s.accel ?: return 0f
        val mag = sqrt(a.first * a.first + a.second * a.second + a.third * a.third)
        return abs(mag - G) // deviation from gravity, m/s^2
    }

    private fun rotationIntensity(s: SensorState): Float {
        val g = s.gyro ?: return 0f
        return sqrt(g.first * g.first + g.second * g.second + g.third * g.third) // rad/s
    }

    /** Movement + exploration: motion, steps, speed. */
    private fun stamina(s: SensorState): Int {
        val motion = (motionIntensity(s) / 3f).coerceIn(0f, 1f) * 25f
        val steps = ((s.steps ?: 0) / 10000f).coerceIn(0f, 1f) * 50f
        val speed = ((s.speedMps ?: 0f) / 3f).coerceIn(0f, 1f) * 25f
        return clamp(motion + steps + speed)
    }

    /** How much environmental information is currently available. */
    private fun awareness(s: SensorState): Int {
        var score = 0f
        if (s.lightLux != null) score += 25f
        if (s.soundDb != null) score += 25f
        if (s.cameraGranted) score += 25f
        if (s.hasLocationFix) score += 25f
        // A little extra when the readings are in usable ranges.
        if ((s.lightLux ?: 0f) in 50f..10000f) score += 0f
        return clamp(score)
    }

    /** Calm movement + quiet surroundings + covered proximity sensor = stealthier. */
    private fun stealth(s: SensorState): Int {
        var score = 100f
        score -= (motionIntensity(s) / 3f).coerceIn(0f, 1f) * 35f
        score -= (rotationIntensity(s) / 2f).coerceIn(0f, 1f) * 20f
        s.soundDb?.let { score -= ((it - 30f) / 50f).coerceIn(0f, 1f) * 35f }
        if (s.proximityNear == true) score -= 10f
        return clamp(score)
    }

    /** Location accuracy, compass availability, steady rotation. */
    private fun navigation(s: SensorState): Int {
        var score = 0f
        s.locationAccuracyM?.let { score += (1f - (it / 50f).coerceIn(0f, 1f)) * 40f }
        if (s.headingDeg != null) score += 35f
        score += (1f - (rotationIntensity(s) / 2f).coerceIn(0f, 1f)) * 15f
        score += (1f - (motionIntensity(s) / 4f).coerceIn(0f, 1f)) * 10f
        return clamp(score)
    }

    /** Brightness, sound, pressure and location context. */
    private fun environment(s: SensorState): Int {
        var score = 0f
        s.lightLux?.let { score += (it / 500f).coerceIn(0f, 1f) * 30f }
        s.soundDb?.let { score += (1f - ((it - 30f) / 60f).coerceIn(0f, 1f)) * 25f }
        s.pressureHpa?.let { score += (1f - (abs(it - 1013f) / 40f).coerceIn(0f, 1f)) * 25f }
        if (s.hasLocationFix) score += 20f
        return clamp(score)
    }
}
