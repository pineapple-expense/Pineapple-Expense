package com.example.pineappleexpense.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_mappings")
data class CategoryMapping(
    @PrimaryKey
    val category: String,
    val accountCode: String
) 