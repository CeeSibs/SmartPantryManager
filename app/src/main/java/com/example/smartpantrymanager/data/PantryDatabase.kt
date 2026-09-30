package com.example.smartpantrymanager.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PantryItem::class],
    version = 1,
    exportSchema = false
)
abstract class PantryDatabase : RoomDatabase() {

    abstract fun pantryDao(): PantryDao
}