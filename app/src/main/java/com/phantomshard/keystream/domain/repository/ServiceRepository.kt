package com.phantomshard.keystream.domain.repository

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Service

interface ServiceRepository {
    suspend fun getServices(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        categoryId: String? = null
    ): Resource<List<Service>>

    suspend fun createService(
        name: String,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit>

    suspend fun updateService(
        id: String,
        name: String?,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit>

    suspend fun deleteService(id: String): Resource<Unit>

    suspend fun syncPending(): Resource<Unit>
}
