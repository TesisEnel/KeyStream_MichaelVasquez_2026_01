package com.phantomshard.keystream.domain.usecase.category

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> =
        repository.deleteCategory(id)
}
