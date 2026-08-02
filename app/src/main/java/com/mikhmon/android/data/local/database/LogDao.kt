package com.mikhmon.android.data.local.database

import androidx.room.*
import com.mikhmon.android.core.logging.LogEntry
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for LogEntry entity
 */
@Dao
interface LogDao {
    
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 500): Flow<List<LogEntryEntity>>
    
    @Query("SELECT * FROM log_entries WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<LogEntryEntity>>
    
    @Query("SELECT * FROM log_entries WHERE correlationId = :correlationId ORDER BY timestamp ASC")
    suspend fun getLogsByCorrelationId(correlationId: String): List<LogEntryEntity>
    
    @Query("SELECT * FROM log_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getLogsBetween(startTime: Long, endTime: Long): List<LogEntryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEntryEntity>)
    
    @Query("DELETE FROM log_entries WHERE timestamp < :beforeTime")
    suspend fun deleteOldLogs(beforeTime: Long): Int
    
    @Query("DELETE FROM log_entries")
    suspend fun deleteAllLogs()
    
    @Query("SELECT COUNT(*) FROM log_entries")
    suspend fun getLogCount(): Int
    
    @Query("SELECT DISTINCT category FROM log_entries")
    suspend fun getCategories(): List<String>
}
