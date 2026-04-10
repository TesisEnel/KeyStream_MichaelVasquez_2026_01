package com.phantomshard.keystream.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface ApiKeyStore {
    var apiKey: String
    fun hasKey(): Boolean
    fun clearKey()
}

class EncryptedApiKeyStore(context: Context) : ApiKeyStore {

    private companion object {
        const val PREFS_FILE = "keystream_secure_prefs"
        const val KEY_API_KEY = "api_key"
    }

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "").orEmpty()
        set(value) { prefs.edit().putString(KEY_API_KEY, value).apply() }

    override fun hasKey(): Boolean = apiKey.isNotEmpty()

    override fun clearKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }
}
