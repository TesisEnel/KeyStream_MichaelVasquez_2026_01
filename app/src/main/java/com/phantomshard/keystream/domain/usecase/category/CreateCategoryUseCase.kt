package com.phantomshard.keystream.domain.usecase.category

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.CategoryRepository

class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, description: String?): Resource<Unit> =
        repository.createCategory(name, description)
}
