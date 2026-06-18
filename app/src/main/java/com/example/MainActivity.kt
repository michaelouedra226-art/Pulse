package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PulseBackground
import com.example.ui.screens.*
import com.example.viewmodel.PulseTheme
import com.example.viewmodel.PulseViewModel

enum class PulseTab(val labelFr: String, val labelEn: String, val icon: ImageVector) {
    DASHBOARD("Accueil", "Home", Icons.Default.Home),
    TASKS("Tâches", "Tasks", Icons.Default.CheckCircle),
    FOCUS("Focus", "Focus", Icons.Default.PlayArrow),
    PLANNING("Planning", "Schedule", Icons.Default.DateRange),
    STATS("Stats", "Stats", Icons.Default.Star), // Fallback Star standard icons representing Stats
    SETTINGS("Paramètres", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PulseViewModel = viewModel()
            val themeState by viewModel.appTheme
            val accentState by viewModel.accentColor
            val langState by viewModel.language

            val isAmoled = themeState == PulseTheme.AMOLED
            val isLight = themeState == PulseTheme.LIGHT
            val accentColor = Color(accentState.hex)

            // Current visible tab state
            var currentTab by remember { mutableStateOf(PulseTab.DASHBOARD) }

            // Define core color themes
            val primaryColorScheme = if (isLight) {
                lightColorScheme(
                    primary = accentColor,
                    secondary = accentColor.copy(alpha = 0.7f),
                    background = Color(0xFFF3F4F6)
                )
            } else {
                darkColorScheme(
                    primary = accentColor,
                    secondary = accentColor.copy(alpha = 0.7f),
                    background = if (isAmoled) Color.Black else Color(0xFF0A0A0F)
                )
            }

            MaterialTheme(colorScheme = primaryColorScheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    PulseBackground(
                        isAmoled = isAmoled,
                        isLight = isLight
                    ) {
                        // Main Tab Views selector
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                PulseTab.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTasks = { currentTab = PulseTab.TASKS },
                                    onNavigateToFocus = { currentTab = PulseTab.FOCUS },
                                    onNavigateToStats = { currentTab = PulseTab.STATS }
                                )
                                PulseTab.TASKS -> TasksScreen(viewModel = viewModel)
                                PulseTab.FOCUS -> FocusScreen(viewModel = viewModel)
                                PulseTab.PLANNING -> PlanningScreen(viewModel = viewModel)
                                PulseTab.STATS -> StatsScreen(viewModel = viewModel)
                                PulseTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }

                            // Dynamic Glass Floating Navigation footer row bar with 6 Tabs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isLight) Color(0xD8FFFFFF) else Color(0xD8110E1C)) // high contrast semi transparent glass nav bar
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PulseTab.values().forEach { tab ->
                                            val isActive = currentTab == tab
                                            val currentAccent = if (isActive) accentColor else (if (isLight) Color.DarkGray else Color.LightGray)

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { currentTab = tab }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.labelFr,
                                                    tint = currentAccent,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (langState == "FR") tab.labelFr else tab.labelEn,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = currentAccent,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
