package dev.scavazzini.clevent.crypto

import android.security.keystore.KeyProperties
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

