package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusSession
import com.example.data.Task
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusScreen(viewModel: PulseViewModel) {
    val isTimerActive by viewModel.isTimerActive.collectAsState()
    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsState()
    val currentTimerMode by viewModel.currentTimerMode.collectAsState()
    val linkedTaskId by viewModel.linkedTaskId.collectAsState()
    val ambientSoundType by viewModel.ambientSoundType.collectAsState()
    val pomsCount by viewModel.pomsCompletedCount.collectAsState()

    val tasks by viewModel.tasks.collectAsState()
    val sessions by viewModel.focusSessions.collectAsState()

    val accentColor = Color(viewModel.accentColor.value.hex)
    val cyanColor = Color(0xFF06B6D4)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White

    // Total duration mapped to active mode
    val totalModeDurationMins = when (currentTimerMode) {
        "focus" -> viewModel.focusDuration.value
        "short_break" -> viewModel.shortBreakDuration.value
        "long_break" -> viewModel.longBreakDuration.value
        else -> viewModel.focusDuration.value
    }
    val totalModeSeconds = totalModeDurationMins * 60

    // Remaining progress calculation for canvas circle
    val ratioRemaining = if (totalModeSeconds > 0) timeLeftSeconds.toFloat() / totalModeSeconds.toFloat() else 0f

    // Format minutes/seconds for screen display
    val displayTimer = remember(timeLeftSeconds) {
        val mins = timeLeftSeconds / 60
        val secs = timeLeftSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    // Active linked task text
    val activeTaskSelected = tasks.find { it.id == linkedTaskId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "MINUTEUR POMODORO" else "POMODORO FOCUS TIMER",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (viewModel.language.value == "FR") "Espace Focus" else "Focus Realm",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor
                )
            }
        }

        // Active Modes Selection Bar
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                isLight = isLight
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modesList = listOf(
                        "focus" to (if (viewModel.language.value == "FR") "Focus" else "Focus"),
                        "short_break" to (if (viewModel.language.value == "FR") "Pause court" else "Short rest"),
                        "long_break" to (if (viewModel.language.value == "FR") "Pause long" else "Long rest")
                    )

                    modesList.forEach { (modeCode, modeLabel) ->
                        val isSelected = currentTimerMode == modeCode
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentColor else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setTimerMode(modeCode) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else labelColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Animated High Fidelity Circular Canvas
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .padding(top = 12.dp)
            ) {
                // Background shadow dynamic pulse if active
                if (isTimerActive) {
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val animatedAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.05f,
                        targetValue = 0.16f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = ""
                    )
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .background(accentColor.copy(alpha = animatedAlpha), CircleShape)
                    )
                }

                // Core Circular Canvas
                Canvas(modifier = Modifier.size(210.dp)) {
                    // Track background
                    drawCircle(
                        color = Color(0x1BFFFFFF),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Active Gradient Arc representing progress remaining
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(accentColor, cyanColor, accentColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = ratioRemaining * 360f,
                        useCenter = false,
                        style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Numbers Inside
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = displayTimer,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.sp)
                        ),
                        color = titleColor,
                        fontSize = 42.sp
                    )
                    Text(
                        text = if (isTimerActive) {
                            if (viewModel.language.value == "FR") "CONCENTRATION ACTIVE" else "FOCUS ACTIVE"
                        } else {
                            if (viewModel.language.value == "FR") "PAUSE" else "PAUSED"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Playing Controller Button bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return to timer start / Reset
                IconButton(
                    onClick = { viewModel.resetFocusTimer() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x11FFFFFF), CircleShape)
                        .border(1.dp, Color(0x1EFFFFFF), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = titleColor)
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Core Play control
                IconButton(
                    onClick = {
                        if (isTimerActive) viewModel.pauseFocusTimer() else viewModel.startFocusTimer()
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(accentColor, CircleShape)
                ) {
                    if (isTimerActive) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(4.dp).height(20.dp).background(Color.White))
                            Box(modifier = Modifier.width(4.dp).height(20.dp).background(Color.White))
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Trigger",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Cycle marker / poms cycle count summary
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x11FFFFFF), CircleShape)
                        .border(1.dp, Color(0x1EFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$pomsCount/4",
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Linked Task Selection drop-down
        item {
            var showTaskSelector by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (viewModel.language.value == "FR") "TRAVAILLER SUR LA TÂCHE :" else "WORK ON TASK LINKED :",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTaskSelector = true },
                    cornerRadius = 14.dp,
                    isLight = isLight
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                text = activeTaskSelected?.title ?: (if (viewModel.language.value == "FR") "Sélectionner une tâche" else "Select a task link"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Open", tint = labelColor)
                    }
                }

                if (showTaskSelector) {
                    AlertDialog(
                        onDismissRequest = { showTaskSelector = false },
                        containerColor = if (isLight) Color.White else Color(0xFF16151C),
                        title = {
                            Text(
                                text = if (viewModel.language.value == "FR") "Associer une tâche" else "Link a task",
                                color = titleColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        text = {
                            val activePending = tasks.filter { !it.isCompleted }
                            if (activePending.isEmpty()) {
                                Text(
                                    text = if (viewModel.language.value == "FR") "Aucune tâche active disponible." else "No pending tasks found.",
                                    color = labelColor
                                )
                            } else {
                                Box(modifier = Modifier.heightIn(max = 240.dp)) {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectLinkedTask(null)
                                                        showTaskSelector = false
                                                    }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🚫 ", fontSize = 16.sp)
                                                Text(
                                                    text = if (viewModel.language.value == "FR") "Aucune (session générique)" else "None (Generic focus)",
                                                    color = titleColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        items(activePending) { task ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectLinkedTask(task.id)
                                                        showTaskSelector = false
                                                    }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🎯 ", fontSize = 16.sp)
                                                Text(
                                                    text = task.title,
                                                    color = titleColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showTaskSelector = false }) {
                                Text(text = "OK", color = accentColor)
                            }
                        }
                    )
                }
            }
        }

        // Ambiance Sonore Synthesis selection toggler
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (viewModel.language.value == "FR") "AMBIANCE SONORE (SYNTHÈSE HAUTE FIDÉLITÉ)" else "AMBIENT NOISE GENERATOR (HI-FI SYNTHESIS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Silence" to "🔇",
                        "Pluie" to "🌧️",
                        "Café" to "☕",
                        "Blanc" to "🎧"
                    ).forEach { (soundKey, soundEmoji) ->
                        val isSoundActive = ambientSoundType == soundKey
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSoundActive) cyanColor else Color(0x0EFFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.changeAmbientSound(soundKey) }
                                .border(
                                    width = 1.dp,
                                    color = if (isSoundActive) cyanColor else Color(0x1BFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = soundEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = soundKey,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSoundActive) Color.White else titleColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Focus Session History display at the vertical bottom of Focus Panel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (viewModel.language.value == "FR") "SESSIONS DU JOUR" else "TODAY'S SESSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val todayStamp = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todaySessions = sessions.filter { it.timestamp >= todayStamp }
                if (todaySessions.isEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isLight = isLight
                    ) {
                        Text(
                            text = if (viewModel.language.value == "FR") "Aucune session focus complétée aujourd'hui. C'est l'heure de commencer !" else "No focus cycles completed today. Connect & launch a Pomodoro!",
                            style = MaterialTheme.typography.bodySmall,
                            color = labelColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    todaySessions.take(5).forEach { session ->
                        val linkedT = tasks.find { it.id == session.taskId }
                        val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.timestamp))
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            cornerRadius = 10.dp,
                            isLight = isLight
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎯 ", fontSize = 14.sp)
                                    Text(
                                        text = linkedT?.title ?: (if (viewModel.language.value == "FR") "Session Focus Générique" else "Generic Focus Cycle"),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "+${session.durationMinutes}m ($timeFormatted)",
                                    fontSize = 11.sp,
                                    color = cyanColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
