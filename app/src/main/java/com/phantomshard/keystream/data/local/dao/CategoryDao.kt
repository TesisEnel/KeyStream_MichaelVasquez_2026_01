package com.phantomshard.keystream.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.phantomshard.keystream.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("""
        SELECT * FROM categories
        WHERE (:search IS NULL OR name LIKE '%' || :search || '%')
        AND isPendingDelete = 0
        ORDER BY name ASC
    """)
    suspend fun getAll(search: String? = null): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isPendingCreate = 1")
    suspend fun getPendingCreate(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isPendingUpdate = 1")
    suspend fun getPendingUpdate(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isPendingDelete = 1")
    suspend fun getPendingDelete(): List<CategoryEntity>

    @Query("UPDATE categories SET isPendingCreate = 0, isPendingUpdate = 0, isPendingDelete = 0 WHERE id = :id")
    suspend fun clearPendingFlags(id: String)
}
