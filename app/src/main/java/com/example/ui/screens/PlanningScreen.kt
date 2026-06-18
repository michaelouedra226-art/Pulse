package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Event
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlanningScreen(viewModel: PulseViewModel) {
    val events by viewModel.events.collectAsState()
    val accentColor = Color(viewModel.accentColor.value.hex)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White

    // Horizontal week selector dates from current week
    val currentCalendar = remember { Calendar.getInstance() }
    val daysOfWeek = remember {
        val list = mutableListOf<Calendar>()
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.DAY_OF_WEEK, tempCal.firstDayOfWeek) // Start of week
        for (i in 0..6) {
            val element = Calendar.getInstance().apply { timeInMillis = tempCal.timeInMillis }
            list.add(element)
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Selected Date state
    var selectedDayCal by remember { mutableStateOf(Calendar.getInstance()) }

    // Event adding overlay
    var showAddEvent by remember { mutableStateOf(false) }
    var selectedHourSlot by remember { mutableStateOf(8) }

    // Check if selectedDay is today
    val isSelectedDayToday = remember(selectedDayCal) {
        val curStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val selStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(selectedDayCal.time)
        curStr == selStr
    }

    // Filter events corresponding to the selected day
    val filteredEventsForSelDay = remember(events, selectedDayCal) {
        events.filter {
            val evCal = Calendar.getInstance().apply { timeInMillis = it.startTime }
            val evStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(evCal.time)
            val selStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(selectedDayCal.time)
            evStr == selStr
        }
    }

    // Current hour / minutes descend red line calculation
    var currentHourMinutesFraction by remember { mutableStateOf(0f) }
    LaunchedEffect(key1 = isSelectedDayToday) {
        while (isSelectedDayToday) {
            val nowCal = Calendar.getInstance()
            val hh = nowCal.get(Calendar.HOUR_OF_DAY)
            val mm = nowCal.get(Calendar.MINUTE)
            val fractionalHour = hh + (mm / 60f)
            // Hourly timeline ranges from 06:00 (index 0) to 23:00 (index 17)
            // Fraction formula: (fractionalHour - 6f) / 18f
            currentHourMinutesFraction = fractionalHour
            delay(10000) // update every 10s
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (viewModel.language.value == "FR") "EMPLOI DU TEMPS" else "WEEKLY PLANNING",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (viewModel.language.value == "FR") "Planning & Agenda" else "Time Optimization",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor
                    )
                }

                IconButton(
                    onClick = { showAddEvent = true },
                    modifier = Modifier.background(accentColor, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add event", tint = Color.White)
                }
            }

            // Horizontal Scrollable 7 days selector bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { dayCal ->
                    val isSelected = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dayCal.time) ==
                            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(selectedDayCal.time)

                    val isToday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dayCal.time) ==
                            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

                    val numericDay = SimpleDateFormat("d", Locale.getDefault()).format(dayCal.time)
                    val shortDayLabel = SimpleDateFormat("EEE", if (viewModel.language.value == "FR") Locale.FRENCH else Locale.ENGLISH).format(dayCal.time).replace(".", "").uppercase()

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> accentColor
                                isToday -> accentColor.copy(alpha = 0.25f)
                                else -> Color(0x0EFFFFFF)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(52.dp)
                            .clickable { selectedDayCal = dayCal }
                            .border(
                                width = 1.dp,
                                color = if (isSelected || isToday) accentColor else Color.Transparent,
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
                            Text(
                                text = shortDayLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else labelColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = numericDay,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else titleColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Day Agenda Timeline Scrollable
            Text(
                text = if (viewModel.language.value == "FR") "TIMELINE HORAIRE (6h à 23h)" else "DAILY TIMELINE (6am to 11pm)",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Timeline container list from 06:00 to 23:00
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Generate 18 slots representing hours 6 to 23
                items((6..23).toList()) { hour ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(if (isLight) Color.White else Color(0x0CFFFFFF))
                            .clickable {
                                selectedHourSlot = hour
                                showAddEvent = true
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 12.dp, top = 8.dp)
                        ) {
                            Text(
                                text = String.format("%02dh00", hour),
                                fontSize = 11.sp,
                                color = labelColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Display hourly items (Events placing visually)
                        val slotEvents = filteredEventsForSelDay.filter {
                            val evCalS = Calendar.getInstance().apply { timeInMillis = it.startTime }
                            val evHour = evCalS.get(Calendar.HOUR_OF_DAY)
                            evHour == hour
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 66.dp, top = 6.dp, bottom = 6.dp, end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            slotEvents.forEach { ev ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(ev.colorHex)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ev.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (ev.location.isNotEmpty()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = ev.location,
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deleteEvent(ev) }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Horizontal Indicator "maintenant" descending line if selected day is today
                        if (isSelectedDayToday && currentHourMinutesFraction in hour.toFloat()..(hour + 1f)) {
                            // Calculate position inside slot
                            val offsetPercent = currentHourMinutesFraction - hour.toFloat()
                            val lineOffsetDp = (offsetPercent * 72f).dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 58.dp)
                                    .offset(y = lineOffsetDp)
                                    .height(2.dp)
                                    .background(Color.Red)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.CenterStart)
                                )
                            }
                        }
                    }
                    Divider(color = Color(0x0EFFFFFF))
                }
            }
        }

        // Custom Fast Add Event overlay Dialog form
        if (showAddEvent) {
            EventCreationDialog(
                viewModel = viewModel,
                selectedDayCal = selectedDayCal,
                initialHour = selectedHourSlot,
                accentColor = accentColor,
                onDismiss = { showAddEvent = false }
            )
        }
    }
}

@Composable
fun EventCreationDialog(
    viewModel: PulseViewModel,
    selectedDayCal: Calendar,
    initialHour: Int,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val startCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = selectedDayCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, initialHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val endCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = selectedDayCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, initialHour + 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var startTimeStamp by remember { mutableStateOf(startCal.timeInMillis) }
    var endTimeStamp by remember { mutableStateOf(endCal.timeInMillis) }

    val context = LocalContext.current
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT
    val textStyleColor = if (isLight) Color.Black else Color.White

    // Preset color palettes (Electric violet, Glacier cyan, Sunrise orange, Emerald green)
    val colorPalettes = listOf(
        0xFF7C3AED, // Violet
        0xFF06B6D4, // Cyan
        0xFFF97316, // Orange
        0xFF10B981, // Green
        0xFF3B82F6, // Blue
        0xFFEC4899  // Pink
    )
    var selectedColor by remember { mutableStateOf(colorPalettes.first().toInt()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) Color.White else Color(0xFF1E1B26)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (viewModel.language.value == "FR") "Ajouter un Événement" else "Schedule New Event",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = textStyleColor
                )

                // Title info
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nom de l'événement") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        focusedLabelColor = accentColor
                    ),
                    singleLine = true
                )

                // Location info
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lieu (Optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        focusedLabelColor = accentColor
                    ),
                    singleLine = true
                )

                // Time Slot selector pickers click row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Heure Début", color = accentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        startCal.set(Calendar.HOUR_OF_DAY, hour)
                                        startCal.set(Calendar.MINUTE, minute)
                                        startTimeStamp = startCal.timeInMillis
                                    },
                                    startCal.get(Calendar.HOUR_OF_DAY),
                                    startCal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startTimeStamp)), color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Heure Fin", color = accentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        endCal.set(Calendar.HOUR_OF_DAY, hour)
                                        endCal.set(Calendar.MINUTE, minute)
                                        endTimeStamp = endCal.timeInMillis
                                    },
                                    endCal.get(Calendar.HOUR_OF_DAY),
                                    endCal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTimeStamp)), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Choose Event color palette
                Column {
                    Text(text = "Couleur Signature", color = accentColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPalettes.forEach { hexCode ->
                            val col = Color(hexCode)
                            val isSel = selectedColor == hexCode.toInt()
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(col, CircleShape)
                                    .clickable { selectedColor = hexCode.toInt() }
                                    .border(
                                        width = if (isSel) 2.dp else 0.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                // Action buttons click row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Annuler")
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                viewModel.addEvent(
                                    title = title,
                                    startTime = startTimeStamp,
                                    endTime = endTimeStamp,
                                    colorHex = selectedColor,
                                    location = location
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Planifier")
                    }
                }
            }
        }
    }
}
