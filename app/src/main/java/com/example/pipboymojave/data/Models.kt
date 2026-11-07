package com.example.pipboymojave.data

data class Scores(
    val stamina: Int = 0,
    val awareness: Int = 0,
    val stealth: Int = 0,
    val navigation: Int = 0,
    val environment: Int = 0
)

data class Readings(
    val light: Float? = null,
    val soundDb: Float? = null,
    val heading: Float? = null,
    val pressure: Float? = null,
    val steps: Int? = null
)

enum class Visibility { DRAFT, PRIVATE, PUBLISHED }

data class JournalEntry(
    val id: Long,
    val title: String,
    val observation: String,
    val photoPath: String? = null,
    val expedition: String = "Lab Survey",
    val tags: List<String> = emptyList(),
    val readings: Readings = Readings(),
    val scores: Scores? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val visibility: Visibility = Visibility.PRIVATE
)

data class LedgerItem(
    val id: Long,
    val name: String,
    val category: String,
    val quantity: Int,
    val minimum: Int,
    val expiry: String = "",
    val storage: String = "",
    val expedition: String = "Lab Survey",
    val notes: String = ""
) {
    val belowMinimum: Boolean get() = quantity < minimum
}

data class Assessment(
    val id: Long,
    val location: String,
    val readings: Readings,
    val photoCount: Int,
    val notes: String,
    val linkedItems: List<String>,
    val task: String,
    val due: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SharedReport(
    val id: Long,
    val title: String,
    val author: String,
    val distanceKm: Float,
    val date: String,
    val description: String,
    val pressure: Float,
    val soundDb: Float,
    val light: Float?,
    val heading: Float?,
    val photoCount: Int,
    val checklist: List<String>,
    val isTemplate: Boolean = false
)

/** Everything that is persisted locally on the device. */
data class AppData(
    val journal: List<JournalEntry> = emptyList(),
    val ledger: List<LedgerItem> = emptyList(),
    val assessments: List<Assessment> = emptyList(),
    val xp: Int = 1240,
    val completedMissions: List<String> = listOf("document"),
    val savedReportIds: List<Long> = emptyList()
)
