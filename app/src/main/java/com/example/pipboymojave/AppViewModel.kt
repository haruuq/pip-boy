package com.example.pipboymojave

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.pipboymojave.data.Assessment
import com.example.pipboymojave.data.AppData
import com.example.pipboymojave.data.JournalEntry
import com.example.pipboymojave.data.LedgerItem
import com.example.pipboymojave.data.LocalStore
import com.example.pipboymojave.data.Readings
import com.example.pipboymojave.data.Scores
import com.example.pipboymojave.data.Visibility
import com.example.pipboymojave.sensors.ScoreEngine
import com.example.pipboymojave.sensors.SensorRepository
import com.example.pipboymojave.sensors.SensorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

data class Mission(val id: String, val title: String, val subtitle: String, val reward: Int)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = LocalStore(app)
    val sensors = SensorRepository(app)
    val sensorState: StateFlow<SensorState> = sensors.state

    private val _data = MutableStateFlow(store.load())
    val data: StateFlow<AppData> = _data

    val authenticated = MutableStateFlow(false)

    // ---- levels -------------------------------------------------------------------------------

    val xpPerLevel = 1940 // XP needed for next level in the mockup (1,240 / 1,940)
    val level: Int get() = 7 + (_data.value.xp - 1240).coerceAtLeast(0) / xpPerLevel

    fun levelProgress(): Float = (_data.value.xp.toFloat() / xpPerLevel).coerceIn(0f, 1f)

    // ---- live scores --------------------------------------------------------------------------

    fun currentScores(): Scores = ScoreEngine.compute(sensorState.value)

    fun currentReadings(): Readings {
        val s = sensorState.value
        return Readings(
            light = s.lightLux, soundDb = s.soundDb, heading = s.headingDeg,
            pressure = s.pressureHpa, steps = s.steps
        )
    }

    // ---- persistence helpers ------------------------------------------------------------------

    private fun update(block: (AppData) -> AppData) {
        val next = block(_data.value)
        _data.value = next
        store.save(next)
    }

    fun savePhoto(bitmap: Bitmap): String {
        val dir = File(getApplication<Application>().filesDir, "photos").apply { mkdirs() }
        val f = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        FileOutputStream(f).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return f.absolutePath
    }

    // ---- journal ------------------------------------------------------------------------------

    fun addJournalEntry(
        title: String, observation: String, photoPath: String?, expedition: String,
        tags: List<String>, include: Set<String>, publish: Boolean
    ) {
        val all = currentReadings()
        val readings = Readings(
            light = if ("Light" in include) all.light else null,
            soundDb = if ("Sound" in include) all.soundDb else null,
            heading = if ("Compass" in include) all.heading else null,
            pressure = if ("Pressure" in include) all.pressure else null,
            steps = if ("Motion" in include) all.steps else null
        )
        val entry = JournalEntry(
            id = System.currentTimeMillis(),
            title = title.ifBlank { "Untitled" },
            observation = observation,
            photoPath = photoPath,
            expedition = expedition,
            tags = tags,
            readings = readings,
            scores = currentScores(),
            visibility = if (publish) Visibility.PUBLISHED else Visibility.PRIVATE
        )
        update { it.copy(journal = listOf(entry) + it.journal) }
        completeMission("document", 50)
    }

    // ---- ledger -------------------------------------------------------------------------------

    fun addLedgerItem(item: LedgerItem) = update { it.copy(ledger = it.ledger + item) }

    fun logUse(itemId: Long) = update { d ->
        d.copy(ledger = d.ledger.map {
            if (it.id == itemId && it.quantity > 0) it.copy(quantity = it.quantity - 1) else it
        })
    }

    fun restock(itemId: Long) = update { d ->
        d.copy(ledger = d.ledger.map { if (it.id == itemId) it.copy(quantity = it.quantity + 1) else it })
    }

    // ---- assessments & missions ---------------------------------------------------------------

    fun saveAssessment(a: Assessment) {
        update { it.copy(assessments = it.assessments + a) }
        completeMission("revisit", 40)
    }

    fun saveReport(id: Long) = update {
        if (id in it.savedReportIds) it else it.copy(savedReportIds = it.savedReportIds + id)
    }

    fun completeMission(id: String, reward: Int) = update {
        if (id in it.completedMissions) it
        else it.copy(completedMissions = it.completedMissions + id, xp = it.xp + reward)
    }

    // ---- sensors lifecycle --------------------------------------------------------------------

    override fun onCleared() {
        sensors.stop()
        super.onCleared()
    }
}
