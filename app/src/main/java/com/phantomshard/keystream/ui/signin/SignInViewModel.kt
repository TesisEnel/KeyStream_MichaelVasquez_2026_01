package com.phantomshard.keystream.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.ApiKeyStore
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val apiKey: String = "",
    val isApiKeyVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface SignInNavigationEvent {
    data object NavigateToDashboard : SignInNavigationEvent
}

class SignInViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onApiKeyChange(value: String) {
        _uiState.update { it.copy(apiKey = value, error = null) }
    }

    fun onToggleApiKeyVisibility() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun onSignIn() {
        val key = _uiState.value.apiKey.trim()
        if (key.isEmpty()) {
            _uiState.update { it.copy(error = "Introduce tu api key") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            apiKeyStore.apiKey = key
            when (val result = getCategoriesUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigationEvent.emit(SignInNavigationEvent.NavigateToDashboard)
                }
                is Resource.Error -> {
                    apiKeyStore.clearKey()
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
