package com.phantomshard.keystream.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.phantomshard.keystream.data.sync.SyncWorker

interface TriggerSyncUseCase {
    operator fun invoke()
}

class TriggerSyncUseCaseImpl(private val context: Context) : TriggerSyncUseCase {

    override fun invoke() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val SYNC_WORK_NAME = "keystream_sync"
    }
}
