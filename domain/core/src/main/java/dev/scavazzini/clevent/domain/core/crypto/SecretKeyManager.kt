package dev.scavazzini.clevent.domain.core.crypto

import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import javax.crypto.SecretKey

interface SecretKeyManager {
    suspend fun getKey(): SecretKey?

    suspend fun getKeyInfo(secretKey: SecretKey): KeyInfo?

    suspend fun createKey(
        size: Int = 256,
        algorithm: String = KeyProperties.KEY_ALGORITHM_AES,
        blockMode: String = KeyProperties.BLOCK_MODE_CBC,
        encryptionPaddings: String = KeyProperties.ENCRYPTION_PADDING_PKCS7,
        requireUserAuthentication: Boolean = false,
    ): SecretKey

    suspend fun clearKey()
}
