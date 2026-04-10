package com.phantomshard.keystream.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.model.Service
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import com.phantomshard.keystream.domain.usecase.service.CreateServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.DeleteServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.GetServicesUseCase
import com.phantomshard.keystream.domain.usecase.service.UpdateServiceUseCase
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

data class ServicesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val services: List<Service> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: Category? = null,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedService: Service? = null,
    val dialogImageUrl: String = "",
    val dialogName: String = "",
    val dialogDescription: String = "",
    val dialogPrice: String = "0",
    val dialogStock: String = "0",
    val dialogMaxProfiles: String = "1",
    val dialogCategoryId: String? = null,
    val isDialogLoading: Boolean = false,
    val dialogError: String? = null
)

class ServicesViewModel(
    private val getServicesUseCase: GetServicesUseCase,
    private val createServiceUseCase: CreateServiceUseCase,
    private val updateServiceUseCase: UpdateServiceUseCase,
    private val deleteServiceUseCase: DeleteServiceUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase
) : ViewModel() {

    private companion object {
        const val DUPLICATE_SERVICE_MESSAGE = "Ya existe un servicio con ese nombre."
    }

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<ToastMessage>()
    val toastEvent: SharedFlow<ToastMessage> = _toastEvent.asSharedFlow()
    private var searchJob: Job? = null

    init {
        loadCategories()
        loadServices()
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadServices()
        }
    }

    fun onRefresh() {
        searchJob?.cancel()
        refreshData(showRefreshIndicator = true)
    }

    fun onScreenVisible() {
        onRefresh()
    }

    fun onCategoryFilterChange(category: Category?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
        loadServices(showRefreshIndicator = true)
    }

    fun onShowAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                dialogImageUrl = "",
                dialogName = "",
                dialogDescription = "",
                dialogPrice = "0",
                dialogStock = "0",
                dialogMaxProfiles = "1",
                dialogCategoryId = null,
                dialogError = null
            )
        }
    }

    fun onShowEditDialog(service: Service) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                selectedService = service,
                dialogImageUrl = service.imageUrl ?: "",
                dialogName = service.name,
                dialogDescription = service.description ?: "",
                dialogPrice = service.price.toString(),
                dialogStock = service.stock.toString(),
                dialogMaxProfiles = service.maxProfiles.toString(),
                dialogCategoryId = service.category?.id,
                dialogError = null
            )
        }
    }

    fun onShowDeleteDialog(service: Service) {
        _uiState.update { it.copy(showDeleteDialog = true, selectedService = service) }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                showEditDialog = false,
                showDeleteDialog = false,
                selectedService = null,
                dialogError = null
            )
        }
    }

    fun onDialogImageUrlChange(url: String) { _uiState.update { it.copy(dialogImageUrl = url) } }
    fun onDialogNameChange(name: String) { _uiState.update { it.copy(dialogName = name, dialogError = null) } }
    fun onDialogDescriptionChange(desc: String) { _uiState.update { it.copy(dialogDescription = desc) } }
    fun onDialogPriceChange(price: String) { _uiState.update { it.copy(dialogPrice = price) } }
    fun onDialogStockChange(stock: String) { _uiState.update { it.copy(dialogStock = stock) } }
    fun onDialogMaxProfilesChange(max: String) { _uiState.update { it.copy(dialogMaxProfiles = max) } }
    fun onDialogCategoryChange(categoryId: String?) { _uiState.update { it.copy(dialogCategoryId = categoryId) } }

    fun onAddService() {
        val name = _uiState.value.dialogName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(dialogError = "El nombre es requerido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            val state = _uiState.value
            val description = state.dialogDescription.trim().takeIf { it.isNotEmpty() }
            val imageUrl = state.dialogImageUrl.trim().takeIf { it.isNotEmpty() }
            val price = state.dialogPrice.toDoubleOrNull() ?: 0.0
            val stock = state.dialogStock.toIntOrNull() ?: 0
            val maxProfiles = state.dialogMaxProfiles.toIntOrNull() ?: 1
            when (val result = createServiceUseCase(name, state.dialogCategoryId, description, imageUrl, price, stock, maxProfiles)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadServices()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Creado", "El servicio ha sido creado.", isError = false))
                }
                is Resource.Error -> {
                    val errorMessage = result.message ?: "No se pudo crear el servicio."
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = errorMessage) }
                    if (errorMessage != DUPLICATE_SERVICE_MESSAGE) {
                        _toastEvent.emit(ToastMessage("Error", errorMessage, isError = true))
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onEditService() {
        val name = _uiState.value.dialogName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(dialogError = "El nombre es requerido") }
            return
        }
        val id = _uiState.value.selectedService?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            val state = _uiState.value
            val description = state.dialogDescription.trim().takeIf { it.isNotEmpty() }
            val imageUrl = state.dialogImageUrl.trim().takeIf { it.isNotEmpty() }
            val price = state.dialogPrice.toDoubleOrNull() ?: 0.0
            val stock = state.dialogStock.toIntOrNull() ?: 0
            val maxProfiles = state.dialogMaxProfiles.toIntOrNull() ?: 1
            when (val result = updateServiceUseCase(id, name, state.dialogCategoryId, description, imageUrl, price, stock, maxProfiles)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadServices()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Actualizado", "El servicio ha sido actualizado.", isError = false))
                }
                is Resource.Error -> {
                    val errorMessage = result.message ?: "No se pudo actualizar el servicio."
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = errorMessage) }
                    if (errorMessage != DUPLICATE_SERVICE_MESSAGE) {
                        _toastEvent.emit(ToastMessage("Error", errorMessage, isError = true))
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteService() {
        val id = _uiState.value.selectedService?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogLoading = true) }
            when (val result = deleteServiceUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDialogLoading = false) }
                    onDismissDialog()
                    loadServices()
                    triggerSyncUseCase()
                    _toastEvent.emit(ToastMessage("Eliminado", "El servicio ha sido eliminado.", isError = false))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDialogLoading = false, dialogError = result.message) }
                    _toastEvent.emit(ToastMessage("Error", result.message ?: "No se pudo eliminar el servicio.", isError = true))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadServices(showRefreshIndicator: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !showRefreshIndicator,
                    isRefreshing = showRefreshIndicator,
                    error = null
                )
            }
            val state = _uiState.value
            val search = state.searchQuery.trim().takeIf { it.isNotEmpty() }
            val categoryId = state.selectedCategoryFilter?.id
            when (val result = getServicesUseCase(search = search, categoryId = categoryId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, services = result.data ?: emptyList())
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is Resource.Success -> _uiState.update { it.copy(categories = result.data ?: emptyList()) }
                else -> Unit
            }
        }
    }

    private fun refreshData(showRefreshIndicator: Boolean = false) {
        loadCategories()
        loadServices(showRefreshIndicator = showRefreshIndicator)
    }
}
