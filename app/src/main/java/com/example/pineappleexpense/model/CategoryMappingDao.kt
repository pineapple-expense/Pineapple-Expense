package com.example.pineappleexpense.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryMappingDao {
    @Query("SELECT * FROM category_mappings")
    fun getAllMappings(): Flow<List<CategoryMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: CategoryMapping)

    @Delete
    suspend fun deleteMapping(mapping: CategoryMapping)

    @Query("SELECT * FROM category_mappings WHERE category = :category")
    suspend fun getMappingByCategory(category: String): CategoryMapping?
} 