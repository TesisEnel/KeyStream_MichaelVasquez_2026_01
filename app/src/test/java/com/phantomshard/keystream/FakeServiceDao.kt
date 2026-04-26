package com.phantomshard.keystream

import com.phantomshard.keystream.data.local.dao.ServiceDao
import com.phantomshard.keystream.data.local.entity.ServiceEntity

class FakeServiceDao : ServiceDao {

    val stored = mutableListOf<ServiceEntity>()

    override suspend fun upsertAll(services: List<ServiceEntity>) {
        services.forEach { new ->
            stored.removeAll { it.id == new.id }
            stored.add(new)
        }
    }

    override suspend fun upsert(service: ServiceEntity) {
        stored.removeAll { it.id == service.id }
        stored.add(service)
    }

    override suspend fun deleteAll() {
        stored.clear()
    }

    override suspend fun deleteById(id: String) {
        stored.removeAll { it.id == id }
    }

    override suspend fun getById(id: String): ServiceEntity? =
        stored.find { it.id == id }

    override suspend fun getAll(search: String?, categoryId: String?): List<ServiceEntity> {
        return stored.filter { entity ->
            !entity.isPendingDelete &&
                (search.isNullOrBlank() || entity.name.contains(search, ignoreCase = true)) &&
                (categoryId == null || entity.categoryId == categoryId)
        }
    }

    override suspend fun getPendingCreate(): List<ServiceEntity> =
        stored.filter { it.isPendingCreate }

    override suspend fun getPendingUpdate(): List<ServiceEntity> =
        stored.filter { it.isPendingUpdate }

    override suspend fun getPendingDelete(): List<ServiceEntity> =
        stored.filter { it.isPendingDelete }

    override suspend fun clearPendingFlags(id: String) {
        val idx = stored.indexOfFirst { it.id == id }
        if (idx >= 0) {
            stored[idx] = stored[idx].copy(
                isPendingCreate = false,
                isPendingUpdate = false,
                isPendingDelete = false
            )
        }
    }
}
