package com.example.pipboymojave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipboymojave.AppViewModel
import com.example.pipboymojave.R
import com.example.pipboymojave.ui.components.ItemImage
import androidx.compose.foundation.layout.fillMaxSize
import com.example.pipboymojave.data.Assessment
import com.example.pipboymojave.data.Readings
import com.example.pipboymojave.data.SampleData
import com.example.pipboymojave.data.SharedReport
import com.example.pipboymojave.ui.components.PhotoPlaceholder
import com.example.pipboymojave.ui.components.PipBar
import com.example.pipboymojave.ui.components.PipButton
import com.example.pipboymojave.ui.components.PipCheckRow
import com.example.pipboymojave.ui.components.PipChip
import com.example.pipboymojave.ui.components.PipPanel
import com.example.pipboymojave.ui.components.PipTextField
import com.example.pipboymojave.ui.components.ScreenScaffold
import com.example.pipboymojave.ui.components.SectionLabel
import com.example.pipboymojave.ui.components.bodyStyle
import com.example.pipboymojave.ui.components.displayStyle
import com.example.pipboymojave.ui.components.smallStyle
import com.example.pipboymojave.ui.theme.Pip
import java.util.Locale

// ------------------------------------------------------------------------------------------------
// Missions
// ------------------------------------------------------------------------------------------------

@Composable
fun MissionsScreen(vm: AppViewModel, onStartAssessment: () -> Unit, bottomBar: @Composable () -> Unit) {
    val data by vm.data.collectAsState()
    val lowStock = data.ledger.count { it.belowMinimum }
    val level = vm.level

    ScreenScaffold(title = "Missions", right = "Rank: Wanderer", bottomBar = bottomBar) {
        PipPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ItemImage(R.drawable.img_avatar, Modifier.size(90.dp).clip(CircleShape))
                Column(Modifier.weight(1f)) {
                    Text("Level $level", style = ScriptStyle)
                    Text("${String.format(Locale.US, "%,d", data.xp)} / ${String.format(Locale.US, "%,d", vm.xpPerLevel)} XP", style = smallStyle().copy(fontSize = 13.sp))
                    Spacer(Modifier.height(8.dp))
                    PipBar(vm.levelProgress(), height = 14.dp)
                }
            }
        }
        MissionRow(
            "Document a new location", "Note + photo · " + if ("document" in data.completedMissions) "Done" else "Open", 50
        )
        MissionRow("Resolve low-stock items", if (lowStock == 0) "All items stocked" else "$lowStock flagged", 30)
        MissionRow(
            "Revisit and compare",
            "Server Room · " + data.journal.count { it.title == "Server Room" } + " visits", 40
        )
        PipPanel(borderColor = Pip.Green) {
            Text("FIELD ASSESSMENT", style = displayStyle(20.sp))
            Spacer(Modifier.height(6.dp))
            Text(
                "Server Room: 2 photos, lighting + sound readings, linked to Scrap Parts. Maintenance task due Nov 20.",
                style = bodyStyle()
            )
            Spacer(Modifier.height(12.dp))
            PipButton("Start Assessment", Modifier.fillMaxWidth(), onClick = onStartAssessment)
        }
        PipPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("REWARD CACHE", style = displayStyle(20.sp))
                Text("COSMETIC UNLOCKS", style = smallStyle())
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    Triple("Rad Badge", 8, R.drawable.img_radaway), Triple("Scrap Frame", 9, R.drawable.img_scrap_parts),
                    Triple("Spark Icon", 10, R.drawable.img_matches), Triple("Olde Theme", 12, R.drawable.img_olde_royale)
                ).forEach { (n, lv, img) ->
                    val unlocked = level >= lv
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(Modifier.size(64.dp).border(1.dp, Pip.Border).padding(4.dp), contentAlignment = Alignment.Center) {
                            ItemImage(img, Modifier.fillMaxSize(), alpha = if (unlocked) 1f else 0.55f)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(n.uppercase(), style = smallStyle().copy(fontSize = 10.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("LVL $lv", style = smallStyle().copy(fontSize = 10.sp))
                    }
                }
            }
        }
        Text("Rewards come from completed actions only.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

@Composable
private fun MissionRow(title: String, sub: String, reward: Int) {
    PipPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), style = displayStyle(20.sp))
                Text(sub, style = smallStyle(Pip.GreenDim))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemImage(R.drawable.img_caps, Modifier.size(40.dp))
                Text("+$reward", style = displayStyle(24.sp))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Assessment (4-step wizard; step 2 matches the mockup)
// ------------------------------------------------------------------------------------------------

@Composable
fun AssessmentScreen(vm: AppViewModel, onClose: () -> Unit) {
    val sensors by vm.sensors.state.collectAsState()
    val data by vm.data.collectAsState()
    var step by remember { mutableStateOf(1) }
    val record = remember { mutableStateListOf("Light", "Sound", "Pressure") }
    var notes by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    val linked = remember { mutableStateListOf<String>() }

    ScreenScaffold(
        title = "Assessment", right = "Step $step of 4",
        footer = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PipButton(if (step == 1) "Cancel" else "Back", Modifier.weight(0.8f), filled = false, onClick = {
                    if (step == 1) onClose() else step--
                })
                Spacer(Modifier.size(16.dp))
                PipButton(if (step == 4) "Finish" else "Next", Modifier.weight(1f), onClick = {
                    if (step < 4) step++ else {
                        vm.saveAssessment(
                            Assessment(
                                System.currentTimeMillis(), "Server Room",
                                Readings(
                                    light = if ("Light" in record) sensors.lightLux else null,
                                    soundDb = if ("Sound" in record) sensors.soundDb else null,
                                    pressure = if ("Pressure" in record) sensors.pressureHpa else null,
                                    heading = if ("Heading" in record) sensors.headingDeg else null
                                ),
                                photoCount = 2, notes = notes, linkedItems = linked.toList(), task = task, due = due
                            )
                        )
                        onClose()
                    }
                })
            }
        }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SERVER ROOM", style = displayStyle(22.sp))
            PipChip("LIVE")
        }
        PipBar(step / 4f, height = 14.dp)

        when (step) {
            1 -> {
                SectionLabel("Location")
                PipPanel { Text("Server Room · Lab Survey", style = bodyStyle()) }
                Text("Pick what to inspect. Sensors give context; your photos and notes are the evidence.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
            }
            2 -> {
                SectionLabel("Readings to record")
                val items = listOf(
                    "Light" to (sensors.lightLux?.let { "Light ${it.toInt()} lx" } ?: "Light --"),
                    "Sound" to (sensors.soundDb?.let { "Sound ≈${it.toInt()} dB" } ?: "Sound --"),
                    "Pressure" to (sensors.pressureHpa?.let { "${it.toInt()} hPa" } ?: "-- hPa"),
                    "Heading" to (sensors.headingDeg?.let { "HDG ${it.toInt()}°" } ?: "HDG --")
                )
                items.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (key, label) ->
                            PipCheckRow(label, key in record, { on -> if (on) record.add(key) else record.remove(key) }, Modifier.weight(1f))
                        }
                    }
                }
                PipPanel { Text("2 photos attached", style = bodyStyle()) }
                PipTextField("Inspection notes", notes, { notes = it }, placeholder = "Visible damage, conditions", singleLine = false, minHeight = 80.dp)
            }
            3 -> {
                SectionLabel("Link equipment")
                data.ledger.forEach { item ->
                    PipCheckRow(item.name, item.name in linked, { on -> if (on) linked.add(item.name) else linked.remove(item.name) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PipTextField("Maintenance task", task, { task = it }, placeholder = "Replace casing", modifier = Modifier.weight(1f))
                    PipTextField("Due", due, { due = it }, placeholder = "mm/dd/yyyy", modifier = Modifier.weight(1f))
                }
            }
            else -> {
                SectionLabel("Review")
                PipPanel {
                    Text("Recorded: ${record.joinToString(", ")}", style = bodyStyle())
                    Spacer(Modifier.height(6.dp))
                    Text("Linked: ${if (linked.isEmpty()) "none" else linked.joinToString(", ")}", style = bodyStyle())
                    Spacer(Modifier.height(6.dp))
                    Text("Task: ${task.ifBlank { "none" }} ${if (due.isNotBlank()) "· due $due" else ""}", style = bodyStyle())
                }
            }
        }
        Text("Sensors cannot diagnose faults. Your photos and notes are the evidence.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

// ------------------------------------------------------------------------------------------------
// Community (sample data only; no backend)
// ------------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(vm: AppViewModel, onOpen: (Long) -> Unit, bottomBar: @Composable () -> Unit) {
    val data by vm.data.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Nearby") }
    var reports = SampleData.community
    reports = when (filter) {
        "Latest" -> reports.sortedByDescending { it.id }
        "Saved" -> reports.filter { it.id in data.savedReportIds }
        else -> reports.sortedBy { it.distanceKm }
    }
    if (query.isNotBlank()) reports = reports.filter { it.title.contains(query, true) }

    ScreenScaffold(title = "Community", right = "Shared network", bottomBar = bottomBar) {
        PipTextField("Search shared reports", query, { query = it }, placeholder = "Place, tag or checklist")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Nearby", "Latest", "Saved").forEach { f ->
                Box(
                    Modifier.weight(1f).height(52.dp).border(1.dp, Pip.Border)
                        .then(if (f == filter) Modifier.background(Pip.Green) else Modifier)
                        .clickable { filter = f },
                    contentAlignment = Alignment.Center
                ) { Text(f, style = bodyStyle(if (f == filter) Pip.BgDeep else Pip.Green)) }
            }
        }
        if (reports.isEmpty()) PipPanel { Text("Nothing here yet.", style = bodyStyle()) }
        reports.forEach { r ->
            PipPanel(onClick = { onOpen(r.id) }) {
                Text(r.title.uppercase(), style = displayStyle(20.sp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${r.author} · ${r.distanceKm} km", style = smallStyle().copy(fontSize = 13.sp))
                    Text(r.date, style = smallStyle().copy(fontSize = 13.sp))
                }
                if (r.photoCount > 0 && r.id == 1L) {
                    Spacer(Modifier.height(8.dp))
                    PhotoPlaceholder("Photo", Modifier.fillMaxWidth().height(90.dp))
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!r.isTemplate) {
                        if (r.light != null && r.id != 1L) PipChip("${r.light.toInt()} lx")
                        if (r.id == 1L) { PipChip("${r.pressure.toInt()} hPa"); PipChip("≈${r.soundDb.toInt()} dB") }
                        if (r.heading != null && r.id != 1L) PipChip("HDG ${r.heading.toInt()}°")
                        if (r.photoCount > 0 && r.id != 1L) PipChip("${r.photoCount} photos")
                    } else PipChip("Mission template")
                    if (r.checklist.isNotEmpty()) PipChip("Checklist · ${r.checklist.size}")
                    if (r.id in data.savedReportIds) PipChip("SAVED OFFLINE")
                }
            }
        }
        Text(
            "Only reports their authors chose to publish appear here. Private notes and raw logs never do.",
            style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp)
        )
    }
}

@Composable
fun SharedReportScreen(vm: AppViewModel, reportId: Long, onBack: () -> Unit, onCompare: () -> Unit, bottomBar: @Composable () -> Unit) {
    val data by vm.data.collectAsState()
    val r: SharedReport = SampleData.community.firstOrNull { it.id == reportId } ?: run { onBack(); return }
    val checked = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }
    val items = if (expanded) r.checklist else r.checklist.take(2)

    ScreenScaffold(title = "Shared Report", right = "Community", bottomBar = bottomBar) {
        Text(r.title.uppercase(), style = displayStyle(24.sp))
        Text("${r.author} · ${r.date} · ${r.distanceKm} km away", style = smallStyle(Pip.GreenDim))
        PhotoPlaceholder("Photo · 1 of ${r.photoCount.coerceAtLeast(1)}", Modifier.fillMaxWidth().height(150.dp))
        Text(r.description, style = bodyStyle())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PipChip("${r.pressure.toInt()} hPa"); PipChip("≈${r.soundDb.toInt()} dB")
            r.light?.let { PipChip("${it.toInt()} lx") }; r.heading?.let { PipChip("HDG ${it.toInt()}°") }
        }
        if (r.checklist.isNotEmpty()) {
            PipPanel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CHECKLIST", style = displayStyle(20.sp))
                    Text("${r.checklist.size} items", style = smallStyle())
                }
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    PipCheckRow(item, item in checked, { on -> if (on) checked.add(item) else checked.remove(item) })
                    Spacer(Modifier.height(6.dp))
                }
                if (!expanded && r.checklist.size > 2) {
                    Text("+${r.checklist.size - 2} more", style = smallStyle(), modifier = Modifier.clickable { expanded = true })
                }
            }
        }
        Text("Readings are the author's own and depend on their device and placement.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PipButton(if (r.id in data.savedReportIds) "Saved" else "Save\nOffline", Modifier.weight(1f), filled = false, onClick = { vm.saveReport(r.id) })
            PipButton("Use\nChecklist", Modifier.weight(1f), onClick = { expanded = true })
        }
        PipButton("Compare with my visit", Modifier.fillMaxWidth(), filled = false, onClick = onCompare)
    }
}
