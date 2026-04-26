package com.phantomshard.keystream

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.domain.repository.ServiceRepository

class FakeServiceRepository : ServiceRepository {

    var getServicesResult: Resource<List<Service>> = Resource.Success(emptyList())
    var createResult: Resource<Unit> = Resource.Success(Unit)
    var updateResult: Resource<Unit> = Resource.Success(Unit)
    var deleteResult: Resource<Unit> = Resource.Success(Unit)
    var syncPendingResult: Resource<Unit> = Resource.Success(Unit)

    data class CreateArgs(
        val name: String,
        val categoryId: String?,
        val description: String?,
        val imageUrl: String?,
        val price: Double?,
        val stock: Int?,
        val maxProfiles: Int?
    )

    data class UpdateArgs(
        val id: String,
        val name: String?,
        val categoryId: String?,
        val description: String?,
        val imageUrl: String?,
        val price: Double?,
        val stock: Int?,
        val maxProfiles: Int?
    )

    val createCalls = mutableListOf<CreateArgs>()
    val updateCalls = mutableListOf<UpdateArgs>()
    val deletedIds = mutableListOf<String>()
    var getServicesCalls = 0

    override suspend fun getServices(
        page: Int,
        limit: Int,
        search: String?,
        categoryId: String?
    ): Resource<List<Service>> {
        getServicesCalls++
        return getServicesResult
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
        createCalls.add(CreateArgs(name, categoryId, description, imageUrl, price, stock, maxProfiles))
        return createResult
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
        updateCalls.add(UpdateArgs(id, name, categoryId, description, imageUrl, price, stock, maxProfiles))
        return updateResult
    }

    override suspend fun deleteService(id: String): Resource<Unit> {
        deletedIds.add(id)
        return deleteResult
    }

    override suspend fun syncPending(): Resource<Unit> = syncPendingResult
}
