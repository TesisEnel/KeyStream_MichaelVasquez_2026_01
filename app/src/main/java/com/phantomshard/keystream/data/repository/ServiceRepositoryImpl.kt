package com.phantomshard.keystream.data.repository

import com.phantomshard.keystream.data.common.ApiErrorMapper
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.dao.ServiceDao
import com.phantomshard.keystream.data.local.entity.ServiceEntity
import com.phantomshard.keystream.data.local.entity.toEntity
import com.phantomshard.keystream.data.remote.KeyStreamApi
import com.phantomshard.keystream.data.remote.dto.CreateServiceRequest
import com.phantomshard.keystream.data.remote.dto.DeleteServiceRequest
import com.phantomshard.keystream.data.remote.dto.UpdateServiceRequest
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.domain.repository.ServiceRepository
import java.util.UUID

class ServiceRepositoryImpl(
    private val api: KeyStreamApi,
    private val serviceDao: ServiceDao
) : ServiceRepository {

    private companion object {
        const val DUPLICATE_SERVICE_MESSAGE = "Ya existe un servicio con ese nombre."
    }

    override suspend fun getServices(
        page: Int,
        limit: Int,
        search: String?,
        categoryId: String?
    ): Resource<List<Service>> {
        return try {
            val response = api.getServices(page, limit, search, categoryId)
            if (response.isSuccessful && response.body() != null) {
                val domains = response.body()!!.data.services.map { it.toDomain() }
                val pending = serviceDao.getPendingCreate() +
                    serviceDao.getPendingUpdate() +
                    serviceDao.getPendingDelete()
                serviceDao.deleteAll()
                serviceDao.upsertAll(domains.map { it.toEntity() })
                val freshIds = domains.map { it.id }.toSet()
                pending.filter { it.id !in freshIds }.forEach { serviceDao.upsert(it) }
                Resource.Success(domains)
            } else {
                val cached = serviceDao.getAll(search, categoryId)
                if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
                else Resource.Error(ApiErrorMapper.toUserMessage(response))
            }
        } catch (e: Exception) {
            val cached = serviceDao.getAll(search, categoryId)
            if (cached.isNotEmpty()) Resource.Success(cached.map { it.toDomain() })
            else Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    override suspend fun createService(
        name: String,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit> {
        return try {
            val response = api.createService(
                CreateServiceRequest(name, categoryId, description, imageUrl, price, stock, maxProfiles)
            )
            if (response.isSuccessful) Resource.Success(Unit)
            else if (response.code() == 409) Resource.Error(DUPLICATE_SERVICE_MESSAGE)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val tempId = "temp_${UUID.randomUUID()}"
            serviceDao.upsert(
                ServiceEntity(
                    id = tempId,
                    name = name,
                    description = description,
                    imageUrl = imageUrl,
                    price = price ?: 0.0,
                    stock = stock ?: 0,
                    maxProfiles = maxProfiles ?: 0,
                    categoryId = categoryId,
                    categoryName = null,
                    isPendingCreate = true
                )
            )
            Resource.Success(Unit)
        }
    }

    override suspend fun updateService(
        id: String,
        name: String?,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit> {
        return try {
            val response = api.updateService(
                UpdateServiceRequest(id, name, categoryId, description, imageUrl, price, stock, maxProfiles)
            )
            if (response.isSuccessful) Resource.Success(Unit)
            else if (response.code() == 409) Resource.Error(DUPLICATE_SERVICE_MESSAGE)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val existing = serviceDao.getById(id)
            if (existing != null) {
                serviceDao.upsert(
                    existing.copy(
                        name = name ?: existing.name,
                        categoryId = categoryId ?: existing.categoryId,
                        description = description ?: existing.description,
                        imageUrl = imageUrl ?: existing.imageUrl,
                        price = price ?: existing.price,
                        stock = stock ?: existing.stock,
                        maxProfiles = maxProfiles ?: existing.maxProfiles,
                        isPendingUpdate = true
                    )
                )
                Resource.Success(Unit)
            } else {
                Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }

    override suspend fun deleteService(id: String): Resource<Unit> {
        return try {
            val response = api.deleteService(DeleteServiceRequest(id))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(ApiErrorMapper.toUserMessage(response))
        } catch (e: Exception) {
            val existing = serviceDao.getById(id)
            if (existing != null) {
                if (existing.isPendingCreate) {
                    serviceDao.deleteById(id)
                } else {
                    serviceDao.upsert(existing.copy(isPendingDelete = true))
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }

    override suspend fun syncPending(): Resource<Unit> {
        return try {
            for (entity in serviceDao.getPendingCreate()) {
                val response = api.createService(
                    CreateServiceRequest(
                        entity.name, entity.categoryId, entity.description,
                        entity.imageUrl, entity.price, entity.stock, entity.maxProfiles
                    )
                )
                if (response.isSuccessful) {
                    serviceDao.deleteById(entity.id)
                }
            }
            for (entity in serviceDao.getPendingUpdate()) {
                val response = api.updateService(
                    UpdateServiceRequest(
                        entity.id, entity.name, entity.categoryId, entity.description,
                        entity.imageUrl, entity.price, entity.stock, entity.maxProfiles
                    )
                )
                if (response.isSuccessful) {
                    serviceDao.clearPendingFlags(entity.id)
                }
            }
            for (entity in serviceDao.getPendingDelete()) {
                val response = api.deleteService(DeleteServiceRequest(entity.id))
                if (response.isSuccessful) {
                    serviceDao.deleteById(entity.id)
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de sincronización")
        }
    }
}
