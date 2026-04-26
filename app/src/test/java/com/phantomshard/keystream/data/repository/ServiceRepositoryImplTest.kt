package com.phantomshard.keystream.data.repository

import com.phantomshard.keystream.FakeServiceDao
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.entity.ServiceEntity
import com.phantomshard.keystream.data.remote.FakeKeyStreamApi
import com.phantomshard.keystream.data.remote.dto.CategoryRefDto
import com.phantomshard.keystream.data.remote.dto.ServiceDto
import com.phantomshard.keystream.data.remote.dto.ServicesDataDto
import com.phantomshard.keystream.data.remote.dto.ServicesResponseDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ServiceRepositoryImplTest {

    private lateinit var api: FakeKeyStreamApi
    private lateinit var dao: FakeServiceDao
    private lateinit var repository: ServiceRepositoryImpl

    @Before
    fun setUp() {
        api = FakeKeyStreamApi()
        dao = FakeServiceDao()
        repository = ServiceRepositoryImpl(api, dao)
    }



    @Test
    fun `getServices returns Success with mapped domain models on 200`() = runTest {
        api.servicesResponse = Response.success(
            ServicesResponseDto(
                data = ServicesDataDto(
                    services = listOf(
                        ServiceDto(
                            id = "s1", name = "Netflix", description = "Streaming",
                            imageUrl = "https://example.com/netflix.png",
                            price = 9.99, stock = 10, maxProfiles = 4,
                            category = CategoryRefDto(id = "c1", name = "Streaming")
                        ),
                        ServiceDto(
                            id = "s2", name = "Spotify", description = null,
                            imageUrl = null, price = 4.99, stock = 5, maxProfiles = 1,
                            category = null
                        )
                    )
                )
            )
        )

        val result = repository.getServices()

        assertTrue(result is Resource.Success)
        val services = (result as Resource.Success).data!!
        assertEquals(2, services.size)
        assertEquals("s1", services[0].id)
        assertEquals("Netflix", services[0].name)
        assertEquals(9.99, services[0].price, 0.001)
        assertEquals("c1", services[0].category?.id)
        assertNull(services[1].category)
        assertNull(services[1].description)
    }

    @Test
    fun `getServices persists results to Room on API success`() = runTest {
        api.servicesResponse = Response.success(
            ServicesResponseDto(
                data = ServicesDataDto(
                    services = listOf(ServiceDto(id = "s1", name = "Netflix"))
                )
            )
        )

        repository.getServices()

        assertEquals(1, dao.stored.size)
        assertEquals("s1", dao.stored[0].id)
    }

    @Test
    fun `getServices replaces Room cache on each successful fetch`() = runTest {
        dao.stored.add(ServiceEntity("old", "Old Service", null, null, 0.0, 0, 1, null, null))

        api.servicesResponse = Response.success(
            ServicesResponseDto(
                data = ServicesDataDto(
                    services = listOf(ServiceDto(id = "new", name = "New Service"))
                )
            )
        )

        repository.getServices()

        assertEquals(1, dao.stored.size)
        assertEquals("new", dao.stored[0].id)
    }

    @Test
    fun `getServices returns Success with empty list`() = runTest {
        api.servicesResponse = Response.success(
            ServicesResponseDto(data = ServicesDataDto(services = emptyList()))
        )

        val result = repository.getServices()

        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data!!.isEmpty())
    }



    @Test
    fun `getServices returns cached data when network fails`() = runTest {
        dao.stored.add(ServiceEntity("s1", "Netflix", null, null, 9.99, 10, 4, null, null))
        api.shouldThrowOnGetServices = true

        val result = repository.getServices()

        assertTrue(result is Resource.Success)
        val services = (result as Resource.Success).data!!
        assertEquals(1, services.size)
        assertEquals("Netflix", services[0].name)
    }

    @Test
    fun `getServices returns cached data when API returns error`() = runTest {
        dao.stored.add(ServiceEntity("s1", "Spotify", null, null, 4.99, 5, 1, null, null))
        api.servicesResponse = Response.error(
            503, "Service Unavailable".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.getServices()

        assertTrue(result is Resource.Success)
        assertEquals("Spotify", (result as Resource.Success).data!![0].name)
    }

    @Test
    fun `getServices returns Error when network fails and cache is empty`() = runTest {
        api.shouldThrowOnGetServices = true

        val result = repository.getServices()

        assertTrue(result is Resource.Error)
        assertNotNull((result as Resource.Error).message)
    }

    @Test
    fun `getServices filters cached results by categoryId when offline`() = runTest {
        dao.stored.add(ServiceEntity("s1", "Netflix", null, null, 9.99, 10, 4, "c1", "Streaming"))
        dao.stored.add(ServiceEntity("s2", "Spotify", null, null, 4.99, 5, 1, "c2", "Music"))
        api.shouldThrowOnGetServices = true

        val result = repository.getServices(categoryId = "c1")

        assertTrue(result is Resource.Success)
        val services = (result as Resource.Success).data!!
        assertEquals(1, services.size)
        assertEquals("Netflix", services[0].name)
    }



    @Test
    fun `createService sends all fields in JSON body`() = runTest {
        api.createServiceResponse = Response.success(Unit)

        val result = repository.createService(
            name = "Disney+",
            categoryId = "c1",
            description = "Streaming Disney",
            imageUrl = "https://example.com/disney.png",
            price = 7.99,
            stock = 20,
            maxProfiles = 4
        )

        assertTrue(result is Resource.Success)
        assertEquals("Disney+", api.lastCreateServiceRequest?.name)
        assertEquals("c1", api.lastCreateServiceRequest?.categoryId)
        assertEquals("https://example.com/disney.png", api.lastCreateServiceRequest?.imageUrl)
        assertEquals(7.99, api.lastCreateServiceRequest?.price)
        assertEquals(20, api.lastCreateServiceRequest?.stock)
        assertEquals(4, api.lastCreateServiceRequest?.maxProfiles)
    }

    @Test
    fun `createService succeeds with nullable fields`() = runTest {
        api.createServiceResponse = Response.success(Unit)

        val result = repository.createService(
            name = "Nuevo",
            categoryId = null,
            description = null,
            imageUrl = null,
            price = null,
            stock = null,
            maxProfiles = null
        )

        assertTrue(result is Resource.Success)
        assertNull(api.lastCreateServiceRequest?.categoryId)
        assertNull(api.lastCreateServiceRequest?.imageUrl)
    }

    @Test
    fun `createService returns specific message on 409 conflict`() = runTest {
        api.createServiceResponse = Response.error(
            409, "Conflict".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.createService("Duplicado", null, null, null, null, null, null)

        assertTrue(result is Resource.Error)
        assertEquals("Ya existe un servicio con ese nombre.", (result as Resource.Error).message)
    }

    @Test
    fun `createService returns Error on non-200 response`() = runTest {
        api.createServiceResponse = Response.error(
            400, "Bad Request".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.createService("Nuevo", null, null, null, null, null, null)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("400"))
    }

    @Test
    fun `createService offline saves service locally with isPendingCreate and returns Success`() = runTest {
        api.shouldThrowOnCreateService = true

        val result = repository.createService("Nuevo", "c1", "Desc", null, 5.0, 10, 2)

        assertTrue(result is Resource.Success)
        assertEquals(1, dao.stored.size)
        assertTrue(dao.stored[0].isPendingCreate)
        assertTrue(dao.stored[0].id.startsWith("temp_"))
        assertEquals("Nuevo", dao.stored[0].name)
    }



    @Test
    fun `updateService sends correct id and updated fields`() = runTest {
        api.updateServiceResponse = Response.success(Unit)

        val result = repository.updateService(
            id = "s1",
            name = "Netflix Pro",
            categoryId = "c2",
            description = "Updated",
            imageUrl = "https://new.png",
            price = 12.99,
            stock = 15,
            maxProfiles = 5
        )

        assertTrue(result is Resource.Success)
        assertEquals("s1", api.lastUpdateServiceRequest?.id)
        assertEquals("Netflix Pro", api.lastUpdateServiceRequest?.name)
        assertEquals("c2", api.lastUpdateServiceRequest?.categoryId)
        assertEquals(12.99, api.lastUpdateServiceRequest?.price)
    }

    @Test
    fun `updateService returns Error on non-200 response`() = runTest {
        api.updateServiceResponse = Response.error(
            404, "Not Found".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.updateService("s999", null, null, null, null, null, null, null)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("404"))
    }

    @Test
    fun `updateService returns Error on network exception`() = runTest {
        api.shouldThrowOnUpdateService = true

        val result = repository.updateService("s1", null, null, null, null, null, null, null)

        assertTrue(result is Resource.Error)
    }



    @Test
    fun `deleteService sends id in request body and returns Success`() = runTest {
        api.deleteServiceResponse = Response.success(Unit)

        val result = repository.deleteService(id = "s1")

        assertTrue(result is Resource.Success)
        assertEquals("s1", api.lastDeleteServiceId)
    }

    @Test
    fun `deleteService returns Error on non-200 response`() = runTest {
        api.deleteServiceResponse = Response.error(
            404, "Not Found".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.deleteService(id = "s999")

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message!!.contains("404"))
    }

    @Test
    fun `deleteService returns Error on network exception when entity not cached`() = runTest {
        api.shouldThrowOnDeleteService = true

        val result = repository.deleteService(id = "s1")

        assertTrue(result is Resource.Error)
    }



    @Test
    fun `updateService offline marks existing entity as isPendingUpdate and returns Success`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "s1", "Netflix", null, null, 9.99, 10, 4, "c1", "Streaming"
            )
        )
        api.shouldThrowOnUpdateService = true

        val result = repository.updateService("s1", "Netflix Pro", null, null, null, null, null, null)

        assertTrue(result is Resource.Success)
        val updated = dao.stored.first { it.id == "s1" }
        assertTrue(updated.isPendingUpdate)
        assertEquals("Netflix Pro", updated.name)
    }

    @Test
    fun `updateService offline returns Error when entity is not cached`() = runTest {
        api.shouldThrowOnUpdateService = true

        val result = repository.updateService("ghost", "X", null, null, null, null, null, null)

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `deleteService offline marks entity as isPendingDelete and returns Success`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "s1", "Netflix", null, null, 9.99, 10, 4, null, null
            )
        )
        api.shouldThrowOnDeleteService = true

        val result = repository.deleteService(id = "s1")

        assertTrue(result is Resource.Success)
        val entity = dao.stored.first { it.id == "s1" }
        assertTrue(entity.isPendingDelete)
    }

    @Test
    fun `deleteService offline removes temp entry if isPendingCreate`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "temp_abc", "Temp", null, null, 0.0, 0, 0, null, null, isPendingCreate = true
            )
        )
        api.shouldThrowOnDeleteService = true

        val result = repository.deleteService(id = "temp_abc")

        assertTrue(result is Resource.Success)
        assertTrue(dao.stored.isEmpty())
    }

    @Test
    fun `syncPending processes pending creates and clears temp entries`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "temp_1", "Offline Service", null, null, 5.0, 3, 1, null, null, isPendingCreate = true
            )
        )
        api.createServiceResponse = Response.success(Unit)

        repository.syncPending()

        assertTrue(dao.stored.isEmpty())
    }

    @Test
    fun `syncPending processes pending updates and clears flags`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "s1", "Updated", null, null, 5.0, 3, 1, null, null, isPendingUpdate = true
            )
        )
        api.updateServiceResponse = Response.success(Unit)

        repository.syncPending()

        val entity = dao.stored.first()
        assertEquals(false, entity.isPendingUpdate)
    }

    @Test
    fun `syncPending processes pending deletes and removes entries`() = runTest {
        dao.stored.add(
            ServiceEntity(
                "s1", "To Delete", null, null, 5.0, 3, 1, null, null, isPendingDelete = true
            )
        )
        api.deleteServiceResponse = Response.success(Unit)

        repository.syncPending()

        assertTrue(dao.stored.isEmpty())
    }
}
