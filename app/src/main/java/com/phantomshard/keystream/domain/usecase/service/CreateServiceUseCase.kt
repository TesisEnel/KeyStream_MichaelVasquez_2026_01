package com.phantomshard.keystream.domain.usecase.service

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.ServiceRepository

class CreateServiceUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(
        name: String,
        categoryId: String?,
        description: String?,
        imageUrl: String?,
        price: Double?,
        stock: Int?,
        maxProfiles: Int?
    ): Resource<Unit> = repository.createService(name, categoryId, description, imageUrl, price, stock, maxProfiles)
}
