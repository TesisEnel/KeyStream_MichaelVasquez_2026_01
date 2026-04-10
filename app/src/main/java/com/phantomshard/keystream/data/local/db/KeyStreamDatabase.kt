package com.phantomshard.keystream.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phantomshard.keystream.data.local.dao.CategoryDao
import com.phantomshard.keystream.data.local.dao.ServiceDao
import com.phantomshard.keystream.data.local.entity.CategoryEntity
import com.phantomshard.keystream.data.local.entity.ServiceEntity

@Database(
    entities = [CategoryEntity::class, ServiceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KeyStreamDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServiceDao
}
