package com.phantomshard.keystream.data.remote

import com.phantomshard.keystream.data.remote.dto.CategoriesDataDto
import com.phantomshard.keystream.data.remote.dto.CategoriesResponseDto
import com.phantomshard.keystream.data.remote.dto.CategoryDto
import com.phantomshard.keystream.data.remote.dto.CreateCategoryRequest
import com.phantomshard.keystream.data.remote.dto.CreateServiceRequest
import com.phantomshard.keystream.data.remote.dto.DeleteCategoryRequest
import com.phantomshard.keystream.data.remote.dto.DeleteServiceRequest
import com.phantomshard.keystream.data.remote.dto.ServicesDataDto
import com.phantomshard.keystream.data.remote.dto.ServicesResponseDto
import com.phantomshard.keystream.data.remote.dto.ServiceDto
import com.phantomshard.keystream.data.remote.dto.UpdateCategoryRequest
import com.phantomshard.keystream.data.remote.dto.UpdateServiceRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class FakeKeyStreamApi : KeyStreamApi {
    var categoriesResponse: Response<CategoriesResponseDto> = Response.success(
        CategoriesResponseDto(data = CategoriesDataDto(categories = emptyList()))
    )
    var createCategoryResponse: Response<Unit> = Response.success(Unit)
    var updateCategoryResponse: Response<Unit> = Response.success(Unit)
    var deleteCategoryResponse: Response<Unit> = Response.success(Unit)

    var shouldThrowOnGetCategories = false
    var shouldThrowOnCreateCategory = false
    var shouldThrowOnUpdateCategory = false
    var shouldThrowOnDeleteCategory = false

    var lastCreateCategoryRequest: CreateCategoryRequest? = null
    var lastUpdateCategoryRequest: UpdateCategoryRequest? = null
    var lastDeleteCategoryId: String? = null

    var servicesResponse: Response<ServicesResponseDto> = Response.success(
        ServicesResponseDto(data = ServicesDataDto(services = emptyList()))
    )
    var createServiceResponse: Response<Unit> = Response.success(Unit)
    var updateServiceResponse: Response<Unit> = Response.success(Unit)
    var deleteServiceResponse: Response<Unit> = Response.success(Unit)

    var shouldThrowOnGetServices = false
    var shouldThrowOnCreateService = false
    var shouldThrowOnUpdateService = false
    var shouldThrowOnDeleteService = false

    var lastCreateServiceRequest: CreateServiceRequest? = null
    var lastUpdateServiceRequest: UpdateServiceRequest? = null
    var lastDeleteServiceId: String? = null

    override suspend fun getCategories(page: Int, limit: Int, search: String?): Response<CategoriesResponseDto> {
        if (shouldThrowOnGetCategories) throw RuntimeException("Network error")
        return categoriesResponse
    }

    override suspend fun createCategory(request: CreateCategoryRequest): Response<Unit> {
        if (shouldThrowOnCreateCategory) throw RuntimeException("Network error")
        lastCreateCategoryRequest = request
        return createCategoryResponse
    }

    override suspend fun updateCategory(request: UpdateCategoryRequest): Response<Unit> {
        if (shouldThrowOnUpdateCategory) throw RuntimeException("Network error")
        lastUpdateCategoryRequest = request
        return updateCategoryResponse
    }

    override suspend fun deleteCategory(request: DeleteCategoryRequest): Response<Unit> {
        if (shouldThrowOnDeleteCategory) throw RuntimeException("Network error")
        lastDeleteCategoryId = request.id
        return deleteCategoryResponse
    }

    override suspend fun getServices(page: Int, limit: Int, search: String?, categoryId: String?): Response<ServicesResponseDto> {
        if (shouldThrowOnGetServices) throw RuntimeException("Network error")
        return servicesResponse
    }

    override suspend fun createService(request: CreateServiceRequest): Response<Unit> {
        if (shouldThrowOnCreateService) throw RuntimeException("Network error")
        lastCreateServiceRequest = request
        return createServiceResponse
    }

    override suspend fun updateService(request: UpdateServiceRequest): Response<Unit> {
        if (shouldThrowOnUpdateService) throw RuntimeException("Network error")
        lastUpdateServiceRequest = request
        return updateServiceResponse
    }

    override suspend fun deleteService(request: DeleteServiceRequest): Response<Unit> {
        if (shouldThrowOnDeleteService) throw RuntimeException("Network error")
        lastDeleteServiceId = request.id
        return deleteServiceResponse
    }
}
