package com.phantomshard.keystream.domain.usecase.service

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.ServiceRepository

class DeleteServiceUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> =
        repository.deleteService(id)
}
