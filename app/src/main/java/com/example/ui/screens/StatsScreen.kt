package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusSession
import com.example.data.Task
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(viewModel: PulseViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val sessions by viewModel.focusSessions.collectAsState()
    val accentColorHex = viewModel.accentColor.value.hex
    val accentColor = Color(accentColorHex)
    val cyanColor = Color(0xFF06B6D4)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White
    val context = LocalContext.current

    // 1. Streak active state
    val streakCount = remember(tasks, sessions) {
        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tasks.filter { it.isCompleted }.forEach {
            uniqueDays.add(sdf.format(Date(it.createdAt)))
        }
        sessions.forEach {
            uniqueDays.add(sdf.format(Date(it.timestamp)))
        }
        uniqueDays.size.coerceAtLeast(1)
    }

    // 2. Bar Chart data - Completed Tasks (last 7 days completed items)
    val last7DaysLabels = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val sdf = SimpleDateFormat("EE", if (viewModel.language.value == "FR") Locale.FRENCH else Locale.ENGLISH)
        for (i in 0..6) {
            list.add(sdf.format(cal.time).replace(".", "").uppercase())
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    val last7DaysValues = remember(tasks) {
        val list = mutableListOf<Float>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -6)

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        for (i in 0..6) {
            val keyDayStr = sdf.format(cal.time)
            val completedOnDay = tasks.count {
                it.isCompleted && sdf.format(Date(it.createdAt)) == keyDayStr
            }
            list.add(completedOnDay.toFloat())
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // 3. Line Chart data - Focus mins spent (last 7 days spent focus duration)
    val last7DaysFocusMins = remember(sessions) {
        val list = mutableListOf<Float>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -6)

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        for (i in 0..6) {
            val keyDayStr = sdf.format(cal.time)
            val focusMinsOnDay = sessions
                .filter { it.mode == "focus" && sdf.format(Date(it.timestamp)) == keyDayStr }
                .sumOf { it.durationMinutes }
            list.add(focusMinsOnDay.toFloat())
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // 4. Donuts Category répartition
    val categoryShares = remember(tasks) {
        val map = mutableMapOf<String, Float>()
        val preset = listOf("Études", "Perso", "Projets", "Santé", "Finance")
        preset.forEach { map[it] = 0f }
        tasks.filter { it.isCompleted }.forEach {
            map[it.category] = (map[it.category] ?: 0f) + 1f
        }
        val totalFinished = map.values.sum()
        if (totalFinished > 0f) {
            map.mapValues { it.value / totalFinished }
        } else {
            map.mapValues { 0.2f } // uniform preview if empty
        }
    }

    // 5. Calculated Productivity score
    val productivityScore = remember(tasks, sessions, streakCount) {
        val finishedTotal = tasks.count { it.isCompleted }
        val focusTotalVal = sessions.filter { it.mode == "focus" }.sumOf { it.durationMinutes }
        ((finishedTotal * 4 + focusTotalVal * 1.0 + streakCount * 8).coerceIn(10.0, 100.0)).toInt()
    }

    var showReportToast by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (viewModel.language.value == "FR") "ANALYTIQUE & REPORTING" else "INSIGHTS & ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (viewModel.language.value == "FR") "Performances" else "Productivity Engine",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor
                    )
                }

                IconButton(
                    onClick = { showReportToast = true },
                    modifier = Modifier.background(accentColor, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Reporting shares", tint = Color.White)
                }
            }

            // Streak card + Productivity Score
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 14.dp,
                    isLight = isLight
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (viewModel.language.value == "FR") "Série" else "Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$streakCount Jours",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = titleColor
                            )
                        }
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1.2f),
                    cornerRadius = 14.dp,
                    isLight = isLight
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛡️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (viewModel.language.value == "FR") "OS Score" else "Performance",
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$productivityScore / 100",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = cyanColor
                            )
                        }
                    }
                }
            }

            // Custom Canvas Chart 1: Tasks completed (7 days bar chart)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isLight = isLight
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "TÂCHES ACCOMPLIES (7 DERNIERS JOURS)" else "TASKS SATISFIED (LAST 7 DAYS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Draw Bar Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val paddingX = 40f
                    val chartWidth = size.width - (paddingX * 2f)
                    val chartHeight = size.height - 40f
                    val columnWidth = chartWidth / 7f

                    // Max val finder
                    val maxVal = (last7DaysValues.maxOrNull() ?: 1f).coerceAtLeast(4f)

                    // Draw baseline
                    drawLine(
                        color = Color(0x1BFFFFFF),
                        start = Offset(paddingX, chartHeight),
                        end = Offset(size.width - paddingX, chartHeight),
                        strokeWidth = 2f
                    )

                    last7DaysValues.forEachIndexed { i, valItem ->
                        val ratio = valItem / maxVal
                        val barHeightHeight = ratio * chartHeight
                        val xPos = paddingX + (i * columnWidth) + (columnWidth / 4f)
                        val yPos = chartHeight - barHeightHeight

                        // Draw rect column
                        if (valItem > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(accentColor, cyanColor)),
                                topLeft = Offset(xPos, yPos),
                                size = Size(columnWidth / 2f, barHeightHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )
                        } else {
                            // Empty trace indicator
                            drawCircle(
                                color = Color(0x1EFFFFFF),
                                radius = 6f,
                                center = Offset(xPos + (columnWidth / 4f), chartHeight - 8f)
                            )
                        }
                    }
                }

                // Days names row below Canvas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last7DaysLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = labelColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }

            // Custom Canvas Chart 2: Focus time Curve (Weekly Focus duration Line Chart)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isLight = isLight
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "TEMPS EN FOCUS (MINUTES)" else "FOCUS ELAPSED MINS",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val paddingX = 40f
                    val chartWidth = size.width - (paddingX * 2f)
                    val chartHeight = size.height - 40f
                    val stepX = chartWidth / 6f

                    val maxMins = (last7DaysFocusMins.maxOrNull() ?: 1f).coerceAtLeast(60f)

                    // Draw background horizontal grid dividers
                    for (g in 0..2) {
                        val gridY = (chartHeight / 2) * g
                        drawLine(
                            color = Color(0x0EFFFFFF),
                            start = Offset(paddingX, gridY),
                            end = Offset(size.width - paddingX, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Map points
                    val points = last7DaysFocusMins.mapWithIndex { i, mins ->
                        val ratio = mins / maxMins
                        val y = chartHeight - (ratio * chartHeight)
                        val x = paddingX + (i * stepX)
                        Offset(x, y)
                    }

                    // Draw curve line of points
                    for (p in 0 until points.size - 1) {
                        drawLine(
                            color = cyanColor,
                            start = points[p],
                            end = points[p + 1],
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        // Gradient fill overlay envelope
                        drawCircle(color = cyanColor, radius = 6f, center = points[p])
                    }
                    if (points.isNotEmpty()) {
                        drawCircle(color = cyanColor, radius = 6f, center = points.last())
                    }
                }

                // Days names row below Canvas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last7DaysLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = labelColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }

            // Category breakdown Donut Chart
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isLight = isLight
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "RÉPARTITION PAR CATÉGORIE" else "CATEGORY SEGMENTATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Draw Canvas Arc for Donut
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            var startAngle = 0f
                            val categoriesPaletteColors = listOf(
                                Color(0xFF7C3AED), // Violet
                                Color(0xFF06B6D4), // Cyan
                                Color(0xFFF97316), // Orange
                                Color(0xFF10B981), // Green
                                Color(0xFFEF4444)  // Red
                            )

                            categoryShares.values.forEachIndexed { idx, share ->
                                val sweep = share * 360f
                                if (sweep > 0L) {
                                    drawArc(
                                        color = categoriesPaletteColors[idx % categoriesPaletteColors.size],
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }
                            // Empty case placeholder ring
                            if (categoryShares.values.all { it == 0.2f }) {
                                drawCircle(color = Color(0x1BFFFFFF), style = Stroke(width = 8.dp.toPx()))
                            }
                        }

                        Text(
                            text = "PULSE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                    }

                    // Legend labels displaying side-by-side
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val colorKeys = listOf(
                            Color(0xFF7C3AED) to "Études",
                            Color(0xFF06B6D4) to "Perso",
                            Color(0xFFF97316) to "Projets",
                            Color(0xFF10B981) to "Santé",
                            Color(0xFFEF4444) to "Finance"
                        )
                        colorKeys.forEach { (col, label) ->
                            val proportionFraction = categoryShares[label] ?: 0f
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(col, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$label (${(proportionFraction * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    color = titleColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2D Hourly Productivity heatmap representation grid
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isLight = isLight
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "HEATMAP D'HEURE PROS" else "HOURLY PRODUCTIVE HEATMAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Heatmap representing 24 hours categorized in 4 horizontal chunks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val intervals = listOf(
                        "0h-6h" to listOf(0..5),
                        "6h-12h" to listOf(6..11),
                        "12h-18h" to listOf(12..17),
                        "18h-24h" to listOf(18..23)
                    )
                    intervals.forEach { (label, rangeList) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = label, fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                rangeList.first().forEach { hr ->
                                    // Calculate simulated color density from completed sessions
                                    val countSessionsHour = sessions.count {
                                        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                                        c.get(Calendar.HOUR_OF_DAY) == hr
                                    }
                                    val bgDensityColor = when {
                                        countSessionsHour >=  3 -> accentColor
                                        countSessionsHour ==  2 -> accentColor.copy(alpha = 0.6f)
                                        countSessionsHour ==  1 -> accentColor.copy(alpha = 0.3f)
                                        else -> Color(0x0EFFFFFF)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(bgDensityColor)
                                            .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Share Dialog sheet containing Copyable stats formatting
        if (showReportToast) {
            val totalFinishedTCount = tasks.count { it.isCompleted }
            val totalFocusCount = sessions.filter { it.mode == "focus" }.sumOf { it.durationMinutes }
            val outputReport = """
                == PULSE OS PRODUCTIVITY WEEKLY EXPORT ==
                Compte d'utilisateur : ${viewModel.userName.value}
                Score Global de Productivité : $productivityScore/100
                Série Actuelle d'Activités : $streakCount jours active 🔥
                Tâches totales accomplies : $totalFinishedTCount
                Temps total de concentration Focus : $totalFocusCount minutes
                == PULSE — Ton système de vie premium ==
            """.trimIndent()

            AlertDialog(
                onDismissRequest = { showReportToast = false },
                containerColor = if (isLight) Color.White else Color(0xFF1E1B26),
                title = {
                    Text(
                        text = if (viewModel.language.value == "FR") "Rapport Hebdomadaire Exportable" else "Weekly Shareable Report",
                        color = titleColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (viewModel.language.value == "FR") "Voici ton rapport prêt à être copié :" else "A summary of your stats is available:",
                            color = labelColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0BFFFFFF), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = outputReport,
                                color = textStyleColorForStats(isLight),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showReportToast = false },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(text = "Fermer")
                    }
                }
            )
        }
    }
}

// Helper generic map with indices
inline fun <T, R> List<T>.mapWithIndex(transform: (index: Int, T) -> R): List<R> {
    val destination = ArrayList<R>(size)
    var index = 0
    for (item in this) {
        destination.add(transform(index++, item))
    }
    return destination
}

@Composable
fun textStyleColorForStats(isLight: Boolean): Color {
    return if (isLight) Color.DarkGray else Color.LightGray
}
