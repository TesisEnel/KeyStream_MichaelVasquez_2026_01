package com.phantomshard.keystream.data.repository

import com.phantomshard.keystream.FakeCategoryDao
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.entity.CategoryEntity
import com.phantomshard.keystream.data.remote.FakeKeyStreamApi
import com.phantomshard.keystream.data.remote.dto.CategoriesDataDto
import com.phantomshard.keystream.data.remote.dto.CategoriesResponseDto
import com.phantomshard.keystream.data.remote.dto.CategoryDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CategoryRepositoryImplTest {

    private lateinit var api: FakeKeyStreamApi
    private lateinit var dao: FakeCategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        api = FakeKeyStreamApi()
        dao = FakeCategoryDao()
        repository = CategoryRepositoryImpl(api, dao)
    }



    @Test
    fun `getCategories returns Success with mapped domain models on 200`() = runTest {
        api.categoriesResponse = Response.success(
            CategoriesResponseDto(
                data = CategoriesDataDto(
                    categories = listOf(
                        CategoryDto(id = "1", name = "Streaming", description = "Plataformas de streaming"),
                        CategoryDto(id = "2", name = "Gaming", description = null)
                    )
                )
            )
        )

        val result = repository.getCategories()

        assertTrue(result is Resource.Success)
        val categories = (result as Resource.Success).data!!
        assertEquals(2, categories.size)
        assertEquals("1", categories[0].id)
        assertEquals("Streaming", categories[0].name)
        assertEquals("Plataformas de streaming", categories[0].description)
        assertEquals("2", categories[1].id)
        assertEquals(null, categories[1].description)
    }

    @Test
    fun `getCategories persists results to Room on API success`() = runTest {
        api.categoriesResponse = Response.success(
            CategoriesResponseDto(
                data = CategoriesDataDto(
                    categories = listOf(CategoryDto(id = "1", name = "Streaming"))
                )
            )
        )

        repository.getCategories()

        assertEquals(1, dao.stored.size)
        assertEquals("1", dao.stored[0].id)
        assertEquals("Streaming", dao.stored[0].name)
    }

    @Test
    fun `getCategories replaces Room cache on each successful fetch`() = runTest {
        dao.stored.add(CategoryEntity("old", "Old Category", null))

        api.categoriesResponse = Response.success(
            CategoriesResponseDto(
                data = CategoriesDataDto(
                    categories = listOf(CategoryDto(id = "new", name = "New Category"))
                )
            )
        )

        repository.getCategories()

        assertEquals(1, dao.stored.size)
        assertEquals("new", dao.stored[0].id)
    }

    @Test
    fun `getCategories returns Success with empty list when API returns no categories`() = runTest {
        api.categoriesResponse = Response.success(
            CategoriesResponseDto(data = CategoriesDataDto(categories = emptyList()))
        )

        val result = repository.getCategories()

        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data!!.isEmpty())
    }



    @Test
    fun `getCategories returns cached data when network fails`() = runTest {
        dao.stored.add(CategoryEntity("c1", "Streaming", "Cached"))
        api.shouldThrowOnGetCategories = true

        val result = repository.getCategories()

        assertTrue(result is Resource.Success)
        val categories = (result as Resource.Success).data!!
        assertEquals(1, categories.size)
        assertEquals("c1", categories[0].id)
        assertEquals("Streaming", categories[0].name)
    }

    @Test
    fun `getCategories returns cached data when API returns error`() = runTest {
        dao.stored.add(CategoryEntity("c1", "Gaming", null))
        api.categoriesResponse = Response.error(
            503, "Service Unavailable".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.getCategories()

        assertTrue(result is Resource.Success)
        assertEquals("Gaming", (result as Resource.Success).data!![0].name)
    }

    @Test
    fun `getCategories returns Error when network fails and cache is empty`() = runTest {
        api.shouldThrowOnGetCategories = true

        val result = repository.getCategories()

        assertTrue(result is Resource.Error)
        assertNotNull((result as Resource.Error).message)
    }

    @Test
    fun `getCategories returns Error when API errors and cache is empty`() = runTest {
        api.categoriesResponse = Response.error(
            401, "Unauthorized".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.getCategories()

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("401"))
    }



    @Test
    fun `createCategory sends correct JSON body and returns Success`() = runTest {
        api.createCategoryResponse = Response.success(Unit)

        val result = repository.createCategory(name = "Streaming", description = "Desc")

        assertTrue(result is Resource.Success)
        assertEquals("Streaming", api.lastCreateCategoryRequest?.name)
        assertEquals("Desc", api.lastCreateCategoryRequest?.description)
    }

    @Test
    fun `createCategory returns Success with null description`() = runTest {
        api.createCategoryResponse = Response.success(Unit)

        val result = repository.createCategory(name = "Gaming", description = null)

        assertTrue(result is Resource.Success)
        assertEquals(null, api.lastCreateCategoryRequest?.description)
    }

    @Test
    fun `createCategory returns specific message on 409 conflict`() = runTest {
        api.createCategoryResponse = Response.error(
            409, "Conflict".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.createCategory(name = "Duplicado", description = null)

        assertTrue(result is Resource.Error)
        assertEquals("Ya existe una categoría con ese nombre.", (result as Resource.Error).message)
    }

    @Test
    fun `createCategory returns Error on non-200 response`() = runTest {
        api.createCategoryResponse = Response.error(
            400, "Bad Request".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.createCategory(name = "Nueva", description = null)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("400"))
    }

    @Test
    fun `createCategory offline saves category locally with isPendingCreate and returns Success`() = runTest {
        api.shouldThrowOnCreateCategory = true

        val result = repository.createCategory(name = "Nueva", description = "Desc")

        assertTrue(result is Resource.Success)
        assertEquals(1, dao.stored.size)
        assertTrue(dao.stored[0].isPendingCreate)
        assertTrue(dao.stored[0].id.startsWith("temp_"))
        assertEquals("Nueva", dao.stored[0].name)
        assertEquals("Desc", dao.stored[0].description)
    }



    @Test
    fun `updateCategory sends correct id and fields`() = runTest {
        api.updateCategoryResponse = Response.success(Unit)

        val result = repository.updateCategory(id = "1", name = "Actualizado", description = "Nueva desc")

        assertTrue(result is Resource.Success)
        assertEquals("1", api.lastUpdateCategoryRequest?.id)
        assertEquals("Actualizado", api.lastUpdateCategoryRequest?.name)
        assertEquals("Nueva desc", api.lastUpdateCategoryRequest?.description)
    }

    @Test
    fun `updateCategory returns specific message on 409 conflict`() = runTest {
        api.updateCategoryResponse = Response.error(
            409, "Conflict".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.updateCategory(id = "1", name = "Duplicado", description = null)

        assertTrue(result is Resource.Error)
        assertEquals("Ya existe una categoría con ese nombre.", (result as Resource.Error).message)
    }

    @Test
    fun `updateCategory returns Error on non-200 response`() = runTest {
        api.updateCategoryResponse = Response.error(
            404, "Not Found".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.updateCategory(id = "999", name = "X", description = null)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("404"))
    }

    @Test
    fun `updateCategory returns Error on network exception`() = runTest {
        api.shouldThrowOnUpdateCategory = true

        val result = repository.updateCategory(id = "1", name = "X", description = null)

        assertTrue(result is Resource.Error)
    }



    @Test
    fun `deleteCategory sends id in request body and returns Success`() = runTest {
        api.deleteCategoryResponse = Response.success(Unit)

        val result = repository.deleteCategory(id = "42")

        assertTrue(result is Resource.Success)
        assertEquals("42", api.lastDeleteCategoryId)
    }

    @Test
    fun `deleteCategory returns Error on non-200 response`() = runTest {
        api.deleteCategoryResponse = Response.error(
            403, "Forbidden".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.deleteCategory(id = "1")

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("403"))
    }

    @Test
    fun `deleteCategory returns Error on network exception when entity not cached`() = runTest {
        api.shouldThrowOnDeleteCategory = true

        val result = repository.deleteCategory(id = "1")

        assertTrue(result is Resource.Error)
    }



    @Test
    fun `updateCategory offline marks existing entity as isPendingUpdate and returns Success`() = runTest {
        dao.stored.add(CategoryEntity("1", "Streaming", "Desc"))
        api.shouldThrowOnUpdateCategory = true

        val result = repository.updateCategory(id = "1", name = "Streaming Plus", description = "Updated")

        assertTrue(result is Resource.Success)
        val updated = dao.stored.first { it.id == "1" }
        assertTrue(updated.isPendingUpdate)
        assertEquals("Streaming Plus", updated.name)
    }

    @Test
    fun `updateCategory offline returns Error when entity is not cached`() = runTest {
        api.shouldThrowOnUpdateCategory = true

        val result = repository.updateCategory(id = "999", name = "Ghost", description = null)

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `deleteCategory offline marks entity as isPendingDelete and returns Success`() = runTest {
        dao.stored.add(CategoryEntity("1", "Streaming", null))
        api.shouldThrowOnDeleteCategory = true

        val result = repository.deleteCategory(id = "1")

        assertTrue(result is Resource.Success)
        val entity = dao.stored.first { it.id == "1" }
        assertTrue(entity.isPendingDelete)
    }

    @Test
    fun `deleteCategory offline removes temp entry directly if isPendingCreate`() = runTest {
        dao.stored.add(CategoryEntity("temp_abc", "Temp", null, isPendingCreate = true))
        api.shouldThrowOnDeleteCategory = true

        val result = repository.deleteCategory(id = "temp_abc")

        assertTrue(result is Resource.Success)
        assertTrue(dao.stored.isEmpty())
    }

    @Test
    fun `getCategories preserves pending items across cache refresh`() = runTest {
        dao.stored.add(CategoryEntity("temp_xyz", "Offline Cat", null, isPendingCreate = true))
        api.categoriesResponse = Response.success(
            CategoriesResponseDto(
                data = CategoriesDataDto(
                    categories = listOf(CategoryDto(id = "1", name = "Online Cat"))
                )
            )
        )

        repository.getCategories()

        // Both fresh data and the pending item should be present
        assertEquals(2, dao.stored.size)
        assertTrue(dao.stored.any { it.id == "1" })
        assertTrue(dao.stored.any { it.id == "temp_xyz" && it.isPendingCreate })
    }

    @Test
    fun `syncPending processes pending creates and clears temp entries`() = runTest {
        dao.stored.add(CategoryEntity("temp_1", "Offline Cat", null, isPendingCreate = true))
        api.createCategoryResponse = Response.success(Unit)

        repository.syncPending()

        assertTrue(dao.stored.isEmpty()) // temp entry removed after successful sync
    }

    @Test
    fun `syncPending processes pending updates and clears flags`() = runTest {
        dao.stored.add(CategoryEntity("1", "Updated Name", "Desc", isPendingUpdate = true))
        api.updateCategoryResponse = Response.success(Unit)

        repository.syncPending()

        val entity = dao.stored.first()
        assertEquals(false, entity.isPendingUpdate)
    }

    @Test
    fun `syncPending processes pending deletes and removes entries`() = runTest {
        dao.stored.add(CategoryEntity("1", "To Delete", null, isPendingDelete = true))
        api.deleteCategoryResponse = Response.success(Unit)

        repository.syncPending()

        assertTrue(dao.stored.isEmpty())
    }
}
