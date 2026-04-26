package com.phantomshard.keystream.domain.usecase.service

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.ServiceRepository

class UpdateServiceUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(
        id: String,
        name: String?,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit> = repository.updateService(id, name, categoryId, description, imageUrl, price, stock, maxProfiles)
}
