package com.phantomshard.keystream.ui.categories

import com.phantomshard.keystream.FakeCategoryRepository
import com.phantomshard.keystream.FakeTriggerSyncUseCase
import com.phantomshard.keystream.MainCoroutineRule
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.usecase.category.CreateCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.DeleteCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import com.phantomshard.keystream.domain.usecase.category.UpdateCategoryUseCase
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
class CategoriesViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var viewModel: CategoriesViewModel

    private val sampleCategories = listOf(
        Category("1", "Streaming", "Servicios de streaming"),
        Category("2", "Gaming", null)
    )

    @Before
    fun setUp() {
        fakeRepository = FakeCategoryRepository()
    }

    private fun createViewModel() = CategoriesViewModel(
        getCategoriesUseCase = GetCategoriesUseCase(fakeRepository),
        createCategoryUseCase = CreateCategoryUseCase(fakeRepository),
        updateCategoryUseCase = UpdateCategoryUseCase(fakeRepository),
        deleteCategoryUseCase = DeleteCategoryUseCase(fakeRepository),
        triggerSyncUseCase = FakeTriggerSyncUseCase()
    )



    @Test
    fun `init loads categories successfully`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleCategories, state.categories)
        assertNull(state.error)
    }

    @Test
    fun `init with API error shows error state`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Error("Error 401: Unauthorized")
        viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.categories.isEmpty())
        assertEquals("Error 401: Unauthorized", state.error)
    }

    @Test
    fun `init with empty list shows empty state without error`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(emptyList())
        viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categories.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }



    @Test
    fun `onSearchChange updates search query in state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchChange("stream")

        assertEquals("stream", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onSearch triggers a reload`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchChange("gam")
        viewModel.onSearch()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onScreenVisible triggers a refresh reload`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()
        advanceUntilIdle()
        val callsAfterInit = fakeRepository.getCategoriesCalls

        viewModel.onScreenVisible()
        advanceUntilIdle()

        assertEquals(callsAfterInit + 1, fakeRepository.getCategoriesCalls)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }



    @Test
    fun `onShowAddDialog opens dialog with empty fields`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()

        val state = viewModel.uiState.value
        assertTrue(state.showAddDialog)
        assertEquals("", state.dialogName)
        assertEquals("", state.dialogDescription)
        assertNull(state.dialogError)
    }

    @Test
    fun `onAddCategory with empty name shows validation error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertEquals("El nombre es requerido", viewModel.uiState.value.dialogError)
        assertTrue(viewModel.uiState.value.showAddDialog)
    }

    @Test
    fun `onAddCategory with whitespace-only name shows validation error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("   ")
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertEquals("El nombre es requerido", viewModel.uiState.value.dialogError)
    }

    @Test
    fun `onAddCategory sends correct name and description to repository`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Nueva Categoría")
        viewModel.onDialogDescriptionChange("Descripción opcional")
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.createdCategories.size)
        assertEquals("Nueva Categoría", fakeRepository.createdCategories[0].first)
        assertEquals("Descripción opcional", fakeRepository.createdCategories[0].second)
    }

    @Test
    fun `onAddCategory with empty description sends null to repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Solo nombre")
        viewModel.onDialogDescriptionChange("")
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertNull(fakeRepository.createdCategories[0].second)
    }

    @Test
    fun `onAddCategory success closes dialog and emits success toast`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(emptyList())
        fakeRepository.createResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Gaming")
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddDialog)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onAddCategory API error keeps dialog open and emits error toast`() = runTest {
        fakeRepository.createResult = Resource.Error("Error 500")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowAddDialog()
        viewModel.onDialogNameChange("Gaming")
        viewModel.onAddCategory()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAddDialog)
        assertNotNull(viewModel.uiState.value.dialogError)
        assertEquals(1, toastEvents.size)
        assertTrue(toastEvents[0].isError)
        job.cancel()
    }



    @Test
    fun `onShowEditDialog pre-fills dialog with category data`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleCategories[0])

        val state = viewModel.uiState.value
        assertTrue(state.showEditDialog)
        assertEquals("Streaming", state.dialogName)
        assertEquals("Servicios de streaming", state.dialogDescription)
        assertEquals(sampleCategories[0], state.selectedCategory)
        assertNull(state.dialogError)
    }

    @Test
    fun `onShowEditDialog with null description sets empty string`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleCategories[1])

        assertEquals("", viewModel.uiState.value.dialogDescription)
    }

    @Test
    fun `onEditCategory sends correct id and updated fields`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleCategories[0])
        viewModel.onDialogNameChange("Streaming Plus")
        viewModel.onDialogDescriptionChange("Nueva descripción")
        viewModel.onEditCategory()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.updatedCategories.size)
        assertEquals("1", fakeRepository.updatedCategories[0].id)
        assertEquals("Streaming Plus", fakeRepository.updatedCategories[0].name)
        assertEquals("Nueva descripción", fakeRepository.updatedCategories[0].description)
        assertFalse(viewModel.uiState.value.showEditDialog)
    }

    @Test
    fun `onEditCategory with empty name shows validation error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowEditDialog(sampleCategories[0])
        viewModel.onDialogNameChange("")
        viewModel.onEditCategory()
        advanceUntilIdle()

        assertEquals("El nombre es requerido", viewModel.uiState.value.dialogError)
        assertTrue(viewModel.uiState.value.showEditDialog)
    }

    @Test
    fun `onEditCategory success closes dialog and emits success toast`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        fakeRepository.updateResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowEditDialog(sampleCategories[0])
        viewModel.onEditCategory()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditDialog)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onEditCategory API error emits error toast`() = runTest {
        fakeRepository.updateResult = Resource.Error("Error 404: Not Found")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowEditDialog(sampleCategories[0])
        viewModel.onEditCategory()
        advanceUntilIdle()

        assertEquals(1, toastEvents.size)
        assertTrue(toastEvents[0].isError)
        job.cancel()
    }



    @Test
    fun `onShowDeleteDialog sets selected category`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowDeleteDialog(sampleCategories[1])

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteDialog)
        assertEquals(sampleCategories[1], state.selectedCategory)
    }

    @Test
    fun `onDeleteCategory calls repository with correct id`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowDeleteDialog(sampleCategories[0])
        viewModel.onDeleteCategory()
        advanceUntilIdle()

        assertEquals(listOf("1"), fakeRepository.deletedIds)
    }

    @Test
    fun `onDeleteCategory success closes dialog and emits success toast`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(sampleCategories)
        fakeRepository.deleteResult = Resource.Success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowDeleteDialog(sampleCategories[0])
        viewModel.onDeleteCategory()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(viewModel.uiState.value.selectedCategory)
        assertEquals(1, toastEvents.size)
        assertFalse(toastEvents[0].isError)
        job.cancel()
    }

    @Test
    fun `onDeleteCategory API error emits error toast and keeps dialog open`() = runTest {
        fakeRepository.deleteResult = Resource.Error("Error 403: Forbidden")
        viewModel = createViewModel()
        advanceUntilIdle()

        val toastEvents = mutableListOf<ToastMessage>()
        val job = launch { viewModel.toastEvent.collect { toastEvents.add(it) } }

        viewModel.onShowDeleteDialog(sampleCategories[0])
        viewModel.onDeleteCategory()
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
        assertEquals("", state.dialogName)
        assertEquals("", state.dialogDescription)
        assertNull(state.selectedCategory)
        assertNull(state.dialogError)
    }



    @Test
    fun `onDialogNameChange updates name and clears error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAddDialog()
        viewModel.onAddCategory()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.dialogError)

        viewModel.onDialogNameChange("Corrección")

        assertEquals("Corrección", viewModel.uiState.value.dialogName)
        assertNull(viewModel.uiState.value.dialogError)
    }

    @Test
    fun `onDialogDescriptionChange updates description`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDialogDescriptionChange("Mi descripción")

        assertEquals("Mi descripción", viewModel.uiState.value.dialogDescription)
    }
}
