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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build()
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

// Migration from version 4 to 5 to add the category_mappings table
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE category_mappings (category TEXT PRIMARY KEY NOT NULL, accountCode TEXT NOT NULL)"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Create the new table
        db.execSQL("""
      CREATE TABLE IF NOT EXISTS `new_report_table` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `name` TEXT NOT NULL,
        `expenseIds` TEXT NOT NULL,
        `status` TEXT NOT NULL,
        `userName` TEXT NOT NULL,
        `comment` TEXT NOT NULL
      )
    """.trimIndent())

        // 2) Copy the old rows, casting id to text
        db.execSQL("""
      INSERT INTO `new_report_table` (
        id, name, expenseIds, status, userName, comment
      )
      SELECT
        CAST(id AS TEXT),
        name,
        expenseIds,
        status,
        userName,
        comment
      FROM `report_table`
    """.trimIndent())

        // 3) Drop the old table
        db.execSQL("DROP TABLE `report_table`")

        // 4) Rename new table
        db.execSQL("ALTER TABLE `new_report_table` RENAME TO `report_table`")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Create a new table with `id` as TEXT PK
        db.execSQL("""
      CREATE TABLE IF NOT EXISTS `new_expense_table` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `title` TEXT NOT NULL,
        `total` REAL NOT NULL,
        `date` INTEGER NOT NULL,
        `comment` TEXT NOT NULL,
        `category` TEXT NOT NULL,
        `imageUri` TEXT
      )
    """.trimIndent())

        // 2) Copy all data, casting the old integer id → text
        db.execSQL("""
      INSERT INTO `new_expense_table` (
        id, title, total, date, comment, category, imageUri
      )
      SELECT
        CAST(id AS TEXT),
        title,
        total,
        date,
        comment,
        category,
        imageUri
      FROM `expense_table`
    """.trimIndent())

        // 3) Drop the old table…
        db.execSQL("DROP TABLE `expense_table`")

        // 4) …and rename the new one
        db.execSQL("ALTER TABLE `new_expense_table` RENAME TO `expense_table`")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Create a new table with expenseIds as TEXT
        db.execSQL("""
      CREATE TABLE IF NOT EXISTS `new_report_table` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `name` TEXT NOT NULL,
        `expenseIds` TEXT NOT NULL,
        `status` TEXT NOT NULL,
        `userName` TEXT NOT NULL,
        `comment` TEXT NOT NULL
      )
    """.trimIndent())

        // 2) Copy all rows, casting the old JSON-string to TEXT again.
        //    (We store lists as JSON in TEXT, so no further SQL transform is needed here.)
        db.execSQL("""
      INSERT INTO `new_report_table` (
        id, name, expenseIds, status, userName, comment
      )
      SELECT
        id,
        name,
        CAST(expenseIds AS TEXT),
        status,
        userName,
        comment
      FROM `report_table`
    """.trimIndent())

        // 3) Drop the old table…
        db.execSQL("DROP TABLE `report_table`")

        // 4) …and rename the new one
        db.execSQL("ALTER TABLE `new_report_table` RENAME TO `report_table`")
    }
}

