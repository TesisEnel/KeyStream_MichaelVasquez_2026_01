package com.phantomshard.keystream.data.repository

import com.phantomshard.keystream.data.common.ApiErrorMapper
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.dao.CategoryDao
import com.phantomshard.keystream.data.local.entity.CategoryEntity
import com.phantomshard.keystream.data.local.entity.toEntity
import com.phantomshard.keystream.data.remote.KeyStreamApi
import com.phantomshard.keystream.data.remote.dto.CreateCategoryRequest
import com.phantomshard.keystream.data.remote.dto.DeleteCategoryRequest
import com.phantomshard.keystream.data.remote.dto.UpdateCategoryRequest
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.repository.CategoryRepository
import java.util.UUID

class CategoryRepositoryImpl(
    private val api: KeyStreamApi,
    private val categoryDao: CategoryDao
) : CategoryRepository {

    private companion object {
        const val DUPLICATE_CATEGORY_MESSAGE = "Ya existe una categoría con ese nombre."
    }

    override suspend fun getCategories(
        page: Int,
        limit: Int,
        search: String?
    ): Resource<List<Category>> {
        return try {
            val response = api.getCategories(page, limit, search)
            if (response.isSuccessful && response.body() != null) {
                val domains = response.body()!!.data.categories.map { it.toDomain() }
                val pending = categoryDao.getPendingCreate() +
                    categoryDao.getPendingUpdate() +
                    categoryDao.getPendingDelete()
                val pendingIds = pending.map { it.id }.toSet()
                categoryDao.deleteAll()
                categoryDao.upsertAll(domains.map { it.toEntity() })
                val freshIds = domains.map { it.id }.toSet()
                pending.filter { it.id !in freshIds }.forEach { categoryDao.upsert(it) }
                Resource.Success(domains)
            } else {
                val cached = categoryDao.getAll(search)
                if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
                else Resource.Error(ApiErrorMapper.toUserMessage(response))
            }
        } catch (e: Exception) {
            val cached = categoryDao.getAll(search)
            if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
            else Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    override suspend fun createCategory(name: String, description: String?): Resource<Unit> {
        return try {
            val response = api.createCategory(CreateCategoryRequest(name, description))
            if (response.isSuccessful) Resource.Success(Unit)
            else if (response.code() == 409) Resource.Error(DUPLICATE_CATEGORY_MESSAGE)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val tempId = "temp_${UUID.randomUUID()}"
            categoryDao.upsert(
                CategoryEntity(
                    id = tempId,
                    name = name,
                    description = description,
                    isPendingCreate = true
                )
            )
            Resource.Success(Unit)
        }
    }

    override suspend fun updateCategory(
        id: String,
        name: String?,
        description: String?
    ): Resource<Unit> {
        return try {
            val response = api.updateCategory(UpdateCategoryRequest(id, name, description))
            if (response.isSuccessful) Resource.Success(Unit)
            else if (response.code() == 409) Resource.Error(DUPLICATE_CATEGORY_MESSAGE)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val existing = categoryDao.getById(id)
            if (existing != null) {
                categoryDao.upsert(
                    existing.copy(
                        name = name ?: existing.name,
                        description = description ?: existing.description,
                        isPendingUpdate = true
                    )
                )
                Resource.Success(Unit)
            } else {
                Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }

    override suspend fun deleteCategory(id: String): Resource<Unit> {
        return try {
            val response = api.deleteCategory(DeleteCategoryRequest(id))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val existing = categoryDao.getById(id)
            if (existing != null) {
                if (existing.isPendingCreate) {
                    categoryDao.deleteById(id)
                } else {
                    categoryDao.upsert(existing.copy(isPendingDelete = true))
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }

    override suspend fun syncPending(): Resource<Unit> {
        return try {
            for (entity in categoryDao.getPendingCreate()) {
                val response = api.createCategory(
                    CreateCategoryRequest(entity.name, entity.description)
                )
                if (response.isSuccessful) {
                    categoryDao.deleteById(entity.id) 
                }
            }
            for (entity in categoryDao.getPendingUpdate()) {
                val response = api.updateCategory(
                    UpdateCategoryRequest(entity.id, entity.name, entity.description)
                )
                if (response.isSuccessful) {
                    categoryDao.clearPendingFlags(entity.id)
                }
            }
            for (entity in categoryDao.getPendingDelete()) {
                val response = api.deleteCategory(DeleteCategoryRequest(entity.id))
                if (response.isSuccessful) {
                    categoryDao.deleteById(entity.id)
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de sincronización")
        }
    }
}
