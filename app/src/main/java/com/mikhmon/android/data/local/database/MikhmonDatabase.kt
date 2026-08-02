package com.mikhmon.android.data.local.database

import androidx.room.*
import com.mikhmon.android.core.logging.LogEntry
import com.mikhmon.android.data.model.Router
import kotlinx.datetime.LocalDateTime

/**
 * Main Room database for Mikhmon Android
 */
@Database(
    entities = [
        Router::class,
        LogEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MikhmonDatabase : RoomDatabase() {
    abstract fun routerDao(): RouterDao
    abstract fun logDao(): LogDao
}

/**
 * LogEntry entity for Room
 */
@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long, // stored as epoch millis
    val level: String,
    val category: String,
    val correlationId: String?,
    val message: String,
    val throwableMessage: String?
) {
    fun toLogEntry(): LogEntry {
        return LogEntry(
            id = id,
            timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
            level = com.mikhmon.android.core.logging.LogLevel.valueOf(level),
            category = category,
            correlationId = correlationId,
            message = message,
            throwableMessage = throwableMessage
        )
    }
    
    companion object {
        fun fromLogEntry(entry: LogEntry): LogEntryEntity {
            return LogEntryEntity(
                id = entry.id,
                timestamp = kotlinx.datetime.Instant.fromEpochSeconds(
                    entry.timestamp.year * 31536000L + entry.timestamp.monthNumber * 2592000L + entry.timestamp.dayOfMonth * 86400L +
                    entry.timestamp.hour * 3600L + entry.timestamp.minute * 60L + entry.timestamp.second
                ).toEpochMilliseconds(),
                level = entry.level.name,
                category = entry.category,
                correlationId = entry.correlationId,
                message = entry.message,
                throwableMessage = entry.throwableMessage
            )
        }
    }
}

/**
 * Type converters for Room
 */
class Converters {
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.let {
            kotlinx.datetime.Instant.fromEpochSeconds(
                it.year * 31536000L + it.monthNumber * 2592000L + it.dayOfMonth * 86400L +
                it.hour * 3600L + it.minute * 60L + it.second
            ).toEpochMilliseconds()
        }
    }
    
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        }
    }
}
