package com.phantomshard.keystream.domain.usecase.service

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.domain.repository.ServiceRepository

class GetServicesUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        categoryId: String? = null
    ): Resource<List<Service>> = repository.getServices(page, limit, search, categoryId)
}
