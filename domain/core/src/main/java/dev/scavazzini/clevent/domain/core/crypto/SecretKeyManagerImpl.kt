package dev.scavazzini.clevent.domain.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SecretKeyManagerImpl @Inject constructor() : SecretKeyManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "CleventKey"
    }

    override suspend fun getKey(): SecretKey? = suspendCoroutine { continuation ->
        try {
            val keyStore = loadKeyStore()
            val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as SecretKeyEntry
            continuation.resume(secretKeyEntry.secretKey)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    private fun loadKeyStore(): KeyStore {
        return KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    override suspend fun getKeyInfo(
        secretKey: SecretKey,
    ): KeyInfo? = suspendCoroutine { continuation ->
        try {
            val factory = SecretKeyFactory.getInstance(secretKey.algorithm, KEYSTORE_PROVIDER)
            val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

            continuation.resume(keyInfo)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun createKey(
        size: Int,
        algorithm: String,
        blockMode: String,
        encryptionPaddings: String,
        requireUserAuthentication: Boolean,
    ): SecretKey = suspendCoroutine { continuation ->
        try {
            val keyGenerator = KeyGenerator.getInstance(algorithm, KEYSTORE_PROVIDER)

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                /* keystoreAlias = */ KEY_ALIAS,
                /* purposes = */ KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).run {
                setKeySize(size)
                setBlockModes(blockMode)
                setEncryptionPaddings(encryptionPaddings)
                setUserAuthenticationRequired(requireUserAuthentication)
                build()
            }

            keyGenerator.init(keyGenParameterSpec)
            continuation.resume(keyGenerator.generateKey())
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun clearKey() = suspendCoroutine { continuation ->
        try {
            loadKeyStore().apply { this.deleteEntry(KEY_ALIAS) }
            continuation.resume(Unit)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
