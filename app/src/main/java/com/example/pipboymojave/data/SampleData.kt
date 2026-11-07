package com.example.pipboymojave.data

import java.util.Calendar

object SampleData {

    private fun ts(month: Int, day: Int, hour: Int, minute: Int, year: Int = 2026): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Seed data so the app looks like the mockup on first launch. */
    fun seed(): AppData = AppData(
        journal = listOf(
            JournalEntry(
                id = 10, title = "Entrance", observation = "Main entrance clear. Door sticks.",
                tags = listOf("#access"),
                readings = Readings(light = 260f, soundDb = 40f, heading = 205f, pressure = 1011f, steps = 3200),
                timestamp = ts(11, 5, 13, 48), visibility = Visibility.PUBLISHED
            ),
            JournalEntry(
                id = 11, title = "Hallway", observation = "Corridor lighting flickers.",
                tags = listOf("#lighting"),
                readings = Readings(light = 190f, soundDb = 35f, heading = 90f),
                timestamp = ts(11, 5, 14, 5), visibility = Visibility.PRIVATE
            ),
            JournalEntry(
                id = 12, title = "Server Room",
                observation = "Two workstations with cracked casings near the window. Lighting uneven; fan noise loud.",
                tags = listOf("#equipment"),
                readings = Readings(light = 312f, soundDb = 38f, heading = 214f, pressure = 1009.4f, steps = 4812),
                scores = Scores(72, 58, 41, 66, 63),
                timestamp = ts(11, 5, 14, 32), visibility = Visibility.PRIVATE
            ),
            // Earlier visit used by the Compare screen (A · Oct 22)
            JournalEntry(
                id = 9, title = "Server Room", observation = "Baseline visit, sensor placed on desk.",
                tags = listOf("#equipment"),
                readings = Readings(light = 280f, soundDb = 41f, heading = 210f, pressure = 1012.1f, steps = 3904),
                scores = Scores(68, 55, 44, 61, 60),
                timestamp = ts(10, 22, 10, 10), visibility = Visibility.PRIVATE
            )
        ),
        ledger = listOf(
            LedgerItem(1, "RadAway", "Medical", 1, 2, "2027-03", "Medical kit"),
            LedgerItem(2, "Scrap Parts", "Tools", 6, 4, "", "Toolbox", notes = "Repair due Nov 20"),
            LedgerItem(3, "Matches", "Gear", 2, 3, "", "Backpack"),
            LedgerItem(4, "Olde Royale", "Other", 3, 1, "", "Shelf B")
        ),
        xp = 1240,
        completedMissions = listOf("document")
    )

    val community: List<SharedReport> = listOf(
        SharedReport(
            1, "Abandoned Gymnasium Survey", "@courier_alba", 2.1f, "Nov 03",
            "Roof partly collapsed on the east side. Water staining along the walls. Lighting drops sharply past the second door.",
            1008f, 44f, 96f, 180f, 4,
            listOf(
                "Flashlight", "First-aid kit", "Gloves", "Dust mask", "Water", "Spare batteries",
                "Notebook", "Phone power bank", "Rope", "Whistle", "Multi-tool", "Snacks"
            )
        ),
        SharedReport(
            2, "Rooftop Light Readings", "@mesa_walker", 4.8f, "Nov 01",
            "Open rooftop, clear sky. Readings taken at midday.",
            1010f, 52f, 540f, 92f, 3, emptyList()
        ),
        SharedReport(
            3, "Trail Supply Checklist", "@dustline", 7.3f, "Oct 28",
            "Reusable supply checklist for day trips.",
            1011f, 40f, null, null, 0,
            List(18) { "Item ${it + 1}" }, isTemplate = true
        )
    )
}
