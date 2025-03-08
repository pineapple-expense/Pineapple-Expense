package com.example.pineappleexpense.model

import android.net.Uri
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

//main access point for the room database to store expenses
@Database(entities = [Expense::class, Report::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppLocalDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun reportDao(): ReportDao
}

//needed to convert complex types to and from ones that can be stored in the database
class Converters {

    // Converter for Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Converter for Uri
    @TypeConverter
    fun fromUri(uri: String?): Uri? {
        return uri?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }

    private val gson = Gson()

    // Convert List<Int> to a JSON string
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return gson.toJson(list)
    }

    // Convert a JSON string back to List<Int>
    @TypeConverter
    fun toIntList(json: String?): List<Int>? {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type)
    }
}
