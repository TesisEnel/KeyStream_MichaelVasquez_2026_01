package com.phantomshard.keystream

import com.phantomshard.keystream.data.local.dao.CategoryDao
import com.phantomshard.keystream.data.local.entity.CategoryEntity

class FakeCategoryDao : CategoryDao {

    val stored = mutableListOf<CategoryEntity>()

    override suspend fun upsertAll(categories: List<CategoryEntity>) {
        categories.forEach { new ->
            stored.removeAll { it.id == new.id }
            stored.add(new)
        }
    }

    override suspend fun upsert(category: CategoryEntity) {
        stored.removeAll { it.id == category.id }
        stored.add(category)
    }

    override suspend fun deleteAll() {
        stored.clear()
    }

    override suspend fun deleteById(id: String) {
        stored.removeAll { it.id == id }
    }

    override suspend fun getById(id: String): CategoryEntity? =
        stored.find { it.id == id }

    override suspend fun getAll(search: String?): List<CategoryEntity> {
        return stored.filter { entity ->
            !entity.isPendingDelete &&
                (search.isNullOrBlank() || entity.name.contains(search, ignoreCase = true))
        }
    }

    override suspend fun getPendingCreate(): List<CategoryEntity> =
        stored.filter { it.isPendingCreate }

    override suspend fun getPendingUpdate(): List<CategoryEntity> =
        stored.filter { it.isPendingUpdate }

    override suspend fun getPendingDelete(): List<CategoryEntity> =
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
