package com.example.pineappleexpense.model

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//instantiates only one instance of the local room database
object DatabaseInstance {
    private var INSTANCE: AppLocalDatabase? = null

    fun getDatabase(context: Context): AppLocalDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppLocalDatabase::class.java,
                "app_local_database"
            ).addMigrations(MIGRATION_1_2).build()
            INSTANCE = instance
            instance
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE report_table (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, expenseIds TEXT NOT NULL)"
        )
    }
}
