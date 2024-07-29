package dev.scavazzini.clevent.core.domain.crypto

import android.security.keystore.KeyProperties
import dev.scavazzini.clevent.core.domain.crypto.KeyInfo
import javax.crypto.SecretKey

interface SecretKeyManager {
    suspend fun getKey(): SecretKey?

    suspend fun getKeyInfo(): KeyInfo?

    suspend fun createKey(
        size: Int = 256,
        algorithm: String = KeyProperties.KEY_ALGORITHM_AES,
    ): SecretKey

    suspend fun importKey(content: ByteArray): SecretKey

    suspend fun clearKey()
}

