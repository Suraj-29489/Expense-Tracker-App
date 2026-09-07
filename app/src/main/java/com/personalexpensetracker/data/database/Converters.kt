package com.personalexpensetracker.data.database

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room TypeConverters for custom types.
 * Converts [Instant] to and from epoch milliseconds stored as SQLite INTEGER.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}

