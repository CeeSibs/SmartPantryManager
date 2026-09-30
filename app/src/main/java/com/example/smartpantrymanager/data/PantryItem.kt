package com.example.smartpantrymanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val quantity: Int,
    val category: String,
    val expiryDate: String
)