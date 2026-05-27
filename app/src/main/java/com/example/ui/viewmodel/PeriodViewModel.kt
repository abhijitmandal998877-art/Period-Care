package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CycleLog
import com.example.data.database.DailyLog
import com.example.data.database.InAppNotification
import com.example.data.database.User
import com.example.data.repository.AppRepository
import com.example.data.utils.DateUtils
import com.example.data.utils.EncryptionHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class PeriodViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        db.userDao(),
        db.cycleLogDao(),
        db.dailyLogDao(),
        db.notificationDao()
    )

    // Current active authenticated user session
    val activeUser: StateFlow<User?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current logged-in user cycle history
    val cycleHistory: StateFlow<List<CycleLog>> = activeUser
        .flatMapLatest { user ->
            if (user != null) repository.getCycles(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current logged-in user daily logs (for symptom/mood tracking history)
    val dailyLogs: StateFlow<List<DailyLog>> = activeUser
        .flatMapLatest { user ->
            if (user != null) repository.getDailyLogs(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All notifications (including global notifications -1)
    val notifications: StateFlow<List<InAppNotification>> = activeUser
        .flatMapLatest { user ->
            if (user != null) repository.getNotifications(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Users Flow (Used strictly in Admin Panel to verify application statistics)
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interaction States ---
    private val _selectedLogDate = MutableStateFlow(DateUtils.getCurrentDateString())
    val selectedLogDate: StateFlow<String> = _selectedLogDate.asStateFlow()

    // Daily log flow for the selected date
    val selectDateDailyLog: StateFlow<DailyLog?> = combine(activeUser, _selectedLogDate) { user, date ->
        user to date
    }.flatMapLatest { (user, date) ->
        if (user != null) {
            repository.getDailyLogForDate(user.id, date)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sync State simulation
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class ConnectedAndSynced(val lastSyncedTime: String) : SyncState()
        object OfflineModeEnabled : SyncState()
    }

    init {
        // Trigger simulated Cloud Sync check on launch
        viewModelScope.launch {
            delay(1000)
            syncData()
        }
    }

    fun clearAuthErrorMessage() {
        _authError.value = null
    }

    // --- Action Methods ---

    // Signup / Registration
    fun register(fullName: String, email: String, passwordRaw: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (fullName.isBlank() || email.isBlank() || passwordRaw.isBlank()) {
                _authError.value = "All fields are required"
                onCompleted(false)
                return@launch
            }
            if (!email.contains("@")) {
                _authError.value = "Invalid email format"
                onCompleted(false)
                return@launch
            }
            if (passwordRaw.length < 6) {
                _authError.value = "Password must be at least 6 characters"
                onCompleted(false)
                return@launch
            }
            
            val success = repository.registerUser(fullName, email, passwordRaw)
            if (!success) {
                _authError.value = "User with this email already exists"
                onCompleted(false)
            } else {
                onCompleted(true)
            }
        }
    }

    // Login
    fun login(email: String, passwordRaw: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || passwordRaw.isBlank()) {
                _authError.value = "Please fill in all credentials"
                onCompleted(false)
                return@launch
            }
            val success = repository.loginUser(email, passwordRaw)
            if (success) {
                onCompleted(true)
                syncData()
            } else {
                _authError.value = "Invalid email or password"
                onCompleted(false)
            }
        }
    }

    // Forgot Password Flow
    fun forgotPasswordReset(email: String, newPasswordRaw: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || newPasswordRaw.isBlank()) {
                _authError.value = "Email and new password are required"
                onCompleted(false)
                return@launch
            }
            if (newPasswordRaw.length < 6) {
                _authError.value = "New password must be at least 6 characters"
                onCompleted(false)
                return@launch
            }
            val success = repository.forgotPasswordAndReset(email, newPasswordRaw)
            if (success) {
                onCompleted(true)
            } else {
                _authError.value = "No registered user found with this email"
                onCompleted(false)
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            repository.logoutActiveUser()
        }
    }

    // Change Password (from profile settings)
    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (oldPass.isBlank() || newPass.isBlank()) {
                onResult(false, "Passwords must not be empty!")
                return@launch
            }
            if (newPass.length < 6) {
                onResult(false, "New password must be at least 6 characters")
                return@launch
            }
            val success = repository.changePasswordForActiveUser(oldPass, newPass)
            if (success) {
                onResult(true, "Password changed successfully!")
            } else {
                onResult(false, "Incorrect current password")
            }
        }
    }

    // Update Account Settings / Reminders
    fun updateCycleSettings(cycleLength: Int, periodLength: Int) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(
                cycleLength = cycleLength,
                periodLength = periodLength
            ))
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(
                reminderHour = hour,
                reminderMinute = minute
            ))
        }
    }

    fun updateWaterGoal(goalMl: Int) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(
                waterGoalMl = goalMl
            ))
        }
    }

    fun toggleEncryption(enabled: Boolean) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(
                isLocalEncryptionEnabled = enabled
            ))
            delay(400)
            // Push notification explaining private state
            db.notificationDao().insertNotification(
                InAppNotification(
                    userId = user.id,
                    title = if (enabled) "AES-256 Local Encryption: ACTIVE" else "Standard Storage Enabled",
                    message = if (enabled) 
                        "Your period, mood, and symptom logs are now heavily scrambled locally inside Room Database using industrial cryptography."
                    else "Encryption disabled. Your logs are stored standard locally for faster debugging.",
                    type = "alert"
                )
            )
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(
                isCloudSyncEnabled = enabled
            ))
            if (enabled) {
                syncData()
            } else {
                _syncState.value = SyncState.OfflineModeEnabled
            }
        }
    }

    // --- Tracking logs ---
    fun selectLogDate(date: String) {
        _selectedLogDate.value = date
    }

    fun trackPeriodStartToday() {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.trackPeriodStart(user.id, DateUtils.getCurrentDateString())
        }
    }

    fun trackPeriodStartOnSelectedDate() {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.trackPeriodStart(user.id, _selectedLogDate.value)
        }
    }

    fun saveSelectedDateDailyLog(mood: String, symptom: String, severity: String, waterMl: Int, notes: String) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.saveDailyLog(user.id, _selectedLogDate.value, mood, symptom, severity, waterMl, notes)
        }
    }

    // --- Admin panel actions ---
    fun sendCustomAdminNotification(targetEmail: String?, title: String, message: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.sendAdminNotification(targetEmail, title, message)
            onCompleted(success)
        }
    }

    // --- Simulated Sync Data (Offline / Online fallback) ---
    fun syncData() {
        val user = activeUser.value
        if (user == null || !user.isCloudSyncEnabled) {
            _syncState.value = SyncState.OfflineModeEnabled
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            delay(1500) // Simulate cloud connection latency
            val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            _syncState.value = SyncState.ConnectedAndSynced(timeString)
        }
    }
}
