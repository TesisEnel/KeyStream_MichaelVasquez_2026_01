package com.phantomshard.keystream

import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.repository.CategoryRepository

class FakeCategoryRepository : CategoryRepository {

    var getCategoriesResult: Resource<List<Category>> = Resource.Success(emptyList())
    var createResult: Resource<Unit> = Resource.Success(Unit)
    var updateResult: Resource<Unit> = Resource.Success(Unit)
    var deleteResult: Resource<Unit> = Resource.Success(Unit)
    var syncPendingResult: Resource<Unit> = Resource.Success(Unit)

    data class UpdateArgs(val id: String, val name: String?, val description: String?)

    val createdCategories = mutableListOf<Pair<String, String?>>()
    val updatedCategories = mutableListOf<UpdateArgs>()
    val deletedIds = mutableListOf<String>()
    var getCategoriesCalls = 0

    override suspend fun getCategories(page: Int, limit: Int, search: String?): Resource<List<Category>> {
        getCategoriesCalls++
        return getCategoriesResult
    }

    override suspend fun createCategory(name: String, description: String?): Resource<Unit> {
        createdCategories.add(name to description)
        return createResult
    }

    override suspend fun updateCategory(id: String, name: String?, description: String?): Resource<Unit> {
        updatedCategories.add(UpdateArgs(id, name, description))
        return updateResult
    }

    override suspend fun deleteCategory(id: String): Resource<Unit> {
        deletedIds.add(id)
        return deleteResult
    }

    override suspend fun syncPending(): Resource<Unit> = syncPendingResult
}
