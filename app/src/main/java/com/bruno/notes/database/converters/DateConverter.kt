package com.bruno.notes.database.converters

import androidx.room.TypeConverter
import java.sql.Date

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

}