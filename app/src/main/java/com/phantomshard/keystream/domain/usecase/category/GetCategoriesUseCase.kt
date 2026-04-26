package com.phantomshard.keystream.domain.usecase.category

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.repository.CategoryRepository

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Resource<List<Category>> = repository.getCategories(page, limit, search)
}
