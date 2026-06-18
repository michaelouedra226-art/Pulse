package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SubTask
import com.example.data.Task
import com.example.ui.components.GlassCard
import com.example.viewmodel.PulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(viewModel: PulseViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val accentColor = Color(viewModel.accentColor.value.hex)
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT
    val context = LocalContext.current

    val labelColor = if (isLight) Color.DarkGray else Color.LightGray
    val titleColor = if (isLight) Color(0xFF1F2937) else Color.White

    // Filters and sorting states
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedPriorityFilter by remember { mutableStateOf(-1) } // -1 means All
    var selectedStatusFilter by remember { mutableStateOf("Active") } // "Active", "Completed", "All"
    var sortBy by remember { mutableStateOf("Priority") } // "Deadline", "Priority", "Duration"

    // Form modal state
    var showAddDialog by remember { mutableStateOf(false) }

    // Categories
    val categoriesList = listOf("Études", "Perso", "Projets", "Santé", "Finance")

    // Filtered and Sorted Tasks
    val filteredSortedTasks = remember(tasks, selectedCategoryFilter, selectedPriorityFilter, selectedStatusFilter, sortBy) {
        var temp = tasks.filter {
            val catCheck = selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
            val prioCheck = selectedPriorityFilter == -1 || it.priority == selectedPriorityFilter
            val statusCheck = when (selectedStatusFilter) {
                "Active" -> !it.isCompleted
                "Completed" -> it.isCompleted
                else -> true
            }
            catCheck && prioCheck && statusCheck
        }

        // Apply Sorting
        temp = when (sortBy) {
            "Deadline" -> temp.sortedWith(compareBy<Task> { it.isCompleted }.thenBy { it.deadline })
            "Duration" -> temp.sortedWith(compareBy<Task> { it.isCompleted }.thenByDescending { it.durationMinutes })
            else -> temp.sortedWith(compareBy<Task> { it.isCompleted }.thenByDescending { it.priority })
        }
        temp
    }

    // Classify into sections: Aujourd'hui, Cette semaine, Plus tard
    val now = System.currentTimeMillis()
    val endOfTodayStamp = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val endOfWeekStamp = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 7)
    }.timeInMillis

    val tasksToday = filteredSortedTasks.filter { it.deadline <= endOfTodayStamp && it.deadline > 0 }
    val tasksThisWeek = filteredSortedTasks.filter { it.deadline in (endOfTodayStamp + 1)..endOfWeekStamp }
    val tasksLater = filteredSortedTasks.filter { it.deadline > endOfWeekStamp || it.deadline == 0L }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
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
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (viewModel.language.value == "FR") "GESTION DES TÂCHES" else "TASKS MANAGEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (viewModel.language.value == "FR") "Tâches PULSE" else "PULSE Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor
                    )
                }

                // Add FAB
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.language.value == "FR") "Ajouter" else "Add",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Filters selector scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Category Filter Choose
                var expandedCat by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expandedCat = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedCategoryFilter == "All") (if (viewModel.language.value == "FR") "Catégorie : Tout" else "Category: All") else selectedCategoryFilter,
                            color = titleColor,
                            fontSize = 12.sp
                        )
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false },
                        modifier = Modifier.background(Color(0xFF1C1917))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tous", color = Color.White) },
                            onClick = { selectedCategoryFilter = "All"; expandedCat = false }
                        )
                        categoriesList.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White) },
                                onClick = { selectedCategoryFilter = category; expandedCat = false }
                            )
                        }
                    }
                }

                // Priority Filter Choose
                var expandedPrio by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expandedPrio = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val prioLabel = when (selectedPriorityFilter) {
                            2 -> "🔴 Haute"
                            1 -> "🟡 Moyenne"
                            0 -> "🟢 Basse"
                            else -> if (viewModel.language.value == "FR") "Priorité : Tout" else "Priority: All"
                        }
                        Text(text = prioLabel, color = titleColor, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = expandedPrio,
                        onDismissRequest = { expandedPrio = false },
                        modifier = Modifier.background(Color(0xFF1C1917))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tous / All", color = Color.White) },
                            onClick = { selectedPriorityFilter = -1; expandedPrio = false }
                        )
                        DropdownMenuItem(
                            text = { Text("🔴 Haute / High", color = Color.White) },
                            onClick = { selectedPriorityFilter = 2; expandedPrio = false }
                        )
                        DropdownMenuItem(
                            text = { Text("🟡 Moyenne / Medium", color = Color.White) },
                            onClick = { selectedPriorityFilter = 1; expandedPrio = false }
                        )
                        DropdownMenuItem(
                            text = { Text("🟢 Basse / Low", color = Color.White) },
                            onClick = { selectedPriorityFilter = 0; expandedPrio = false }
                        )
                    }
                }

                // Sorting Trigger
                var expandedSort by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expandedSort = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val sortLabel = when (sortBy) {
                            "Deadline" -> "📅 Deadline"
                            "Duration" -> "⏱️ Durée"
                            else -> "⚡ Priorité"
                        }
                        Text(text = "Tri : $sortLabel", color = titleColor, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false },
                        modifier = Modifier.background(Color(0xFF1C1917))
                    ) {
                        DropdownMenuItem(
                            text = { Text("⚡ Priorité", color = Color.White) },
                            onClick = { sortBy = "Priority"; expandedSort = false }
                        )
                        DropdownMenuItem(
                            text = { Text("📅 Deadline", color = Color.White) },
                            onClick = { sortBy = "Deadline"; expandedSort = false }
                        )
                        DropdownMenuItem(
                            text = { Text("⏱️ Durée", color = Color.White) },
                            onClick = { sortBy = "Duration"; expandedSort = false }
                        )
                    }
                }
            }

            // Quick Toggle Status: Active vs Completed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Active", "Completed", "All").forEach { status ->
                    val statusLabel = when (status) {
                        "Active" -> if (viewModel.language.value == "FR") "En cours" else "Active"
                        "Completed" -> if (viewModel.language.value == "FR") "Complétées" else "Completed"
                        else -> if (viewModel.language.value == "FR") "Tout" else "All"
                    }
                    val isActive = selectedStatusFilter == status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) accentColor else Color(0x11FFFFFF)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { selectedStatusFilter = status }
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else labelColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main tasks list container
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section Aujourd'hui
                if (tasksToday.isNotEmpty()) {
                    item {
                        Text(
                            text = if (viewModel.language.value == "FR") "Aujourd'hui" else "Today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    items(tasksToday, key = { it.id }) { task ->
                        TaskItemBlock(task = task, viewModel = viewModel, accentColor = accentColor)
                    }
                }

                // Section Cette semaine
                if (tasksThisWeek.isNotEmpty()) {
                    item {
                        Text(
                            text = if (viewModel.language.value == "FR") "Cette semaine" else "This Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    items(tasksThisWeek, key = { it.id }) { task ->
                        TaskItemBlock(task = task, viewModel = viewModel, accentColor = accentColor)
                    }
                }

                // Section Plus tard
                if (tasksLater.isNotEmpty() || (tasksToday.isEmpty() && tasksThisWeek.isEmpty() && filteredSortedTasks.isNotEmpty())) {
                    val label = if (tasksLater.isNotEmpty()) {
                        if (viewModel.language.value == "FR") "Plus tard" else "Later"
                    } else {
                        if (viewModel.language.value == "FR") "Autres" else "Others"
                    }
                    item {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = labelColor,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    items(tasksLater, key = { it.id }) { task ->
                        TaskItemBlock(task = task, viewModel = viewModel, accentColor = accentColor)
                    }
                }

                // Empty state
                if (filteredSortedTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📝", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (viewModel.language.value == "FR") "Aucune tâche trouvée" else "No tasks found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = titleColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (viewModel.language.value == "FR") "Crée une tâche à réaliser et commence ton focus" else "Create a task and schedule your goals!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Beautiful Form overlay Dialog
        if (showAddDialog) {
            TaskCreationDialog(
                viewModel = viewModel,
                categories = categoriesList,
                accentColor = accentColor,
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun TaskItemBlock(task: Task, viewModel: PulseViewModel, accentColor: Color) {
    var expandedDetail by remember { mutableStateOf(false) }
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT
    val dynamicTitleColor = if (isLight) Color(0xFF1F2937) else Color.White
    val labelColor = if (isLight) Color.DarkGray else Color.LightGray

    val prioSymbol = when (task.priority) {
        2 -> "🔴"
        1 -> "🟡"
        else -> "🟢"
    }

    val deadlineText = remember(task.deadline) {
        if (task.deadline == 0L) {
            if (viewModel.language.value == "FR") "Pas de échéance" else "No deadline"
        } else {
            val date = Date(task.deadline)
            SimpleDateFormat("dd MMM (HH:mm)", if (viewModel.language.value == "FR") Locale.FRENCH else Locale.ENGLISH).format(date)
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandedDetail = !expandedDetail },
        cornerRadius = 14.dp,
        borderColor = if (task.isCompleted) (if (isLight) Color(0x0F000000) else Color(0x0EFFFFFF)) else accentColor.copy(alpha = 0.2f),
        isLight = isLight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { viewModel.completeTask(task, it) },
                colors = CheckboxDefaults.colors(checkedColor = accentColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (task.isCompleted) "${task.title} (Fait)" else task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) labelColor else dynamicTitleColor,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = prioSymbol, fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
                    Text(
                        text = "${task.category} • $deadlineText • ⏱️ ${task.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelColor
                    )
                }
            }

            Row {
                if (task.recurrence != "none") {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Récurent",
                        tint = accentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                }

                IconButton(onClick = { viewModel.deleteTask(task) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Subtask lists expanding
        AnimatedVisibility(visible = expandedDetail) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                Divider(color = Color(0x1BFFFFFF))
                Spacer(modifier = Modifier.height(10.dp))

                // Detail category recurrence info
                if (task.recurrence != "none") {
                    Text(
                        text = "Récurrence : ${task.recurrence.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Subtasks Header
                Text(
                    text = if (viewModel.language.value == "FR") "LISTE DES SOUS-TÂCHES :" else "SUB-TASKS CHECKLIST :",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )

                if (task.subtasks.isEmpty()) {
                    Text(
                        text = if (viewModel.language.value == "FR") "Aucune sous-tâche" else "No child sub-tasks listed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    task.subtasks.forEachIndexed { index, sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.isCompleted,
                                onCheckedChange = { viewModel.updateSubtask(task, index, it) },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                modifier = Modifier.scale(0.85f)
                            )
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (sub.isCompleted) labelColor else dynamicTitleColor,
                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationDialog(
    viewModel: PulseViewModel,
    categories: List<String>,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) } // Default Medium
    var category by remember { mutableStateOf(categories.first()) }
    var durationMinutes by remember { mutableStateOf("25") }
    var recurrence by remember { mutableStateOf("none") } // "none", "daily", "weekly", "monthly"

    val calendar = remember { Calendar.getInstance() }
    var deadlineDate by remember { mutableStateOf(calendar.timeInMillis) }
    var hasDeadline by remember { mutableStateOf(true) }

    // Subtasks builder state
    var newSubtaskText by remember { mutableStateOf("") }
    val builtSubtasks = remember { mutableStateListOf<SubTask>() }

    val context = LocalContext.current
    val isLight = viewModel.appTheme.value == com.example.viewmodel.PulseTheme.LIGHT
    val textStyleColor = if (isLight) Color.Black else Color.White

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) Color.White else Color(0xFF1E1B26)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = if (viewModel.language.value == "FR") "Nouvelle Tâche PULSE" else "Create New PULSE Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = textStyleColor
                    )
                }

                // Title Input
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre de la tâche") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor
                        ),
                        singleLine = true
                    )
                }

                // Category dropdown Select
                item {
                    var exp by remember { mutableStateOf(false) }
                    Column {
                        Text(
                            text = if (viewModel.language.value == "FR") "Catégorie" else "Category",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                .clickable { exp = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category, color = textStyleColor)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand",
                                    tint = textStyleColor
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = exp,
                            onDismissRequest = { exp = false },
                            modifier = Modifier.background(Color(0xFF1C1917))
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = { category = cat; exp = false }
                                )
                            }
                        }
                    }
                }

                // Priority Row Select (Buttons Layout)
                item {
                    Column {
                        Text(
                            text = if (viewModel.language.value == "FR") "Niveau de Priorité" else "Priority Rate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                0 to (if (viewModel.language.value == "FR") "🟢 Basse" else "🟢 Low"),
                                1 to (if (viewModel.language.value == "FR") "🟡 Moyenne" else "🟡 Mid"),
                                2 to (if (viewModel.language.value == "FR") "🔴 Haute" else "🔴 High")
                            ).forEach { (valInt, labelStr) ->
                                val selected = priority == valInt
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) accentColor else Color(0x1BFFFFFF)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { priority = valInt }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = labelStr,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Estimated minutes duration
                item {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("Durée estimée (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor
                        ),
                        singleLine = true
                    )
                }

                // Recurrence option
                item {
                    Column {
                        Text(
                            text = if (viewModel.language.value == "FR") "Récurrence" else "Recurrence",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("none", "daily", "weekly", "monthly").forEach { rec ->
                                val recLabel = when (rec) {
                                    "none" -> "Aucune"
                                    "daily" -> "Quotidien"
                                    "weekly" -> "Hebdo"
                                    else -> "Mensuel"
                                }
                                val active = recurrence == rec
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) accentColor else Color(0x0BFFFFFF)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { recurrence = rec }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = recLabel, fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Native Date Picker click handler
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewModel.language.value == "FR") "Définir une échéance" else "Set a deadline limit",
                            fontWeight = FontWeight.Bold,
                            color = textStyleColor,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = hasDeadline,
                            onCheckedChange = { hasDeadline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    if (hasDeadline) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date select click button
                            Button(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            calendar.set(Calendar.YEAR, year)
                                            calendar.set(Calendar.MONTH, month)
                                            calendar.set(Calendar.DAY_OF_MONTH, day)
                                            deadlineDate = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(deadlineDate)),
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            // Time select click button
                            Button(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            deadlineDate = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1BFFFFFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Heure") // Fallback clock standard icon
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(deadlineDate)),
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Subtask checklist interactive builder inside Dialog
                item {
                    Column {
                        Text(
                            text = if (viewModel.language.value == "FR") "Sous-tâches" else "Sub-tasks helper",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Faire la recherche, etc...") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newSubtaskText.isNotEmpty()) {
                                        builtSubtasks.add(SubTask(title = newSubtaskText))
                                        newSubtaskText = ""
                                    }
                                },
                                modifier = Modifier.background(accentColor, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add sub", tint = Color.White)
                            }
                        }

                        // Built sub list labels view
                        builtSubtasks.forEachIndexed { iIn, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("•", color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                                    Text(text = item.title, style = MaterialTheme.typography.bodySmall, color = textStyleColor)
                                }
                                IconButton(onClick = { builtSubtasks.removeAt(iIn) }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Dialog Buttons Actions
                item {
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
                            Text(text = if (viewModel.language.value == "FR") "Annuler" else "Cancel")
                        }

                        Button(
                            onClick = {
                                if (title.isNotEmpty()) {
                                    viewModel.addTask(
                                        title = title,
                                        priority = priority,
                                        category = category,
                                        deadline = if (hasDeadline) deadlineDate else 0L,
                                        durationMinutes = durationMinutes.toIntOrNull() ?: 25,
                                        recurrence = recurrence,
                                        subtasks = builtSubtasks.toList()
                                    )
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (viewModel.language.value == "FR") "Créer" else "Create")
                        }
                    }
                }
            }
        }
    }
}
