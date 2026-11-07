package com.example.pipboymojave.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipboymojave.AppViewModel
import com.example.pipboymojave.R
import com.example.pipboymojave.ui.components.ItemImage
import com.example.pipboymojave.data.JournalEntry
import com.example.pipboymojave.data.LedgerItem
import com.example.pipboymojave.data.Visibility
import com.example.pipboymojave.ui.components.PhotoPlaceholder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("MMM dd · HH:mm", Locale.US)
private val shortDate = SimpleDateFormat("MMM dd", Locale.US)

private fun readingChips(e: JournalEntry): List<String> = buildList {
    e.readings.light?.let { add("${it.toInt()} lx") }
    e.readings.soundDb?.let { add("≈${it.toInt()} dB") }
    e.readings.heading?.let { add("HDG ${it.toInt()}°") }
    e.readings.pressure?.let { add("${it.toInt()} hPa") }
}

// ------------------------------------------------------------------------------------------------
// Journal
// ------------------------------------------------------------------------------------------------

@Composable
fun JournalScreen(
    vm: AppViewModel, onNew: () -> Unit, onCompare: () -> Unit, bottomBar: @Composable () -> Unit
) {
    val data by vm.data.collectAsState()
    val entries = data.journal.sortedByDescending { it.timestamp }
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    val shown = if (query.isBlank()) entries else entries.filter {
        it.title.contains(query, true) || it.observation.contains(query, true) || it.tags.any { t -> t.contains(query, true) }
    }

    ScreenScaffold(
        title = "Field Journal", right = "${entries.size} entries", bottomBar = bottomBar,
        footer = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PipButton("Compare\nVisits", Modifier.weight(1f), filled = false, onClick = onCompare)
                PipButton("+ New\nEntry", Modifier.weight(1f), onClick = onNew)
            }
        }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PipChip("EXPEDITION: LAB SURVEY")
            Box(Modifier.clickable { searching = !searching }) { PipChip("SEARCH") }
        }
        if (searching) PipTextField("Search", query, { query = it }, placeholder = "Title, note or #tag")

        shown.forEachIndexed { i, e ->
            if (i == 0 && e.observation.isNotBlank()) {
                FullEntryCard(e)
            } else {
                PipPanel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("ENTRY ${"%03d".format(entryNumber(entries, e))} · ${e.title.uppercase()}", style = displayStyle(18.sp))
                            Text(
                                dateFmt.format(Date(e.timestamp)) + if (e.visibility == Visibility.PUBLISHED) " · Shared" else " · ${e.readingCount()} readings",
                                style = smallStyle(Pip.GreenDim)
                            )
                        }
                        PipChip(if (e.visibility == Visibility.PUBLISHED) "PUBLISHED" else "PRIVATE")
                    }
                }
            }
        }
    }
}

private fun JournalEntry.readingCount() = listOfNotNull(
    readings.light, readings.soundDb, readings.heading, readings.pressure, readings.steps
).size

private fun entryNumber(all: List<JournalEntry>, e: JournalEntry): Int =
    all.sortedBy { it.timestamp }.indexOf(e) + 1

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullEntryCard(e: JournalEntry) {
    PipPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ENTRY · ${e.title.uppercase()}", style = displayStyle(20.sp), modifier = Modifier.weight(1f))
            Text(dateFmt.format(Date(e.timestamp)), style = smallStyle(), modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(10.dp))
        EntryPhoto(e.photoPath, Modifier.fillMaxWidth().height(150.dp))
        Spacer(Modifier.height(10.dp))
        Text(e.observation, style = bodyStyle())
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            readingChips(e).forEach { PipChip(it) }
            e.tags.forEach { PipChip(it) }
        }
        e.scores?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                "SCORES SAVED: STA ${it.stamina} · AWR ${it.awareness} · STL ${it.stealth} · NAV ${it.navigation} · ENV ${it.environment}",
                style = smallStyle(Pip.GreenDim)
            )
        }
    }
}

@Composable
fun EntryPhoto(path: String?, modifier: Modifier) {
    val bmp = remember(path) { path?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() } }
    if (bmp != null) {
        Image(bmp.asImageBitmap(), null, modifier.border(1.dp, Pip.Border), contentScale = ContentScale.Crop)
    } else {
        PhotoPlaceholder(if (path == null) "No photo" else "Photo", modifier)
    }
}

// ------------------------------------------------------------------------------------------------
// New entry
// ------------------------------------------------------------------------------------------------

@Composable
fun NewEntryScreen(vm: AppViewModel, onClose: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var obs by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var publish by remember { mutableStateOf(false) }
    val include = remember { mutableStateListOf("Light", "Sound", "Compass", "Pressure") }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) photoPath = vm.savePhoto(bmp)
    }
    val camPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) { camera.launch(null); vm.sensors.onPermissionsChanged() }
    }

    ScreenScaffold(
        title = "New Entry", right = "Draft · saved locally",
        footer = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PipButton("Cancel", Modifier.weight(0.8f), filled = false, onClick = onClose)
                Spacer(Modifier.size(16.dp))
                PipButton("Save Entry", Modifier.weight(1f), onClick = {
                    vm.addJournalEntry(
                        title, obs, photoPath, "Lab Survey",
                        tags.split(" ", ",").map { it.trim() }.filter { it.isNotEmpty() }
                            .map { if (it.startsWith("#")) it else "#$it" },
                        include.toSet(), publish
                    )
                    onClose()
                })
            }
        }
    ) {
        PipTextField("Title", title, { title = it }, placeholder = "Server Room")
        PipTextField("Observation", obs, { obs = it }, placeholder = "What did you notice?", singleLine = false, minHeight = 110.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EntryPhoto(photoPath, Modifier.weight(1f).height(60.dp))
            PipButton("Take Photo", Modifier.weight(1f), filled = false, onClick = {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                    vm.getApplication<android.app.Application>(), android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) camera.launch(null) else camPerm.launch(android.Manifest.permission.CAMERA)
            })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                SectionLabel("Expedition")
                Spacer(Modifier.height(4.dp))
                PipPanel { Text("Lab Survey", style = bodyStyle()) }
            }
            PipTextField("Tags", tags, { tags = it }, placeholder = "#equipment", modifier = Modifier.weight(1f))
        }
        SectionLabel("Record readings")
        listOf("Light" to "Sound ≈", "Compass" to "Pressure", "Motion" to "Location").forEach { (a, b) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(a, b).forEach { label ->
                    val key = label.removeSuffix(" ≈")
                    PipCheckRow(label, key in include, { on -> if (on) include.add(key) else include.remove(key) }, Modifier.weight(1f))
                }
            }
        }
        PipCheckRow("Publish to community", publish, { publish = it })
        Text("Off by default. Entries stay private on this device.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

// ------------------------------------------------------------------------------------------------
// Compare visits
// ------------------------------------------------------------------------------------------------

@Composable
fun CompareScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsState()
    // Compare the two most recent entries that share the same title (e.g. two visits to "Server Room").
    val pair = data.journal.groupBy { it.title }.values
        .filter { it.size >= 2 }
        .maxByOrNull { g -> g.maxOf { it.timestamp } }
        ?.sortedBy { it.timestamp }?.takeLast(2)
    val a = pair?.getOrNull(0)
    val b = pair?.getOrNull(1)

    ScreenScaffold(
        title = "Compare", right = a?.title?.uppercase() ?: "",
        footer = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PipButton("Back", Modifier.weight(0.7f), filled = false, onClick = onBack)
                Spacer(Modifier.size(16.dp))
                PipButton("Export Report", Modifier.weight(1f), onClick = onBack)
            }
        }
    ) {
        if (a == null || b == null) {
            PipPanel { Text("Log at least two visits to the same location to compare them.", style = bodyStyle()) }
            return@ScreenScaffold
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PipButton("A · ${shortDate.format(Date(a.timestamp))}", Modifier.weight(1f), onClick = {})
            PipButton("B · ${shortDate.format(Date(b.timestamp))}", Modifier.weight(1f), onClick = {})
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EntryPhoto(a.photoPath, Modifier.weight(1f).height(110.dp))
            EntryPhoto(b.photoPath, Modifier.weight(1f).height(110.dp))
        }
        PipPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionLabel("Reading"); SectionLabel("A → B")
            }
            Spacer(Modifier.height(8.dp))
            DiffRow("Light", a.readings.light, b.readings.light, " lx", 0, true)
            DiffRow("Sound ≈", a.readings.soundDb, b.readings.soundDb, " dB", 0, true)
            DiffRow("Pressure", a.readings.pressure, b.readings.pressure, "", 1, true)
            DiffRow("Heading", a.readings.heading, b.readings.heading, "°", 0, false)
            DiffRow("Steps", a.readings.steps?.toFloat(), b.readings.steps?.toFloat(), "", 0, false)
        }
        val sa = a.scores
        val sb = b.scores
        if (sa != null && sb != null) {
            PipPanel {
                SectionLabel("Saved scores A → B")
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("STA ${sa.stamina}→${sb.stamina}", style = smallStyle().copy(fontSize = 13.sp))
                    Text("AWR ${sa.awareness}→${sb.awareness}", style = smallStyle().copy(fontSize = 13.sp))
                    Text("STL ${sa.stealth}→${sb.stealth}", style = smallStyle().copy(fontSize = 13.sp))
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NAV ${sa.navigation}→${sb.navigation}", style = smallStyle().copy(fontSize = 13.sp))
                    Text("ENV ${sa.environment}→${sb.environment}", style = smallStyle().copy(fontSize = 13.sp))
                }
            }
        }
        PipPanel(borderColor = Pip.Amber) {
            Text(
                "Placement differs between visits. Light and sound may not be directly comparable.",
                style = bodyStyle(Pip.Amber)
            )
        }
    }
}

@Composable
private fun DiffRow(label: String, a: Float?, b: Float?, unit: String, decimals: Int, showDelta: Boolean) {
    fun fmt(v: Float?) = if (v == null) "--" else if (decimals == 0) String.format(Locale.US, "%,d", v.toInt()) else String.format(Locale.US, "%.${decimals}f", v)
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label.uppercase(), style = displayStyle(20.sp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${fmt(a)} → ${fmt(b)}$unit", style = displayStyle(18.sp).copy(fontFamily = com.example.pipboymojave.ui.theme.BodyFont, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
            if (showDelta && a != null && b != null) {
                val d = b - a
                Spacer(Modifier.size(8.dp))
                val txt = (if (d >= 0) "+" else "−") + (if (decimals == 0) kotlin.math.abs(d).toInt().toString() else String.format(Locale.US, "%.${decimals}f", kotlin.math.abs(d)))
                Text(txt, style = displayStyle(18.sp, if (d >= 0) Pip.Amber else Pip.Green).copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Ledger
// ------------------------------------------------------------------------------------------------

@Composable
fun LedgerScreen(vm: AppViewModel, onAdd: () -> Unit, bottomBar: @Composable () -> Unit) {
    val data by vm.data.collectAsState()
    val below = data.ledger.count { it.belowMinimum }
    var logging by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Resource Ledger", right = "${data.ledger.size} items", bottomBar = bottomBar,
        footer = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PipButton(if (logging) "Done" else "Log Use", Modifier.weight(0.8f), filled = false, onClick = { logging = !logging })
                PipButton("+ Add Item", Modifier.weight(1f), onClick = onAdd)
            }
        }
    ) {
        if (below > 0) {
            PipPanel(borderColor = Pip.Amber) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$below BELOW MINIMUM", style = displayStyle(20.sp, Pip.Amber))
                    Text("Checklist: Lab Survey", style = smallStyle(Pip.Amber))
                }
            }
        }
        data.ledger.forEach { item -> LedgerRow(item, logging, { vm.logUse(item.id) }, { vm.restock(item.id) }) }
        Text("Quantities and item identity are confirmed by you, not detected by camera.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

/** Known items get their own picture; anything else falls back to a category picture. */
private fun itemImage(item: LedgerItem): Int = when (item.name.trim().lowercase()) {
    "radaway" -> R.drawable.img_radaway
    "scrap parts" -> R.drawable.img_scrap_parts
    "matches" -> R.drawable.img_matches
    "olde royale" -> R.drawable.img_olde_royale
    else -> when (item.category) {
        "Medical" -> R.drawable.img_medical_items
        "Tools" -> R.drawable.img_rad_barrel
        else -> R.drawable.img_vault_lunchbox
    }
}

@Composable
private fun LedgerRow(item: LedgerItem, logging: Boolean, onUse: () -> Unit, onRestock: () -> Unit) {
    val low = item.belowMinimum
    PipPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).border(1.dp, Pip.Border).padding(4.dp), contentAlignment = Alignment.Center) {
                ItemImage(itemImage(item), Modifier.fillMaxSize())
            }
            Column(Modifier.weight(1f)) {
                Text(item.name.uppercase(), style = displayStyle(20.sp))
                val sub = buildList {
                    add(item.storage.ifBlank { item.category })
                    when {
                        item.expiry.isNotBlank() -> add("Exp ${item.expiry}")
                        item.notes.isNotBlank() -> add(item.notes)
                        else -> add("No expiry")
                    }
                }.joinToString(" · ")
                Text(sub, style = smallStyle(Pip.GreenDim))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.quantity}", style = displayStyle(24.sp, if (low) Pip.Amber else Pip.Green))
                Text("MIN ${item.minimum}", style = smallStyle(if (low) Pip.Amber else Pip.Green))
            }
        }
        if (logging) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PipButton("− Use 1", Modifier.weight(1f), filled = false, enabled = item.quantity > 0, onClick = onUse)
                PipButton("+ Restock 1", Modifier.weight(1f), filled = false, onClick = onRestock)
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Add item
// ------------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(vm: AppViewModel, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Medical") }
    var qty by remember { mutableStateOf(3) }
    var min by remember { mutableStateOf(2) }
    var expiry by remember { mutableStateOf("") }
    var storage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ScreenScaffold(
        title = "Add Item", right = "Ledger · local",
        footer = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PipButton("Cancel", Modifier.weight(0.8f), filled = false, onClick = onClose)
                Spacer(Modifier.size(16.dp))
                PipButton("Save Item", Modifier.weight(1f), enabled = name.isNotBlank(), onClick = {
                    vm.addLedgerItem(
                        LedgerItem(System.currentTimeMillis(), name.trim(), category, qty, min, expiry, storage, "Lab Survey", notes)
                    )
                    onClose()
                })
            }
        }
    ) {
        PipTextField("Item name", name, { name = it }, placeholder = "Water purification tablets")
        SectionLabel("Category")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Medical", "Tools", "Food & Water", "Gear", "Other").forEach { c ->
                Box(
                    Modifier.border(1.dp, Pip.Border)
                        .then(if (c == category) Modifier.background(Pip.Green) else Modifier)
                        .clickable { category = c }.padding(horizontal = 14.dp, vertical = 12.dp)
                ) { Text(c, style = bodyStyle(if (c == category) Pip.BgDeep else Pip.Green).copy(fontSize = 13.sp)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stepper("Quantity", qty, { qty = it }, Modifier.weight(1f))
            Stepper("Minimum stock", min, { min = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PipTextField("Expiry date", expiry, { expiry = it }, placeholder = "yyyy-mm", modifier = Modifier.weight(1f))
            PipTextField("Storage", storage, { storage = it }, placeholder = "Backpack", modifier = Modifier.weight(1f))
        }
        SectionLabel("Link to expedition")
        PipPanel { Text("Lab Survey", style = bodyStyle()) }
        PipPanel {
            Text("Label/receipt scanning is not implemented yet. Enter the name and quantity yourself.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
        }
        PipTextField("Notes", notes, { notes = it }, placeholder = "Inspect monthly")
    }
}

@Composable
private fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionLabel(label)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(44.dp).border(2.dp, Pip.Green).clickable { onChange((value - 1).coerceAtLeast(0)) }, contentAlignment = Alignment.Center) {
                Text("−", style = displayStyle(22.sp))
            }
            Text("$value", style = displayStyle(22.sp))
            Box(Modifier.size(44.dp).border(2.dp, Pip.Green).clickable { onChange(value + 1) }, contentAlignment = Alignment.Center) {
                Text("+", style = displayStyle(22.sp))
            }
        }
    }
}
