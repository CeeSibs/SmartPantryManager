package com.example.smartpantrymanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PantryDao {

    @Insert
    fun insertItem(item: PantryItem)

    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    fun getAllItems(): List<PantryItem>

    @Update
    fun updateItem(item: PantryItem)

    @Delete
    fun deleteItem(item: PantryItem)
}