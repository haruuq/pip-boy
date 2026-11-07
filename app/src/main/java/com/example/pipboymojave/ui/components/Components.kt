package com.example.pipboymojave.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.example.pipboymojave.ui.theme.BodyFont
import com.example.pipboymojave.ui.theme.ButtonFont
import com.example.pipboymojave.ui.theme.DisplayFont
import com.example.pipboymojave.ui.theme.Pip

/** CRT-style background: green gradient with faint scanlines. */
@Composable
fun CrtBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pip.Bg, Pip.BgDeep)))
            .drawBehind {
                val step = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(Color(0x14000000), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                    y += step
                }
            }
    ) { content() }
}

/** Standard screen: header bar + scrollable body. Bottom bar is supplied by the scaffold. */
@Composable
fun ScreenScaffold(
    title: String,
    right: String = "",
    bottomBar: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    CrtBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title.uppercase(), style = displayStyle(28.sp))
                if (right.isNotEmpty()) Text(right.uppercase(), style = smallStyle(), modifier = Modifier.padding(bottom = 4.dp))
            }
            Box(Modifier.fillMaxWidth().height(2.dp).background(Pip.Green))
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
            footer()
            Box(Modifier.navigationBarsPadding()) { bottomBar() }
        }
    }
}

fun displayStyle(size: androidx.compose.ui.unit.TextUnit, color: Color = Pip.Green) = TextStyle(
    fontFamily = DisplayFont, fontWeight = FontWeight.Black, fontSize = size, color = color, letterSpacing = 1.sp
)

fun smallStyle(color: Color = Pip.Green) = TextStyle(
    fontFamily = BodyFont, fontSize = 11.sp, color = color, letterSpacing = 1.sp
)

fun bodyStyle(color: Color = Pip.Green) = TextStyle(fontFamily = BodyFont, fontSize = 13.sp, color = color)

@Composable
fun PipPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = Pip.Border,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier.fillMaxWidth()
        .background(Pip.Panel)
        .border(1.dp, borderColor)
    Column(
        (if (onClick != null) base.clickable(onClick = onClick) else base).padding(14.dp),
        content = content
    )
}

@Composable
fun PipButton(
    text: String,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bg = if (filled) Pip.Green else Color.Transparent
    val fg = if (filled) Pip.BgDeep else Pip.Green
    Box(
        modifier
            .height(52.dp)
            .background(bg)
            .border(2.dp, Pip.Green)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(),
            style = TextStyle(fontFamily = ButtonFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 2.sp, color = fg),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PipBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 16.dp) {
    Box(modifier.fillMaxWidth().height(height).border(1.5.dp, Pip.Green)) {
        Box(Modifier.fillMaxSize().padding(0.dp)) {
            Box(
                Modifier.fillMaxSize()
                    .drawBehind {
                        drawRect(Pip.Green, size = androidx.compose.ui.geometry.Size(size.width * progress.coerceIn(0f, 1f), size.height))
                    }
            )
        }
    }
}

@Composable
fun PipChip(text: String, color: Color = Pip.Green) {
    Box(Modifier.border(1.dp, color.copy(alpha = 0.6f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, style = smallStyle(color))
    }
}

@Composable
fun PipTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label.uppercase(), style = smallStyle())
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().heightIn(min = minHeight).border(2.dp, Pip.Green).padding(12.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (value.isEmpty()) Text(placeholder, style = bodyStyle(Pip.GreenDim.copy(alpha = 0.7f)))
            BasicTextField(
                value = value, onValueChange = onValueChange, singleLine = singleLine,
                textStyle = bodyStyle(), cursorBrush = SolidColor(Pip.Green),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PipCheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().border(1.dp, Pip.Border).clickable { onChange(!checked) }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = bodyStyle())
        Box(
            Modifier.size(26.dp).background(if (checked) Pip.Green else Color.White, androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Filled.Check, null, tint = Pip.BgDeep, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun PhotoPlaceholder(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier.border(1.dp, Pip.Border).drawBehind {
            val step = 14.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(Pip.Hatch, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Butt)
                x += step
            }
        },
        contentAlignment = Alignment.Center
    ) { Text(label.uppercase(), style = smallStyle()) }
}

@Composable
fun SectionLabel(text: String) = Text(text.uppercase(), style = smallStyle())

@Composable
fun RowScope.Weighted(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f)) { content() }
}

@Composable
fun ItemImage(@androidx.annotation.DrawableRes id: Int, modifier: Modifier = Modifier, alpha: Float = 1f) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id),
        contentDescription = null,
        modifier = modifier,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        alpha = alpha
    )
}
