package com.phantomshard.keystream.domain.usecase.category

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.CategoryRepository

class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String, name: String?, description: String?): Resource<Unit> =
        repository.updateCategory(id, name, description)
}
