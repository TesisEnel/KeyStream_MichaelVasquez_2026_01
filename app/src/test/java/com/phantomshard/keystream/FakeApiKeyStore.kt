package com.phantomshard.keystream

import com.phantomshard.keystream.data.local.ApiKeyStore

class FakeApiKeyStore : ApiKeyStore {
    private var _apiKey: String = ""

    override var apiKey: String
        get() = _apiKey
        set(value) { _apiKey = value }

    override fun hasKey(): Boolean = _apiKey.isNotEmpty()

    override fun clearKey() { _apiKey = "" }
}
