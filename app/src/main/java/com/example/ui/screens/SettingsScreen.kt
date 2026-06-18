package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseAccentColor
import com.example.viewmodel.PulseTheme
import com.example.viewmodel.PulseViewModel

@Composable
fun SettingsScreen(viewModel: PulseViewModel) {
    var nameState by viewModel.userName
    var avatarState by viewModel.userAvatar
    var themeState by viewModel.appTheme
    var accentState by viewModel.accentColor
    var langState by viewModel.language

    var focusDurState by viewModel.focusDuration
    var shortDurState by viewModel.shortBreakDuration
    var longDurState by viewModel.longBreakDuration

    val accentColor = Color(accentState.hex)
    val isLight = themeState == PulseTheme.LIGHT

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White

    // Notifications toggle slots
    var notifUrgente by remember { mutableStateOf(true) }
    var notifPomComplete by remember { mutableStateOf(true) }
    var notifRemindToday by remember { mutableStateOf(true) }

    var showWipeConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (langState == "FR") "CONFIGURATIONS DU SYSTÈME" else "OS SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (langState == "FR") "Paramètres" else "Control Panel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor
            )
        }

        // 1. User Profile Customizer
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isLight = isLight
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "👤", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (langState == "FR") "PROFIL DE L'UTILISATEUR" else "USER PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nameState,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text field name editor
            OutlinedTextField(
                value = nameState,
                onValueChange = { nameState = it },
                label = { Text(if (langState == "FR") "Nom d'utilisateur" else "Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Avatar select list
            Text(
                text = if (langState == "FR") "Choisir un symbole d'avatar" else "Select Avatar Symbol",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                listOf("⚡", "🔥", "🌸", "🧠", "🔮", "🚀", "💻").forEach { item ->
                    val isAvSel = avatarState == item
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isAvSel) accentColor else Color(0x0EFFFFFF), CircleShape)
                            .clickable { avatarState = item }
                            .border(
                                width = if (isAvSel) 1.dp else 0.dp,
                                color = if (isAvSel) Color.White else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item, fontSize = 16.sp)
                    }
                }
            }
        }

        // 2. Personalization Skin (Theme & Colors Accent)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isLight = isLight
        ) {
            Text(
                text = if (langState == "FR") "THÈMES & SYSTÈME VISUEL" else "VISUAL SKIN & INTENSITY",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Dynamic themes selector row (Dark / AMOLED / Light)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    PulseTheme.DARK to (if (langState == "FR") "Sombre" else "Dark"),
                    PulseTheme.AMOLED to "AMOLED",
                    PulseTheme.LIGHT to (if (langState == "FR") "Clair" else "Light")
                ).forEach { (thm, l) ->
                    val isThemeActive = themeState == thm
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isThemeActive) accentColor else Color(0x0EFFFFFF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { themeState = thm }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = l, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accent colors select
            Text(
                text = if (langState == "FR") "Couleur d'accentuation" else "App Highlight Accent Color",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                PulseAccentColor.values().forEach { col ->
                    val isColorSel = accentState == col
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(col.hex), CircleShape)
                            .clickable { accentState = col }
                            .border(
                                width = if (isColorSel) 2.dp else 0.dp,
                                color = if (isColorSel) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // 3. Durations Customizing (Pomodoro lengths custom sliders)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isLight = isLight
        ) {
            Text(
                text = if (langState == "FR") "INTERVALLES POMODORO (MINUTES)" else "POMODORO DURATIONS (MINUTES)",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Focus Minutes length Sliders
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (langState == "FR") "Temps de Focus" else "Focus Period", fontSize = 12.sp, color = titleColor, fontWeight = FontWeight.Bold)
                    Text(text = "${focusDurState}m", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Black)
                }
                Slider(
                    value = focusDurState.toFloat(),
                    onValueChange = { focusDurState = it.toInt() },
                    valueRange = 10f..60f,
                    colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                )
            }

            // Short Rest length Sliders
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (langState == "FR") "Pause Courte" else "Short Rest", fontSize = 12.sp, color = titleColor, fontWeight = FontWeight.Bold)
                    Text(text = "${shortDurState}m", fontSize = 12.sp, color = Color(0xFF06B6D4), fontWeight = FontWeight.Black)
                }
                Slider(
                    value = shortDurState.toFloat(),
                    onValueChange = { shortDurState = it.toInt() },
                    valueRange = 2f..15f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF06B6D4), activeTrackColor = Color(0xFF06B6D4))
                )
            }

            // Long Rest length Sliders
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (langState == "FR") "Pause Longue" else "Long Break", fontSize = 12.sp, color = titleColor, fontWeight = FontWeight.Bold)
                    Text(text = "${longDurState}m", fontSize = 12.sp, color = labelColor, fontWeight = FontWeight.Black)
                }
                Slider(
                    value = longDurState.toFloat(),
                    onValueChange = { longDurState = it.toInt() },
                    valueRange = 5f..30f,
                    colors = SliderDefaults.colors(thumbColor = Color.LightGray, activeTrackColor = Color.Gray)
                )
            }
        }

        // 4. Notification Preferences & Language Toggle
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isLight = isLight
        ) {
            Text(
                text = if (langState == "FR") "PRÉFÉRENCES SYSTEME & LANGUES" else "SYSTEM NOISES & METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Lang choosing button selection FR vs EN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (langState == "FR") "Langue de l'application" else "App Core Language",
                    fontSize = 13.sp,
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("FR", "EN").forEach { lText ->
                        val isLSelected = langState == lText
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (isLSelected) accentColor else Color(0x0EFFFFFF), CircleShape)
                                .clickable { langState = lText }
                                .border(1.dp, if (isLSelected) Color.White else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = lText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Divider(color = Color(0x0EFFFFFF), modifier = Modifier.padding(vertical = 8.dp))

            // Toggle 1: Notif urgente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = if (langState == "FR") "Alerte de deadline" else "Urgent Deadline limit", fontSize = 12.sp, color = titleColor, fontWeight = FontWeight.Bold)
                    Text(text = if (langState == "FR") "Rappel 30 minutes avant l'échéance" else "Get warning 30m before deadline", fontSize = 10.sp, color = labelColor)
                }
                Switch(checked = notifUrgente, onCheckedChange = { notifUrgente = it }, colors = SwitchDefaults.colors(checkedThumbColor = accentColor))
            }

            // Toggle 2: Notif pom completed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = if (langState == "FR") "Fin de Pomodoro" else "End of focus notification", fontSize = 12.sp, color = titleColor, fontWeight = FontWeight.Bold)
                    Text(text = if (langState == "FR") "Signal par vibration / alerte fin de cycle" else "Receive instant ring on pom completed", fontSize = 10.sp, color = labelColor)
                }
                Switch(checked = notifPomComplete, onCheckedChange = { notifPomComplete = it }, colors = SwitchDefaults.colors(checkedThumbColor = accentColor))
            }
        }

        // 5. Data Management (JSON display & Complete Reset wipes DB)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = Color(0x33EF4444),
            isLight = isLight
        ) {
            Text(
                text = if (langState == "FR") "ZONES DE SÉCURITÉ DES DONNÉES" else "DANGEROUS DATA PRIVACY",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Complete DB purge reset click
            Button(
                onClick = { showWipeConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Purger")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (langState == "FR") "Réinitialiser toutes les données" else "Reset & Wipe system data",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Confirmation dialog for complete database WIPE
        if (showWipeConfirm) {
            AlertDialog(
                onDismissRequest = { showWipeConfirm = false },
                containerColor = if (isLight) Color.White else Color(0xFF16151C),
                title = {
                    Text(
                        text = if (langState == "FR") "Confirmer la suppression complète ?" else "Wipe all profile data?",
                        color = titleColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        text = if (langState == "FR") {
                            "Cette action est irréversible. Toutes tes tâches, événements planifiés, et historiques de Pomodoro seront définitivement effacés."
                        } else {
                            "Warning: Wiping is permanent. This actions deletes all custom tasks, scheduled timelines, and focus history completely."
                        },
                        color = labelColor,
                        fontSize = 12.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllData()
                            showWipeConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(text = if (langState == "FR") "Supprimer définitivement" else "Confirm Purge")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showWipeConfirm = false }) {
                        Text(text = if (langState == "FR") "Annuler" else "Cancel")
                    }
                }
            )
        }
    }
}
