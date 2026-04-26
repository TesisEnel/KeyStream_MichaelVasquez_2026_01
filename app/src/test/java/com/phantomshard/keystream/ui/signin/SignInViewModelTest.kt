package com.phantomshard.keystream.ui.signin

import com.phantomshard.keystream.FakeApiKeyStore
import com.phantomshard.keystream.FakeCategoryRepository
import com.phantomshard.keystream.MainCoroutineRule
import com.phantomshard.keystream.data.common.Resource
import com.phantomshard.keystream.data.local.ApiKeyStore
import com.phantomshard.keystream.domain.model.Category
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var apiKeyStore: ApiKeyStore
    private lateinit var viewModel: SignInViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeCategoryRepository()
        apiKeyStore = FakeApiKeyStore()
        viewModel = SignInViewModel(GetCategoriesUseCase(fakeRepository), apiKeyStore)
    }

    @Test
    fun `initial state is empty and not loading`() {
        val state = viewModel.uiState.value
        assertEquals("", state.apiKey)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onApiKeyChange updates apiKey and clears error`() {
        viewModel.onApiKeyChange("ks_live_test")
        assertEquals("ks_live_test", viewModel.uiState.value.apiKey)

        viewModel.onApiKeyChange("ks_live_bad")
        viewModel.onSignIn()
        viewModel.onApiKeyChange("ks_live_new")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onSignIn with empty key shows error without API call`() = runTest {
        viewModel.onSignIn()
        advanceUntilIdle()

        assertEquals("Introduce tu api key", viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("", apiKeyStore.apiKey)
    }

    @Test
    fun `onSignIn with valid key and successful response emits navigation event`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(
            listOf(Category("1", "Streaming", null))
        )
        viewModel.onApiKeyChange("ks_live_valid_key")

        val events = mutableListOf<SignInNavigationEvent>()
        val job = launch { viewModel.navigationEvent.collect { events.add(it) } }

        viewModel.onSignIn()
        advanceUntilIdle()

        assertTrue(events.contains(SignInNavigationEvent.NavigateToDashboard))
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals("ks_live_valid_key", apiKeyStore.apiKey)
        job.cancel()
    }

    @Test
    fun `onSignIn with API error shows error and clears stored key`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Error("INVALID_API_KEY")
        viewModel.onApiKeyChange("ks_live_bad")

        viewModel.onSignIn()
        advanceUntilIdle()

        assertEquals("INVALID_API_KEY", viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("", apiKeyStore.apiKey)
    }

    @Test
    fun `onToggleApiKeyVisibility flips visibility`() {
        assertEquals(false, viewModel.uiState.value.isApiKeyVisible)
        viewModel.onToggleApiKeyVisibility()
        assertEquals(true, viewModel.uiState.value.isApiKeyVisible)
        viewModel.onToggleApiKeyVisibility()
        assertEquals(false, viewModel.uiState.value.isApiKeyVisible)
    }

    @Test
    fun `onSignIn trims whitespace from api key`() = runTest {
        fakeRepository.getCategoriesResult = Resource.Success(emptyList())
        viewModel.onApiKeyChange("  ks_live_trimmed  ")

        viewModel.onSignIn()
        advanceUntilIdle()

        assertEquals("ks_live_trimmed", apiKeyStore.apiKey)
    }
}
