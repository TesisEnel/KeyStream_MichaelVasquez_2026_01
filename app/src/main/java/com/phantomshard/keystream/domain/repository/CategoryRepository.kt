package com.phantomshard.keystream.domain.repository

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null
    ): Resource<List<Category>>

    suspend fun createCategory(name: String, description: String?): Resource<Unit>

    suspend fun updateCategory(id: String, name: String?, description: String?): Resource<Unit>

    suspend fun deleteCategory(id: String): Resource<Unit>

    suspend fun syncPending(): Resource<Unit>
}
