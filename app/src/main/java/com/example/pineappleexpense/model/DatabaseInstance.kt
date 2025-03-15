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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
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

// Migration from version 2 to 3 to add the status column
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new column 'status' with a default value "unsubmitted"
        database.execSQL("ALTER TABLE report_table ADD COLUMN status TEXT NOT NULL DEFAULT 'Unsubmitted'")
    }
}

// Migration from version 3 to 4 to add the userName and comment columns
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new columns 'userName' and 'comment' with default empty values
        database.execSQL("ALTER TABLE report_table ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE report_table ADD COLUMN comment TEXT NOT NULL DEFAULT ''")
    }
}