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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: PulseViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val isTimerActive by viewModel.isTimerActive.collectAsState()
    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsState()
    val currentTimerMode by viewModel.currentTimerMode.collectAsState()
    val accentColor = viewModel.accentColor.value.hex
    val accentColorObj = Color(accentColor)
    val cyanColor = Color(0xFF06B6D4)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White

    // Calculate dynamic greetings
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> if (viewModel.language.value == "FR") "Bonjour" else "Good morning"
        hour < 18 -> if (viewModel.language.value == "FR") "Bon après-midi" else "Good afternoon"
        else -> if (viewModel.language.value == "FR") "Bonsoir" else "Good evening"
    }

    val todayDateFormatted = SimpleDateFormat("EEEE, d MMMM", if (viewModel.language.value == "FR") Locale.FRENCH else Locale.ENGLISH).format(Date()).replaceFirstChar { it.uppercase() }

    // Stat estimations
    val todayStartStamp = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val tCompletedCount = tasks.count { it.isCompleted && it.createdAt >= todayStartStamp }
    val tTotalCount = tasks.count { it.createdAt >= todayStartStamp }
    val complRatio = if (tTotalCount > 0) tCompletedCount.toFloat() / tTotalCount.toFloat() else 0f

    // Total focus mins today
    val focusMinsToday = focusSessions
        .filter { it.timestamp >= todayStartStamp && it.mode == "focus" }
        .sumOf { it.durationMinutes }

    // Streak calculation (simple mock / estimation from finished task dates)
    val streakCount = remember(tasks, focusSessions) {
        val uniqueDays = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tasks.filter { it.isCompleted }.forEach {
            uniqueDays.add(sdf.format(Date(it.createdAt)))
        }
        focusSessions.forEach {
            uniqueDays.add(sdf.format(Date(it.timestamp)))
        }
        uniqueDays.size.coerceAtLeast(1)
    }

    // Weather simulation based on user selection or generic
    val weatherTemp = "22°C"
    val weatherStatus = if (viewModel.language.value == "FR") "Ensoleillé" else "Sunny"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp) // padding for nav bottom bar
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = todayDateFormatted.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF06B6D4), // Cyan 400
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp // tracking-widest
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${if (viewModel.language.value == "FR") "Bonjour" else "Hello"}, ${viewModel.userName.value}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp, // tracking-tight
                        color = titleColor
                    )
                }
                
                // Elegant profile container from Editorial theme (MJ gradient ring)
                val userInitials = remember(viewModel.userName.value) {
                    if (viewModel.userName.value.length >= 2) {
                        viewModel.userName.value.substring(0, 2).uppercase()
                    } else if (viewModel.userName.value.isNotEmpty()) {
                        viewModel.userName.value.take(1).uppercase()
                    } else {
                        "U"
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFF7C3AED), Color(0xFF06B6D4), Color(0xFF7C3AED))
                            )
                        )
                        .padding(1.5.dp) // Border thickness
                        .clip(CircleShape)
                        .background(if (isLight) Color.White else Color(0xFF0A0A0F))
                        .clickable { onNavigateToStats() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isLight) Color(0xFFF3F4F6) else Color(0x1BFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = "${viewModel.userAvatar.value} $userInitials",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Card - Today progress circle animation styled for Editorial Aesthetic
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = accentColorObj.copy(alpha = 0.22f),
                cornerRadius = 24.dp, // 2rem corner equivalent
                isLight = isLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (viewModel.language.value == "FR") "OBJECTIF JOURNALIER" else "DAILY OBJECTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4), // Cyan-400
                            letterSpacing = 1.6.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Beautiful dynamic editorial title based on progress ratio
                        val editorialHeadline = when {
                            complRatio >= 0.85f -> if (viewModel.language.value == "FR") "Presque fini." else "Almost done."
                            complRatio >= 0.5f -> if (viewModel.language.value == "FR") "En bonne voie." else "Past half way."
                            complRatio > 0f -> if (viewModel.language.value == "FR") "En marche." else "Gaining speed."
                            else -> if (viewModel.language.value == "FR") "Nouveau départ." else "Fresh start."
                        }
                        
                        Text(
                            text = editorialHeadline,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp, // Space Grotesk tight feel
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (tTotalCount > 0) {
                                if (viewModel.language.value == "FR") {
                                    "+${((complRatio)*100).toInt()}% accomplis aujourd'hui"
                                } else {
                                    "+${((complRatio)*100).toInt()}% completed today"
                                }
                            } else {
                                if (viewModel.language.value == "FR") "Aucune activité plannifiée" else "No activities scheduled yet"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF06B6D4)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Animated Progress ring
                    val animProgress by animateFloatAsState(
                        targetValue = complRatio,
                        animationSpec = tween(durationMillis = 800), label = ""
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Canvas(modifier = Modifier.size(72.dp)) {
                            // Track background
                            drawCircle(
                                color = Color(0x1BFFFFFF),
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Animated active arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(accentColorObj, cyanColor, accentColorObj)
                                ),
                                startAngle = -90f,
                                sweepAngle = if (complRatio > 0f) animProgress * 360f else 0f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(complRatio * 100).toInt()}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = titleColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Mini Glass cards grid styled for Editorial Aesthetic
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mini item 1: Today remaining
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToTasks() },
                    cornerRadius = 16.dp,
                    isLight = isLight
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tâches",
                            tint = Color(0xFF06B6D4), // Cyan active color
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$tCompletedCount/$tTotalCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = if (viewModel.language.value == "FR") "TÂCHES" else "TASKS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = labelColor
                        )
                    }
                }

                // Mini item 2: Focus minutes total
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToFocus() },
                    cornerRadius = 16.dp,
                    isLight = isLight
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Focus",
                            tint = Color(0xFF7C3AED), // Violet accent
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${focusMinsToday}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = if (viewModel.language.value == "FR") "FOCUS" else "FOCUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = labelColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mini item 3: Streak
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToStats() },
                    cornerRadius = 16.dp,
                    isLight = isLight
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "🔥",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (streakCount > 0) "$streakCount j" else "0 j",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = if (viewModel.language.value == "FR") "SÉRIE" else "STREAK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = labelColor
                        )
                    }
                }

                // Mini item 4: Weather
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    cornerRadius = 16.dp,
                    isLight = isLight
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Météo",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = weatherTemp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = if (viewModel.language.value == "FR") "MÉTÉO" else "WEATHER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = labelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Mini active timer if focus session active
            if (isTimerActive) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = accentColorObj.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFocus() }
                        .border(1.dp, accentColorObj.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentTimerMode == "focus") {
                                        if (viewModel.language.value == "FR") "FOCUS ACTIF EN COURS" else "FOCUS SESSION RUNNING"
                                    } else {
                                        if (viewModel.language.value == "FR") "PAUSE DE FOCUS" else "RELAXATION REST BREAK"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColorObj
                                )
                                val minutes = timeLeftSeconds / 60
                                val seconds = timeLeftSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = titleColor
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.pauseFocusTimer() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(13.dp)
                                        .background(Color.Black)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(13.dp)
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section "Aujourd'hui" (Top 3 Priority task)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "Top Priorités" else "Top Priorities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor
                )
                Text(
                    text = if (viewModel.language.value == "FR") "Voir tout" else "See all",
                    modifier = Modifier.clickable { onNavigateToTasks() },
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColorObj,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val uncompletedTasks = tasks.filter { !it.isCompleted }.sortedByDescending { it.priority }.take(3)
            if (uncompletedTasks.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isLight = isLight
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (viewModel.language.value == "FR") "Toutes les tâches faites !" else "All caught up for today!",
                                style = MaterialTheme.typography.titleSmall,
                                color = titleColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewModel.language.value == "FR") "Profite de ton temps libre ou lance un Pomodoro" else "Take a breath, style a focus timer!",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelColor
                            )
                        }
                    }
                }
            } else {
                uncompletedTasks.forEach { task ->
                    DashboardTaskRow(task = task, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardTaskRow(task: Task, viewModel: PulseViewModel) {
    val accentColor = viewModel.accentColor.value.hex
    val accentColorObj = Color(accentColor)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT
    val dynamicTitleColor = if (isLight) Color(0xFF1F2937) else Color.White

    val prioSymbol = when (task.priority) {
        2 -> "🔴"
        1 -> "🟡"
        else -> "🟢"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 12.dp,
        isLight = isLight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { viewModel.completeTask(task, it) },
                colors = CheckboxDefaults.colors(checkedColor = accentColorObj)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prioSymbol,
                        modifier = Modifier.padding(end = 6.dp),
                        fontSize = 12.sp
                    )
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = dynamicTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${task.category} • ${task.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLight) Color.DarkGray else Color.LightGray
                )
            }
        }
    }
}
