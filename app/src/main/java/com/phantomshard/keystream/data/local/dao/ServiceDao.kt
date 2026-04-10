package com.phantomshard.keystream.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.phantomshard.keystream.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {

    @Upsert
    suspend fun upsertAll(services: List<ServiceEntity>)

    @Upsert
    suspend fun upsert(service: ServiceEntity)

    @Query("DELETE FROM services")
    suspend fun deleteAll()

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: String): ServiceEntity?

    @Query("""
        SELECT * FROM services
        WHERE (:search IS NULL OR name LIKE '%' || :search || '%')
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND isPendingDelete = 0
        ORDER BY name ASC
    """)
    suspend fun getAll(search: String? = null, categoryId: String? = null): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE isPendingCreate = 1")
    suspend fun getPendingCreate(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE isPendingUpdate = 1")
    suspend fun getPendingUpdate(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE isPendingDelete = 1")
    suspend fun getPendingDelete(): List<ServiceEntity>

    @Query("UPDATE services SET isPendingCreate = 0, isPendingUpdate = 0, isPendingDelete = 0 WHERE id = :id")
    suspend fun clearPendingFlags(id: String)
}
