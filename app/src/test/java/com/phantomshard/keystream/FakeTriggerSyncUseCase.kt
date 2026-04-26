package com.phantomshard.keystream

import com.phantomshard.keystream.domain.usecase.TriggerSyncUseCase

class FakeTriggerSyncUseCase : TriggerSyncUseCase {
    var invokeCount = 0

    override fun invoke() {
        invokeCount++
    }
}
