package com.example.pipboymojave.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipboymojave.AppViewModel
import com.example.pipboymojave.sensors.headingLabel
import com.example.pipboymojave.ui.components.CrtBackground
import com.example.pipboymojave.ui.components.ItemImage
import com.example.pipboymojave.ui.components.PipButton
import com.example.pipboymojave.ui.components.PipBar
import com.example.pipboymojave.ui.components.PipPanel
import com.example.pipboymojave.ui.components.ScreenScaffold
import com.example.pipboymojave.ui.components.bodyStyle
import com.example.pipboymojave.ui.components.displayStyle
import com.example.pipboymojave.ui.components.smallStyle
import com.example.pipboymojave.ui.theme.BodyFont
import com.example.pipboymojave.ui.theme.Pip
import com.example.pipboymojave.ui.theme.ScriptFont
import java.util.Locale

val ScriptStyle = TextStyle(
    fontFamily = ScriptFont, fontWeight = FontWeight.Normal,
    fontSize = 30.sp, color = Pip.Amber
)

// ------------------------------------------------------------------------------------------------
// Bottom navigation
// ------------------------------------------------------------------------------------------------

val Tabs = listOf(
    "stat" to "STAT", "sens" to "SENS", "jrnl" to "JRNL",
    "ldgr" to "LDGR", "msns" to "MSNS", "comm" to "COMM"
)

@Composable
fun PipBottomBar(selected: String, onSelect: (String) -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(2.dp).background(Pip.Green))
        Row(Modifier.fillMaxWidth().height(56.dp).background(Pip.BgDeep)) {
            Tabs.forEach { (route, label) ->
                val sel = route == selected
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .background(if (sel) Pip.Green else Color.Transparent)
                        .clickable { onSelect(route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = smallStyle(if (sel) Pip.BgDeep else Pip.Green).copy(fontSize = 13.sp, letterSpacing = 1.sp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Authenticate
// ------------------------------------------------------------------------------------------------

@Composable
fun AuthScreen(onAuthenticate: () -> Unit, errorMessage: String?) {
    CrtBackground {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            ItemImage(com.example.pipboymojave.R.drawable.img_pipboy, Modifier.fillMaxWidth().height(170.dp))
            Spacer(Modifier.height(28.dp))
            Text("PERSONAL FIELD TERMINAL", style = smallStyle().copy(fontSize = 14.sp, letterSpacing = 3.sp))
            Spacer(Modifier.height(20.dp))
            Text("PIP-BOY", style = displayStyle(60.sp), textAlign = TextAlign.Center)
            Text("MOJAVE", style = displayStyle(56.sp), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("Begin Again.", style = ScriptStyle.copy(fontSize = 34.sp))
            Spacer(Modifier.weight(1f))
            if (errorMessage != null) {
                Text(errorMessage, style = smallStyle(Pip.Amber), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }
            PipButton("Authenticate", Modifier.fillMaxWidth(), onClick = onAuthenticate)
            Spacer(Modifier.height(14.dp))
            Text(
                "Biometric unlock protects journal and ledger records stored locally on this device.",
                style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Text("MONITOR · EXPLORE · LEVEL UP", style = smallStyle().copy(fontSize = 12.sp, letterSpacing = 2.sp))
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Status
// ------------------------------------------------------------------------------------------------

@Composable
fun StatusScreen(vm: AppViewModel, bottomBar: @Composable () -> Unit) {
    val sensors by vm.sensors.state.collectAsState()
    val data by vm.data.collectAsState()
    val scores = com.example.pipboymojave.sensors.ScoreEngine.compute(sensors)

    ScreenScaffold(
        title = "Status", right = "Courier · LVL ${vm.level}", bottomBar = bottomBar
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ItemImage(com.example.pipboymojave.R.drawable.img_avatar, Modifier.size(110.dp).clip(CircleShape))
            Column(Modifier.weight(1f)) {
                Text("The Courier", style = ScriptStyle)
                Spacer(Modifier.height(4.dp))
                Text("HP 100/100 · CAPS ${String.format(Locale.US, "%,d", data.xp)} XP", style = smallStyle().copy(fontSize = 12.sp))
                Spacer(Modifier.height(6.dp))
                PipBar(vm.levelProgress(), height = 14.dp)
                Spacer(Modifier.height(4.dp))
                Text("${(vm.levelProgress() * 100).toInt()}% TO LVL ${vm.level + 1}", style = smallStyle())
            }
        }
        AttributeCard("Stamina", "ACCEL · STEPS · GPS · BARO", scores.stamina)
        AttributeCard("Awareness", "LIGHT · MIC · CAMERA · GPS", scores.awareness)
        AttributeCard("Stealth", "ACCEL · GYRO · MIC · PROXIMITY", scores.stealth)
        AttributeCard("Navigation", "GPS · MAGNET · GYRO · ACCEL", scores.navigation)
        AttributeCard("Environment", "LIGHT · MIC · BARO · GPS", scores.environment)
        Text("Illustrative game scores. Not health, fitness or safety metrics.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

@Composable
private fun AttributeCard(name: String, sources: String, value: Int) {
    PipPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(name.uppercase(), style = displayStyle(22.sp))
            Text("$value", style = displayStyle(22.sp).copy(fontWeight = FontWeight.Normal, fontFamily = BodyFont))
        }
        Text(sources, style = smallStyle(Pip.GreenDim))
        Spacer(Modifier.height(8.dp))
        PipBar(value / 100f)
    }
}

// ------------------------------------------------------------------------------------------------
// Sensor array
// ------------------------------------------------------------------------------------------------

@Composable
fun SensorScreen(vm: AppViewModel, onRequestPermissions: () -> Unit, bottomBar: @Composable () -> Unit) {
    val s by vm.sensors.state.collectAsState()

    fun f2(v: Float?, signed: Boolean = false): String =
        if (v == null) "--" else String.format(Locale.US, if (signed) "%+.2f" else "%.2f", v)

    ScreenScaffold(title = "Sensor Array", right = "Live · 10 Hz", bottomBar = bottomBar) {
        PipPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ACCELEROMETER", style = displayStyle(18.sp))
                Text("m/s²", style = smallStyle())
            }
            Spacer(Modifier.height(8.dp))
            AccelGraph(s.accelHistory)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("X ${f2(s.accel?.first, true)}", style = smallStyle().copy(fontSize = 13.sp))
                Text("Y ${f2(s.accel?.second, true)}", style = smallStyle(Pip.Amber).copy(fontSize = 13.sp))
                Text("Z ${f2(s.accel?.third, true)}", style = smallStyle().copy(fontSize = 13.sp))
            }
        }
        SensorRow(
            "Gyroscope",
            s.gyro?.let { "X ${f2(it.first)} · Y ${f2(it.second)} · Z ${f2(it.third)} rad/s" } ?: "Not available"
        )
        SensorRow(
            "Magnetometer",
            if (s.headingDeg != null && s.magnetUt != null)
                "HDG ${s.headingDeg!!.toInt()}° ${headingLabel(s.headingDeg!!)} · ${s.magnetUt!!.toInt()} µT"
            else "Not available"
        )
        SensorRow("Step Counter", s.steps?.let { String.format(Locale.US, "%,d", it) } ?: "Permission missing",
            warn = s.steps == null, onClick = if (s.steps == null) onRequestPermissions else null)
        SensorRow("Barometer", s.pressureHpa?.let { String.format(Locale.US, "%.1f hPa", it) } ?: "Not available")
        SensorRow("Ambient Light", s.lightLux?.let { "${it.toInt()} lx" } ?: "Not available")
        SensorRow("Proximity", when (s.proximityNear) { true -> "NEAR"; false -> "FAR"; null -> "Not available" })
        SensorRow("Microphone", s.soundDb?.let { "≈ ${it.toInt()} dB · estimate" } ?: "Permission missing",
            warn = s.soundDb == null, onClick = if (s.soundDb == null) onRequestPermissions else null)
        SensorRow(
            "GPS / Location",
            when {
                !s.locationGranted -> "Permission missing"
                s.locationAccuracyM == null -> "Searching…"
                else -> "±${s.locationAccuracyM!!.toInt()} m"
            },
            warn = !s.locationGranted, onClick = if (!s.locationGranted) onRequestPermissions else null
        )
        Text("Readings are direct measurements. Scores on Status are game estimates.", style = bodyStyle(Pip.GreenDim).copy(fontSize = 12.sp))
    }
}

@Composable
private fun SensorRow(name: String, value: String, warn: Boolean = false, onClick: (() -> Unit)? = null) {
    val c = if (warn) Pip.Amber else Pip.Green
    PipPanel(borderColor = if (warn) Pip.Amber else Pip.Border, onClick = onClick) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(name.uppercase(), style = displayStyle(18.sp, c))
            Text(value, style = bodyStyle(c).copy(fontSize = 13.sp), textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 12.dp))
        }
    }
}

@Composable
private fun AccelGraph(history: List<Triple<Float, Float, Float>>) {
    Canvas(Modifier.fillMaxWidth().height(90.dp)) {
        if (history.size < 2) return@Canvas
        val lo = -12f
        val hi = 12f
        fun y(v: Float) = size.height - ((v.coerceIn(lo, hi) - lo) / (hi - lo)) * size.height
        val stepX = size.width / (history.size - 1)
        fun draw(sel: (Triple<Float, Float, Float>) -> Float, color: Color, dashed: Boolean = false) {
            for (i in 1 until history.size) {
                drawLine(
                    color, Offset((i - 1) * stepX, y(sel(history[i - 1]))), Offset(i * stepX, y(sel(history[i]))),
                    strokeWidth = 2.5f,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null
                )
            }
        }
        draw({ it.first }, Pip.Green, dashed = true)
        draw({ it.second }, Pip.Amber)
        draw({ it.third }, Pip.Green)
    }
}
