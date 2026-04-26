package com.phantomshard.keystream.ui.dashboard

import com.phantomshard.keystream.FakeApiKeyStore
import com.phantomshard.keystream.FakeCategoryDao
import com.phantomshard.keystream.FakeServiceDao
import com.phantomshard.keystream.MainCoroutineRule
import com.phantomshard.keystream.data.local.entity.CategoryEntity
import com.phantomshard.keystream.data.local.entity.ServiceEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var apiKeyStore: FakeApiKeyStore
    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var serviceDao: FakeServiceDao
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        apiKeyStore = FakeApiKeyStore()
        categoryDao = FakeCategoryDao()
        serviceDao = FakeServiceDao()
        viewModel = DashboardViewModel(apiKeyStore, categoryDao, serviceDao)
    }

    @Test
    fun `onLogout clears the stored API key`() = runTest {
        apiKeyStore.apiKey = "ks_live_somekey"

        viewModel.onLogout()
        advanceUntilIdle()

        assertEquals("", apiKeyStore.apiKey)
    }

    @Test
    fun `onLogout emits logout navigation event`() = runTest {
        val events = mutableListOf<Unit>()
        val job = launch { viewModel.logoutEvent.collect { events.add(it) } }

        viewModel.onLogout()
        advanceUntilIdle()

        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `onLogout clears category Room cache`() = runTest {
        categoryDao.stored.add(CategoryEntity("1", "Streaming", null))
        categoryDao.stored.add(CategoryEntity("2", "Gaming", null))

        viewModel.onLogout()
        advanceUntilIdle()

        assertTrue(categoryDao.stored.isEmpty())
    }

    @Test
    fun `onLogout clears service Room cache`() = runTest {
        serviceDao.stored.add(
            ServiceEntity("s1", "Netflix", null, null, 9.99, 10, 4, null, null)
        )

        viewModel.onLogout()
        advanceUntilIdle()

        assertTrue(serviceDao.stored.isEmpty())
    }

    @Test
    fun `onLogout clears all data atomically before emitting event`() = runTest {
        apiKeyStore.apiKey = "ks_live_key"
        categoryDao.stored.add(CategoryEntity("1", "Streaming", null))
        serviceDao.stored.add(
            ServiceEntity("s1", "Netflix", null, null, 9.99, 10, 4, null, null)
        )

        val events = mutableListOf<Unit>()
        val job = launch { viewModel.logoutEvent.collect { events.add(it) } }

        viewModel.onLogout()
        advanceUntilIdle()

        assertEquals("", apiKeyStore.apiKey)
        assertTrue(categoryDao.stored.isEmpty())
        assertTrue(serviceDao.stored.isEmpty())
        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `onLogout called multiple times emits multiple events`() = runTest {
        val events = mutableListOf<Unit>()
        val job = launch { viewModel.logoutEvent.collect { events.add(it) } }

        viewModel.onLogout()
        viewModel.onLogout()
        advanceUntilIdle()

        assertEquals(2, events.size)
        job.cancel()
    }
}
