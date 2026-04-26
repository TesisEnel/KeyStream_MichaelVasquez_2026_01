package com.phantomshard.keystream.ui.services

import com.phantomshard.keystream.FakeCategoryRepository
import com.phantomshard.keystream.FakeServiceRepository
import com.phantomshard.keystream.FakeTriggerSyncUseCase
import com.phantomshard.keystream.MainCoroutineRule
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.model.CategoryRef
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import com.phantomshard.keystream.domain.usecase.service.CreateServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.DeleteServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.GetServicesUseCase
import com.phantomshard.keystream.domain.usecase.service.UpdateServiceUseCase
import com.phantomshard.keystream.ui.common.ToastMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var serviceRepo: FakeServiceRepository
    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var viewModel: ServicesViewModel

    private val sampleCategories = listOf(
        Category("c1", "Streaming", null),
        Category("c2", "Gaming", null)
    )

    private val sampleServices = listOf(
        Service("s1", "Netflix", "Streaming HD", "https://img.png", 9.99, 10, 4, CategoryRef("c1", "Streaming")),
        Service("s2", "Spotify", null, null, 4.99, 5, 1, null)
    )

    @Before
    fun setUp() {
        serviceRepo = FakeServiceRepository()
        categoryRepo = FakeCategoryRepository()
    }

    private fun createViewModel() = ServicesViewModel(
        getServicesUseCase = GetServicesUseCase(serviceRepo),
        createServiceUseCase = CreateServiceUseCase(serviceRepo),
        updateServiceUseCase = UpdateServiceUseCase(serviceRepo),
        deleteServiceUseCase = DeleteServiceUseCase(serviceRepo),
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepo),
        triggerSyncUseCase = FakeTriggerSyncUseCase()
    )



    @Test
    fun `init loads services and categories on startup`() = runTest {
        categoryRepo.getCategoriesResult = Resource.Success(sampleCategories)
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleServices, state.services)
        assertEquals(sampleCategories, state.categories)
        assertNull(state.error)
    }

    @Test
    fun `init with service API error shows error state`() = runTest {
        serviceRepo.getServicesResult = Resource.Error("Error 401: Unauthorized")
        viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.services.isEmpty())
        assertEquals("Error 401: Unauthorized", state.error)
    }

    @Test
    fun `init with empty services shows empty list`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(emptyList())
        viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.services.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }



    @Test
    fun `onSearchChange updates searchQuery in state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchChange("netflix")

        assertEquals("netflix", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onCategoryFilterChange updates filter and triggers reload`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCategoryFilterChange(sampleCategories[0])
        advanceUntilIdle()

        assertEquals(sampleCategories[0], viewModel.uiState.value.selectedCategoryFilter)
    }

    @Test
    fun `onCategoryFilterChange with null clears filter`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCategoryFilterChange(sampleCategories[0])
        viewModel.onCategoryFilterChange(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategoryFilter)
    }

    @Test
    fun `onRefresh reloads services and categories`() = runTest {
        categoryRepo.getCategoriesResult = Resource.Success(sampleCategories)
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()
        val serviceCallsAfterInit = serviceRepo.getServicesCalls
        val categoryCallsAfterInit = categoryRepo.getCategoriesCalls

        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(serviceCallsAfterInit + 1, serviceRepo.getServicesCalls)
        assertEquals(categoryCallsAfterInit + 1, categoryRepo.getCategoriesCalls)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `onScreenVisible triggers a refresh reload`() = runTest {
        categoryRepo.getCategoriesResult = Resource.Success(sampleCategories)
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()
        val serviceCallsAfterInit = serviceRepo.getServicesCalls
        val categoryCallsAfterInit = categoryRepo.getCategoriesCalls

        viewModel.onScreenVisible()
        advanceUntilIdle()

        assertEquals(serviceCallsAfterInit + 1, serviceRepo.getServicesCalls)
        assertEquals(categoryCallsAfterInit + 1, categoryRepo.getCategoriesCalls)
    }



    @Test
    fun `onShowAddDialog opens dialog with blank fields`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()

        val state = viewModel.uiState.value
        assertTrue(state.showAddDialog)
        assertEquals("", state.dialogName)
        assertEquals("", state.dialogImageUrl)
        assertEquals("", state.dialogDescription)
        assertEquals("0", state.dialogPrice)
        assertEquals("1", state.dialogMaxProfiles)
        assertNull(state.dialogCategoryId)
        assertNull(state.dialogError)
    }

    @Test
    fun `onAddService with empty name shows validation error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onAddService()
        advanceUntilIdle()

        assertEquals("El nombre es requerido", viewModel.uiState.value.dialogError)
        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `onAddService sends all dialog fields to repository`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Disney+")
        viewModel.onDialogImageUrlChange("https://img.com/disney.png")
        viewModel.onDialogDescriptionChange("Plataforma Disney")
        viewModel.onDialogPriceChange("7.99")
        viewModel.onDialogStockChange("20")
        viewModel.onDialogMaxProfilesChange("4")
        viewModel.onDialogCategoryChange("c1")
        viewModel.onAddService()
        advanceUntilIdle()

        assertEquals(1, serviceRepo.createCalls.size)
        val args = serviceRepo.createCalls[0]
        assertEquals("Disney+", args.name)
        assertEquals("https://img.com/disney.png", args.imageUrl)
        assertEquals("Plataforma Disney", args.description)
        assertEquals(7.99, args.price)
        assertEquals(20, args.stock)
        assertEquals(4, args.maxProfiles)
        assertEquals("c1", args.categoryId)
    }

    @Test
    fun `onAddService success closes dialog and emits success toast`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(emptyList())
        serviceRepo.createResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Netflix")
        viewModel.onAddService()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddDialog)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onAddService error keeps dialog open and emits error toast`() = runTest {
        serviceRepo.createResult = Resource.Error("Error 500: Internal Server Error")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Netflix")
        viewModel.onAddService()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAddDialog)
        assertNotNull(viewModel.uiState.value.dialogError)
        assertEquals(1, toastEvents.size)
        assertTrue(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onAddService with empty imageUrl sends null to repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Netflix")
        viewModel.onDialogImageUrlChange("   ")
        viewModel.onAddService()
        advanceUntilIdle()

        val args = serviceRepo.createCalls[0]
        assertNull(args.imageUrl)
    }



    @Test
    fun `onShowEditDialog pre-fills dialog with service data`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleServices[0])

        val state = viewModel.uiState.value
        assertTrue(state.showEditDialog)
        assertEquals("Netflix", state.dialogName)
        assertEquals("https://img.png", state.dialogImageUrl)
        assertEquals("Streaming HD", state.dialogDescription)
        assertEquals("9.99", state.dialogPrice)
        assertEquals("10", state.dialogStock)
        assertEquals("4", state.dialogMaxProfiles)
        assertEquals("c1", state.dialogCategoryId)
        assertEquals(sampleServices[0], state.selectedService)
    }

    @Test
    fun `onShowEditDialog with null imageUrl sets empty string`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleServices[1])

        assertEquals("", viewModel.uiState.value.dialogImageUrl)
        assertNull(viewModel.uiState.value.dialogCategoryId)
    }

    @Test
    fun `onEditService sends correct id and updated fields`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleServices[0])
        viewModel.onDialogNameChange("Netflix Pro")
        viewModel.onDialogPriceChange("12.99")
        viewModel.onEditService()
        advanceUntilIdle()

        assertEquals(1, serviceRepo.updateCalls.size)
        val args = serviceRepo.updateCalls[0]
        assertEquals("s1", args.id)
        assertEquals("Netflix Pro", args.name)
        assertEquals(12.99, args.price)
    }

    @Test
    fun `onEditService with empty name shows validation error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleServices[0])
        viewModel.onDialogNameChange("")
        viewModel.onEditService()
        advanceUntilIdle()

        assertEquals("El nombre es requerido", viewModel.uiState.value.dialogError)
        assertEquals(0, serviceRepo.updateCalls.size)
    }

    @Test
    fun `onEditService success emits success toast`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        serviceRepo.updateResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowEditDialog(sampleServices[0])
        viewModel.onEditService()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditDialog)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onEditService error emits error toast`() = runTest {
        serviceRepo.updateResult = Resource.Error("Error 404: Not Found")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowEditDialog(sampleServices[0])
        viewModel.onEditService()
        advanceUntilIdle()

        assertEquals(1, toastEvents.size)
        assertTrue(toastEvents[0].isError)
        job.cancel()
    }



    @Test
    fun `onShowDeleteDialog sets selected service`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowDeleteDialog(sampleServices[1])

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteDialog)
        assertEquals(sampleServices[1], state.selectedService)
    }

    @Test
    fun `onDeleteService calls repo with correct id`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowDeleteDialog(sampleServices[0])
        viewModel.onDeleteService()
        advanceUntilIdle()

        assertEquals(listOf("s1"), serviceRepo.deletedIds)
    }

    @Test
    fun `onDeleteService success closes dialog and emits success toast`() = runTest {
        serviceRepo.getServicesResult = Resource.Success(sampleServices)
        serviceRepo.deleteResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowDeleteDialog(sampleServices[0])
        viewModel.onDeleteService()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(viewModel.uiState.value.selectedService)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onDeleteService error emits error toast and keeps dialog open`() = runTest {
        serviceRepo.deleteResult = Resource.Error("Error 403: Forbidden")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowDeleteDialog(sampleServices[0])
        viewModel.onDeleteService()
        advanceUntilIdle()

        assertEquals(1, toastEvents.size)
        assertTrue(toastEvents[0].isError)
        job.cancel()
    }



    @Test
    fun `onDismissDialog resets all dialog state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("test")
        viewModel.onDismissDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showAddDialog)
        assertFalse(state.showEditDialog)
        assertFalse(state.showDeleteDialog)
        assertNull(state.selectedService)
        assertNull(state.dialogError)
    }



    @Test
    fun `dialog field setters update state correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDialogImageUrlChange("https://img.png")
        viewModel.onDialogNameChange("Nombre")
        viewModel.onDialogDescriptionChange("Desc")
        viewModel.onDialogPriceChange("5.99")
        viewModel.onDialogStockChange("10")
        viewModel.onDialogMaxProfilesChange("2")
        viewModel.onDialogCategoryChange("c1")

        val state = viewModel.uiState.value
        assertEquals("https://img.png", state.dialogImageUrl)
        assertEquals("Nombre", state.dialogName)
        assertEquals("Desc", state.dialogDescription)
        assertEquals("5.99", state.dialogPrice)
        assertEquals("10", state.dialogStock)
        assertEquals("2", state.dialogMaxProfiles)
        assertEquals("c1", state.dialogCategoryId)
    }

    @Test
    fun `onDialogNameChange clears dialog error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onAddService()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.dialogError)

        viewModel.onDialogNameChange("algo")
        assertNull(viewModel.uiState.value.dialogError)
    }
}
