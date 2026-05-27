package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailSync(email: String): User?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUserSync(): User?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface CycleLogDao {
    @Query("SELECT * FROM cycle_logs WHERE userId = :userId ORDER BY startDate DESC")
    fun getCyclesForUser(userId: Int): Flow<List<CycleLog>>

    @Query("SELECT * FROM cycle_logs WHERE userId = :userId ORDER BY startDate DESC")
    suspend fun getCyclesForUserSync(userId: Int): List<CycleLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleLog): Long

    @Update
    suspend fun updateCycle(cycle: CycleLog)

    @Delete
    suspend fun deleteCycle(cycle: CycleLog)
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE userId = :userId ORDER BY date DESC")
    fun getDailyLogsForUser(userId: Int): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE userId = :userId AND date = :date LIMIT 1")
    fun getDailyLogForDate(userId: Int, date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyLogForDateSync(userId: Int, date: String): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLog): Long

    @Update
    suspend fun updateDailyLog(log: DailyLog)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = -1 ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<InAppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: InAppNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<InAppNotification>)

    @Update
    suspend fun updateNotification(notification: InAppNotification)

    @Delete
    suspend fun deleteNotification(notification: InAppNotification)
}
