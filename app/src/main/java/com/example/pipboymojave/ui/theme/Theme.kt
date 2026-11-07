package com.example.pipboymojave.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.pipboymojave.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Pip {
    val Bg = Color(0xFF031A0F)
    val BgDeep = Color(0xFF020F08)
    val Green = Color(0xFF4DFF8C)
    val GreenDim = Color(0xFF2FBF6A)
    val Border = Color(0xFF1E8A4C)
    val Panel = Color(0x331E8A4C)
    val Amber = Color(0xFFFFB347)
    val Hatch = Color(0x2236D676)
}

// Each font is a single weight, so register it for every weight to stop Android faking bold.
private fun single(res: Int) = FontFamily(
    Font(res, FontWeight.Normal), Font(res, FontWeight.Medium), Font(res, FontWeight.SemiBold),
    Font(res, FontWeight.Bold), Font(res, FontWeight.ExtraBold), Font(res, FontWeight.Black)
)

/** Headings, attribute names, big numbers. */
val DisplayFont = single(R.font.gothic_821_condensed)
/** Body text, small labels, readings. */
val BodyFont = single(R.font.overseer_oblique)
/** Amber script: "Begin Again.", "The Courier", "Level 7". */
val ScriptFont = single(R.font.sf_americana_dreams_bold)
/** Wide caps for buttons and the app title. */
val ButtonFont = single(R.font.add_city_boy)

private val PipTypography = Typography(
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 1.sp, color = Pip.Green),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 1.sp, color = Pip.Green),
    titleMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp, color = Pip.Green),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontSize = 13.sp, color = Pip.Green),
    bodySmall = TextStyle(fontFamily = BodyFont, fontSize = 11.sp, letterSpacing = 1.sp, color = Pip.GreenDim),
    labelLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 3.sp)
)

@Composable
fun PipBoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pip.Green, onPrimary = Pip.BgDeep,
            background = Pip.Bg, onBackground = Pip.Green,
            surface = Pip.Bg, onSurface = Pip.Green,
            secondary = Pip.Amber
        ),
        typography = PipTypography,
        content = content
    )
}
