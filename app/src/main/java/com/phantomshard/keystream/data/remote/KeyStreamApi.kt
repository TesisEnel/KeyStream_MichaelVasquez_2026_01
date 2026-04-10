package com.phantomshard.keystream.data.remote

import com.phantomshard.keystream.data.remote.dto.CategoriesResponseDto
import com.phantomshard.keystream.data.remote.dto.CreateCategoryRequest
import com.phantomshard.keystream.data.remote.dto.CreateServiceRequest
import com.phantomshard.keystream.data.remote.dto.DeleteCategoryRequest
import com.phantomshard.keystream.data.remote.dto.DeleteServiceRequest
import com.phantomshard.keystream.data.remote.dto.ServicesResponseDto
import com.phantomshard.keystream.data.remote.dto.UpdateCategoryRequest
import com.phantomshard.keystream.data.remote.dto.UpdateServiceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface KeyStreamApi {

    @GET("categories")
    suspend fun getCategories(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<CategoriesResponseDto>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Unit>

    @PATCH("categories")
    suspend fun updateCategory(@Body request: UpdateCategoryRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "categories", hasBody = true)
    suspend fun deleteCategory(@Body request: DeleteCategoryRequest): Response<Unit>

    @GET("services")
    suspend fun getServices(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): Response<ServicesResponseDto>

    @POST("services")
    suspend fun createService(@Body request: CreateServiceRequest): Response<Unit>

    @PATCH("services")
    suspend fun updateService(@Body request: UpdateServiceRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "services", hasBody = true)
    suspend fun deleteService(@Body request: DeleteServiceRequest): Response<Unit>
}
