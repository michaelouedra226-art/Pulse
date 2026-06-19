package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.AmbientSoundGenerator
import com.example.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PulseAccentColor(val hex: Long, val displayNameFr: String, val displayNameEn: String) {
    VIOLET(0xFF7C3AED, "Violet électrique", "Electric Purple"),
    CYAN(0xFF06B6D4, "Cyan glacier", "Glacier Cyan"),
    ORANGE(0xFFF97316, "Orange couchant", "Sunset Orange"),
    GREEN(0xFF10B981, "Vert émeraude", "Emerald Green"),
    BLUE(0xFF3B82F6, "Bleu royal", "Royal Blue"),
    PINK(0xFFEC4899, "Rose fuchsia", "Fuchsia Pink")
}

enum class PulseTheme {
    DARK, AMOLED, LIGHT
}

class PulseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = PulseRepository(db.pulseDao())
    private val notificationHelper = NotificationHelper(application)
    private val soundGenerator = AmbientSoundGenerator()

    // Exposed Flows from Room
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<Event>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings States
    var userName = mutableStateOf("Michael")
    var userAvatar = mutableStateOf("⚡") // emoji selector or icon string
    var appTheme = mutableStateOf(PulseTheme.DARK)
    var accentColor = mutableStateOf(PulseAccentColor.VIOLET)
    var language = mutableStateOf("FR") // "FR" or "EN"
    var notifUrgente = mutableStateOf(true)
    var notifPomComplete = mutableStateOf(true)

    // Custom Durations (in minutes)
    var focusDuration = mutableStateOf(25)
    var shortBreakDuration = mutableStateOf(5)
    var longBreakDuration = mutableStateOf(15)

    // Active Focus States
    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _currentTimerMode = MutableStateFlow("focus") // "focus", "short_break", "long_break"
    val currentTimerMode: StateFlow<String> = _currentTimerMode.asStateFlow()

    private val _linkedTaskId = MutableStateFlow<Int?>(null)
    val linkedTaskId: StateFlow<Int?> = _linkedTaskId.asStateFlow()

    private val _ambientSoundType = MutableStateFlow("Silence")
    val ambientSoundType: StateFlow<String> = _ambientSoundType.asStateFlow()

    private val _pomsCompletedCount = MutableStateFlow(0)
    val pomsCompletedCount: StateFlow<Int> = _pomsCompletedCount.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Prepare initial loaded timer
        _timeLeftSeconds.value = focusDuration.value * 60
    }

    // Task Actions
    fun addTask(
        title: String,
        priority: Int,
        category: String,
        deadline: Long,
        durationMinutes: Int,
        recurrence: String = "none",
        subtasks: List<SubTask> = emptyList()
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                priority = priority,
                category = category,
                deadline = deadline,
                durationMinutes = durationMinutes,
                recurrence = recurrence,
                subtasks = subtasks
            )
            repository.insertTask(task)
            
            // Check deadline for sending near deadline notification warning
            // Normally scheduled, let's keep track locally or mock remind 30m before
        }
    }

    fun completeTask(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.insertTask(updatedTask)

            // If recurrences apply, auto spawn duplicate tomorrow / next cycle
            if (isCompleted && task.recurrence != "none") {
                val nextDeadline = when (task.recurrence) {
                    "daily" -> task.deadline + 24 * 60 * 60 * 1000
                    "weekly" -> task.deadline + 7 * 24 * 60 * 60 * 1000
                    "monthly" -> task.deadline + 30L * 24 * 60 * 60 * 1000
                    else -> task.deadline
                }
                val recurredTask = task.copy(
                    id = 0,
                    isCompleted = false,
                    deadline = nextDeadline,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertTask(recurredTask)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun updateSubtask(task: Task, subtaskIndex: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedSubtasks = task.subtasks.toMutableList()
            if (subtaskIndex in updatedSubtasks.indices) {
                updatedSubtasks[subtaskIndex] = updatedSubtasks[subtaskIndex].copy(isCompleted = isCompleted)
                val updatedTask = task.copy(subtasks = updatedSubtasks)
                repository.insertTask(updatedTask)
            }
        }
    }

    // Focus Actions
    fun startFocusTimer() {
        if (_isTimerActive.value) return
        _isTimerActive.value = true

        // Play sound if not silence
        soundGenerator.startSound(getApplication(), _ambientSoundType.value)

        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0) {
                delay(1000)
                _timeLeftSeconds.value -= 1
            }
            onTimerComplete()
        }
    }

    fun pauseFocusTimer() {
        _isTimerActive.value = false
        timerJob?.cancel()
        soundGenerator.stopSound()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        _timeLeftSeconds.value = getCurrentDurationMinutes() * 60
    }

    fun setTimerMode(mode: String) {
        pauseFocusTimer()
        _currentTimerMode.value = mode
        _timeLeftSeconds.value = getCurrentDurationMinutes() * 60
    }

    fun selectLinkedTask(id: Int?) {
        _linkedTaskId.value = id
    }

    fun changeAmbientSound(sound: String) {
        _ambientSoundType.value = sound
        if (_isTimerActive.value) {
            soundGenerator.startSound(getApplication(), sound)
        }
    }

    private fun getCurrentDurationMinutes(): Int {
        return when (_currentTimerMode.value) {
            "focus" -> focusDuration.value
            "short_break" -> shortBreakDuration.value
            "long_break" -> longBreakDuration.value
            else -> focusDuration.value
        }
    }

    private suspend fun onTimerComplete() {
        _isTimerActive.value = false
        soundGenerator.stopSound()

        val modeText = when (_currentTimerMode.value) {
            "focus" -> if (language.value == "FR") "Session de Focus terminée !" else "Focus Session Complete!"
            "short_break" -> if (language.value == "FR") "Pause courte terminée, au travail !" else "Short break complete, back to work!"
            "long_break" -> if (language.value == "FR") "Pause longue terminée !" else "Long break complete!"
            else -> "Fin de session"
        }
        val detailText = when (_currentTimerMode.value) {
            "focus" -> if (language.value == "FR") "Félicitations ! Tu as complété un cycle focus 🔥" else "Great job! You completed a focus cycle 🔥"
            else -> if (language.value == "FR") "Prêt pour relever de nouveaux défis ?" else "Ready to tackle your goals?"
        }

        // Send push notification
        notificationHelper.sendNotification(modeText, detailText)

        // Play alarm sound and vibrate device if enabled in Settings
        if (notifPomComplete.value) {
            notificationHelper.playAlarmFeedback(vibrate = true, playSound = true)
        }

        // Save session if in focus mode
        if (_currentTimerMode.value == "focus") {
            val session = FocusSession(
                taskId = _linkedTaskId.value,
                durationMinutes = focusDuration.value,
                mode = "focus"
            )
            repository.insertFocusSession(session)

            // Update poms completed count and transition helper
            _pomsCompletedCount.value += 1
            val nextMode = if (_pomsCompletedCount.value % 4 == 0) "long_break" else "short_break"
            setTimerMode(nextMode)
        } else {
            setTimerMode("focus")
        }
    }

    // Planning Events Actions
    fun addEvent(title: String, startTime: Long, endTime: Long, colorHex: Int, location: String = "") {
        viewModelScope.launch {
            val event = Event(
                title = title,
                startTime = startTime,
                endTime = endTime,
                colorHex = colorHex,
                location = location
            )
            repository.insertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // Config duration updates
    fun updateDurations(focusMins: Int, shortMins: Int, longMins: Int) {
        focusDuration.value = focusMins
        shortBreakDuration.value = shortMins
        longBreakDuration.value = longMins
        // Reset active timer if not running
        if (!_isTimerActive.value) {
            _timeLeftSeconds.value = getCurrentDurationMinutes() * 60
        }
    }

    // System management / reset
    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllTasks()
            repository.deleteAllFocusSessions()
            repository.deleteAllEvents()
            
            // resets
            userName.value = "Michael"
            userAvatar.value = "⚡"
            notifUrgente.value = true
            notifPomComplete.value = true
            appTheme.value = PulseTheme.DARK
            accentColor.value = PulseAccentColor.VIOLET
            focusDuration.value = 25
            shortBreakDuration.value = 5
            longBreakDuration.value = 15
            _pomsCompletedCount.value = 0
            _currentTimerMode.value = "focus"
            _timeLeftSeconds.value = 25 * 60
            _isTimerActive.value = false
            _ambientSoundType.value = "Silence"
            _linkedTaskId.value = null
            soundGenerator.stopSound()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundGenerator.stopSound()
    }
}
