package com.phantomshard.keystream.di

import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.phantomshard.keystream.data.local.ApiKeyStore
import com.phantomshard.keystream.data.local.EncryptedApiKeyStore
import com.phantomshard.keystream.data.local.db.KeyStreamDatabase
import com.phantomshard.keystream.data.remote.KeyStreamApi
import com.phantomshard.keystream.data.repository.CategoryRepositoryImpl
import com.phantomshard.keystream.data.repository.ServiceRepositoryImpl
import com.phantomshard.keystream.domain.repository.CategoryRepository
import com.phantomshard.keystream.domain.repository.ServiceRepository
import com.phantomshard.keystream.domain.usecase.category.CreateCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.DeleteCategoryUseCase
import com.phantomshard.keystream.domain.usecase.category.GetCategoriesUseCase
import com.phantomshard.keystream.domain.usecase.category.UpdateCategoryUseCase
import com.phantomshard.keystream.domain.usecase.service.CreateServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.DeleteServiceUseCase
import com.phantomshard.keystream.domain.usecase.service.GetServicesUseCase
import com.phantomshard.keystream.domain.usecase.service.UpdateServiceUseCase
import com.phantomshard.keystream.domain.usecase.TriggerSyncUseCase
import com.phantomshard.keystream.domain.usecase.TriggerSyncUseCaseImpl
import com.phantomshard.keystream.ui.categories.CategoriesViewModel
import com.phantomshard.keystream.ui.dashboard.DashboardViewModel
import com.phantomshard.keystream.ui.services.ServicesViewModel
import com.phantomshard.keystream.ui.signin.SignInViewModel
import com.phantomshard.keystream.util.Constants
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {

    single<ApiKeyStore> { EncryptedApiKeyStore(androidContext()) }

    single {
        val apiKeyStore: ApiKeyStore = get()
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-Key", apiKeyStore.apiKey)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    single {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(KeyStreamApi::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            KeyStreamDatabase::class.java,
            "keystream.db"
        ).fallbackToDestructiveMigration(true).build()
    }

    single { get<KeyStreamDatabase>().categoryDao() }
    single { get<KeyStreamDatabase>().serviceDao() }

    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get(), get()) }

    single { GetCategoriesUseCase(get()) }
    single { CreateCategoryUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }

    single { GetServicesUseCase(get()) }
    single { CreateServiceUseCase(get()) }
    single { UpdateServiceUseCase(get()) }
    single { DeleteServiceUseCase(get()) }

    single<TriggerSyncUseCase> { TriggerSyncUseCaseImpl(androidContext()) }

    viewModel { SignInViewModel(get(), get()) }
    viewModel { CategoriesViewModel(get(), get(), get(), get(), get()) }
    viewModel { ServicesViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
}
