package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0x1CFFFFFF), // white/10 equivalent
    isLight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (isLight) Color(0x0C000000) else Color(0x0DFFFFFF) // light amoled transparent vs dark translucent and rich
    val borderCol = if (isLight) Color(0x0F000000) else borderColor
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderCol,
                        borderCol.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(18.dp) // generous padding
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun PulseBackground(
    isAmoled: Boolean = false,
    isLight: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val bgBrush = when {
        isAmoled -> Brush.verticalGradient(colors = listOf(Color(0xFF000000), Color(0xFF000000)))
        isLight -> Brush.verticalGradient(colors = listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB)))
        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0F), // noir / bleu nuit
                Color(0xFF0D1B2A)
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        content()
    }
}
