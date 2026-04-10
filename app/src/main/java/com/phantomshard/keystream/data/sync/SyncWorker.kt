package com.phantomshard.keystream.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phantomshard.keystream.domain.repository.CategoryRepository
import com.phantomshard.keystream.domain.repository.ServiceRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val categoryRepository: CategoryRepository by inject()
    private val serviceRepository: ServiceRepository by inject()

    override suspend fun doWork(): Result {
        categoryRepository.syncPending()
        serviceRepository.syncPending()
        return Result.success()
    }
}
