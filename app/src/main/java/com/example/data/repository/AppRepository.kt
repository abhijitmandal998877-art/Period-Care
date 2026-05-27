package com.example.data.repository

import com.example.data.database.*
import com.example.data.utils.DateUtils
import com.example.data.utils.EncryptionHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val userDao: UserDao,
    private val cycleLogDao: CycleLogDao,
    private val dailyLogDao: DailyLogDao,
    private val notificationDao: NotificationDao
) {
    // Current Active (logged in) User Flow
    val activeUser: Flow<User?> = userDao.getActiveUser().map { user ->
        user?.let { decryptIfNeeded(it) }
    }

    // List of all registered users (for Admin Statistics)
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow().map { list ->
        list.map { decryptIfNeeded(it) }
    }

    // Get notifications for a user (including global alerts with userId = -1)
    fun getNotifications(userId: Int): Flow<List<InAppNotification>> {
        return notificationDao.getNotificationsForUser(userId)
    }

    // Get cycle history
    fun getCycles(userId: Int): Flow<List<CycleLog>> {
        return cycleLogDao.getCyclesForUser(userId)
    }

    // Get daily logs for analytics
    fun getDailyLogs(userId: Int): Flow<List<DailyLog>> {
        return dailyLogDao.getDailyLogsForUser(userId).map { logs ->
            logs.map { decryptDailyLogIfNeeded(it, userId) }
        }
    }

    // Get log for specific date
    fun getDailyLogForDate(userId: Int, date: String): Flow<DailyLog?> {
        return dailyLogDao.getDailyLogForDate(userId, date).map { log ->
            log?.let { decryptDailyLogIfNeeded(it, userId) }
        }
    }

    // -- Authentication flow --
    suspend fun registerUser(fullName: String, email: String, passwordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmailSync(email)
        val existingEncrypted = userDao.getUserByEmailSync(EncryptionHelper.encrypt(email))
        if (existing != null || existingEncrypted != null) {
            return@withContext false // User already exists
        }

        // Auto-encrypt user credentials during signup
        val encryptedName = EncryptionHelper.encrypt(fullName)
        val encryptedEmail = EncryptionHelper.encrypt(email)
        val encryptedPassword = EncryptionHelper.encrypt(passwordRaw)

        val newUser = User(
            fullName = encryptedName,
            email = encryptedEmail,
            passwordHash = encryptedPassword,
            isLoggedIn = true,
            isLocalEncryptionEnabled = true
        )

        val newId = userDao.insertUser(newUser).toInt()

        // Inject initial tips and welcome message
        val welcomeNotif = InAppNotification(
            userId = newId,
            title = "Welcome to Period Care!",
            message = "Hi $fullName, thank you for joining! Track your symptoms, log your mood daily, practice customized yoga inside the app, and secure your data private offline.",
            type = "info"
        )
        val hydrationTip = InAppNotification(
            userId = newId,
            title = "Welcome Tip: Stay Hydrated!",
            message = "During period days, average hydration speeds up cramp recovery! Aim to drink at least 2,000ml of water.",
            type = "tip"
        )
        notificationDao.insertNotifications(listOf(welcomeNotif, hydrationTip))
        true
    }

    suspend fun loginUser(email: String, passwordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val allUsersFromDb = userDao.getAllUsersSync()
        // Try to match decrypted or encrypted email
        val matchedUser = allUsersFromDb.find {
            val decryptedEmail = EncryptionHelper.decrypt(it.email)
            decryptedEmail.equals(email, ignoreCase = true) || it.email.equals(email, ignoreCase = true)
        } ?: return@withContext false

        // Match password
        val decryptedPass = EncryptionHelper.decrypt(matchedUser.passwordHash)
        val isPasswordCorrect = decryptedPass == passwordRaw || matchedUser.passwordHash == passwordRaw

        if (isPasswordCorrect) {
            // Log out previous active sessions just in case
            val active = userDao.getActiveUserSync()
            if (active != null) {
                userDao.updateUser(active.copy(isLoggedIn = false))
            }
            // Set current user as logged in
            userDao.updateUser(matchedUser.copy(isLoggedIn = true))
            true
        } else {
            false
        }
    }

    suspend fun logoutActiveUser() = withContext(Dispatchers.IO) {
        val active = userDao.getActiveUserSync()
        if (active != null) {
            userDao.updateUser(active.copy(isLoggedIn = false))
        }
    }

    suspend fun forgotPasswordAndReset(email: String, newPasswordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val allUsersFromDb = userDao.getAllUsersSync()
        val matchedUser = allUsersFromDb.find {
            val decryptedEmail = EncryptionHelper.decrypt(it.email)
            decryptedEmail.equals(email, ignoreCase = true) || it.email.equals(email, ignoreCase = true)
        } ?: return@withContext false

        // Reset password
        val encryptedNewPass = EncryptionHelper.encrypt(newPasswordRaw)
        userDao.updateUser(matchedUser.copy(passwordHash = encryptedNewPass))
        true
    }

    suspend fun changePasswordForActiveUser(oldPasswordRaw: String, newPasswordRaw: String): Boolean = withContext(Dispatchers.IO) {
        val active = userDao.getActiveUserSync() ?: return@withContext false
        val decryptedPass = EncryptionHelper.decrypt(active.passwordHash)
        if (decryptedPass == oldPasswordRaw || active.passwordHash == oldPasswordRaw) {
            val encryptedNewPass = EncryptionHelper.encrypt(newPasswordRaw)
            userDao.updateUser(active.copy(passwordHash = encryptedNewPass))
            true
        } else {
            false
        }
    }

    suspend fun updateUserProfile(updatedUser: User) = withContext(Dispatchers.IO) {
        // Before updating, re-encrypt fields
        val encryptedUser = encryptUser(updatedUser)
        userDao.updateUser(encryptedUser)
    }

    // -- Cycle logs operations --
    suspend fun trackPeriodStart(userId: Int, date: String) = withContext(Dispatchers.IO) {
        // Get active cycle configs
        val user = userDao.getUserByIdSync(userId) ?: return@withContext
        val newLog = CycleLog(
            userId = userId,
            startDate = date,
            cycleLengthDays = user.cycleLength,
            periodLengthDays = user.periodLength
        )
        cycleLogDao.insertCycle(newLog)

        // Also update standard user lastPeriodStartDate
        userDao.updateUser(user.copy(lastPeriodStartDate = date))

        // Trigger dynamic period recommendations inside notifications
        notificationDao.insertNotification(
            InAppNotification(
                userId = userId,
                title = "Your Period Started!",
                message = "Stay strong! On cycle days, track your water intake and symptom progression daily. Try our Menstrual Yoga to relieve cramps.",
                type = "period"
            )
        )
    }

    suspend fun saveDailyLog(userId: Int, date: String, mood: String, symptom: String, severity: String, waterMl: Int, notes: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext
        val isEncrypted = user.isLocalEncryptionEnabled

        val finalMood = if (isEncrypted) EncryptionHelper.encrypt(mood) else mood
        val finalSymptom = if (isEncrypted) EncryptionHelper.encrypt(symptom) else symptom
        val finalSeverity = if (isEncrypted) EncryptionHelper.encrypt(severity) else severity
        val finalNotes = if (isEncrypted) EncryptionHelper.encrypt(notes) else notes

        val existingLog = dailyLogDao.getDailyLogForDateSync(userId, date)
        if (existingLog != null) {
            dailyLogDao.insertDailyLog(
                existingLog.copy(
                    mood = finalMood,
                    symptom = finalSymptom,
                    symptomSeverity = finalSeverity,
                    waterIntakeMl = waterMl,
                    notes = finalNotes
                )
            )
        } else {
            dailyLogDao.insertDailyLog(
                DailyLog(
                    userId = userId,
                    date = date,
                    mood = finalMood,
                    symptom = finalSymptom,
                    symptomSeverity = finalSeverity,
                    waterIntakeMl = waterMl,
                    notes = finalNotes
                )
            )
        }

        // Trigger warning alert if symptom progression is 'Severe'
        if (severity == "Severe") {
            notificationDao.insertNotification(
                InAppNotification(
                    userId = userId,
                    title = "Symptom Warning: Severe $symptom",
                    message = "You've logged severe symptoms today. Ensure rest, avoid high sodium, practice slow core yoga, and consult a doctor if severe pain persists.",
                    type = "alert"
                )
            )
        }
    }

    // -- Admin notification sending --
    suspend fun sendAdminNotification(targetEmail: String?, title: String, message: String): Boolean = withContext(Dispatchers.IO) {
        if (targetEmail.isNullOrBlank()) {
            // Global Notification (-1)
            notificationDao.insertNotification(
                InAppNotification(
                    userId = -1,
                    title = "[Announcement] $title",
                    message = message,
                    type = "admin"
                )
            )
            true
        } else {
            // Specific User Notification
            val allUsersFromDb = userDao.getAllUsersSync()
            val matchedUser = allUsersFromDb.find {
                val decrypted = EncryptionHelper.decrypt(it.email)
                decrypted.equals(targetEmail, ignoreCase = true) || it.email.equals(targetEmail, ignoreCase = true)
            } ?: return@withContext false

            notificationDao.insertNotification(
                InAppNotification(
                    userId = matchedUser.id,
                    title = "[Support Alert] $title",
                    message = message,
                    type = "admin"
                )
            )
            true
        }
    }

    // -- Utility Encryption/Decryption maps --
    private fun decryptIfNeeded(user: User): User {
        val decryptName = EncryptionHelper.decrypt(user.fullName)
        val decryptEmail = EncryptionHelper.decrypt(user.email)
        return user.copy(fullName = decryptName, email = decryptEmail)
    }

    private fun encryptUser(user: User): User {
        val encryptName = EncryptionHelper.encrypt(user.fullName)
        val encryptEmail = EncryptionHelper.encrypt(user.email)
        return user.copy(fullName = encryptName, email = encryptEmail)
    }

    private fun decryptDailyLogIfNeeded(log: DailyLog, userId: Int): DailyLog {
        // Daily logs are saved either encrypted or plain, we try to decrypt, or fallback to plain
        val decMood = EncryptionHelper.decrypt(log.mood)
        val decSymptom = EncryptionHelper.decrypt(log.symptom)
        val decSeverity = EncryptionHelper.decrypt(log.symptomSeverity)
        val decNotes = EncryptionHelper.decrypt(log.notes)

        // If decryption results in something non-empty and readable, use it, else keep originals
        return log.copy(
            mood = if (decMood.isNotBlank()) decMood else log.mood,
            symptom = if (decSymptom.isNotBlank()) decSymptom else log.symptom,
            symptomSeverity = if (decSeverity.isNotBlank()) decSeverity else log.symptomSeverity,
            notes = if (decNotes.isNotBlank()) decNotes else log.notes
        )
    }
}
