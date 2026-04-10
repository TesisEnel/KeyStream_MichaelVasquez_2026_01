package com.phantomshard.keystream.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomshard.keystream.data.local.ApiKeyStore
import com.phantomshard.keystream.data.local.dao.CategoryDao
import com.phantomshard.keystream.data.local.dao.ServiceDao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val apiKeyStore: ApiKeyStore,
    private val categoryDao: CategoryDao,
    private val serviceDao: ServiceDao
) : ViewModel() {

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    fun onLogout() {
        viewModelScope.launch {
            apiKeyStore.clearKey()
            categoryDao.deleteAll()
            serviceDao.deleteAll()
            _logoutEvent.emit(Unit)
        }
    }
}
