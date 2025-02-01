package com.example.pineappleexpense.model

import android.content.Context
import androidx.room.Room

//instantiates only one instance of the local room database
object DatabaseInstance {
    private var INSTANCE: AppLocalDatabase? = null

    fun getDatabase(context: Context): AppLocalDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppLocalDatabase::class.java,
                "expense_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
