package com.phantomshard.keystream.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.usecase.category.CreateCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.DeleteCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import com.phantomshard.keystream.domain.usecase.category.UpdateCategoryUseCase
import com.phantomshard.keystream.domain.usecase.TriggerSyncUseCase
import com.phantomshard.keystream.ui.common.ToastMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedCategory: Category? = null,
    val dialogName: String = "",
    val dialogDescription: String = "",
    val isDialogLoading: Boolean = false,
    val dialogError: String? = null
)

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase
) : ViewModel() {

    private companion object {
        const val DUPLICATE_CATEGORY_MESSAGE = "Ya existe una categoría con ese nombre."
    }

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<ToastMessage>()
    val toastEvent: SharedFlow<ToastMessage> = _toastEvent.asSharedFlow()
    private var searchJob: Job? = null

    init {
        loadCategories()
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadCategories()
        }
    }

    fun onSearch() {
        searchJob?.cancel()
        loadCategories()
    }

    fun onRefresh() {
        searchJob?.cancel()
        loadCategories(showRefreshIndicator = true)
    }

    fun onScreenVisible() {
        onRefresh()
    }

    fun onShowAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, dialogName = "", dialogDescription = "", dialogError = null) }
    }

    fun onShowEditDialog(category: Category) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                selectedCategory = category,
                dialogName = category.name,
                dialogDescription = category.description ?: "",
                dialogError = null
            )
        }
    }

    fun onShowDeleteDialog(category: Category) {
        _uiState.update { it.copy(showDeleteDialog = true, selectedCategory = category) }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                showEditDialog = false,
                showDeleteDialog = false,
                selectedCategory = null,
                dialogName = "",
                dialogDescription = "",
                dialogError = null
            )
        }
    }

    fun onDialogNameChange(name: String) {
        _uiState.update { it.copy(dialogName = name, dialogError = null) }
    }

    fun onDialogDescriptionChange(description: String) {
        _uiState.update { it.copy(dialogDescription = description) }
    }

    fun onAddCategory() {
        val name = _uiState.value.dialogName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(dialogError = "El nombre es requerido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            val description = _uiState.value.dialogDescription.trim().takeIf { it.isNotEmpty() }
            when (val result = createCategoryUseCase(name, description)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadCategories()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Creado", "La categoría ha sido creada.", isError = false))
                }
                is Resource.Error -> {
                    val errorMessage = result.message ?: "No se pudo crear la categoría."
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = errorMessage) }
                    if (errorMessage != DUPLICATE_CATEGORY_MESSAGE) {
                        _toastEvent.emit(ToastMessage("Error", errorMessage, isError = true))
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onEditCategory() {
        val name = _uiState.value.dialogName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(dialogError = "El nombre es requerido") }
            return
        }
        val id = _uiState.value.selectedCategory?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            val description = _uiState.value.dialogDescription.trim().takeIf { it.isNotEmpty() }
            when (val result = updateCategoryUseCase(id, name, description)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadCategories()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Actualizado", "La categoría ha sido actualizada.", isError = false))
                }
                is Resource.Error -> {
                    val errorMessage = result.message ?: "No se pudo actualizar la categoría."
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = errorMessage) }
                    if (errorMessage != DUPLICATE_CATEGORY_MESSAGE) {
                        _toastEvent.emit(ToastMessage("Error", errorMessage, isError = true))
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteCategory() {
        val id = _uiState.value.selectedCategory?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            when (val result = deleteCategoryUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadCategories()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Eliminado", "La categoría ha sido eliminada.", isError = false))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = result.message) }
                    _toastEvent.emit(ToastMessage("Error", result.message ?: "No se pudo eliminar la categoría.", isError = true))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadCategories(showRefreshIndicator: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !showRefreshIndicator,
                    isRefreshing = showRefreshIndicator,
                    error = null
                )
            }
            val search = _uiState.value.searchQuery.trim().takeIf { it.isNotEmpty() }
            when (val result = getCategoriesUseCase(search = search)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        categories = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
