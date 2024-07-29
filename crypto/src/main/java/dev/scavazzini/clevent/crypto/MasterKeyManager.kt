package dev.scavazzini.clevent.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.inject.Inject
import android.security.keystore.KeyInfo as AndroidKeyInfo

class MasterKeyManager @Inject constructor() : SecretKeyManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "CleventMasterKey"
    }

    override suspend fun getKey(): SecretKey? {
        val keyStore = loadKeyStore()

        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    private fun loadKeyStore(): KeyStore {
        return KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    override suspend fun getKeyInfo(): KeyInfo? {
        val secretKey = getKey() ?: return null

        val factory = SecretKeyFactory.getInstance(
            secretKey.algorithm,
            KEYSTORE_PROVIDER,
        )

        val keyInfo = factory.getKeySpec(
            secretKey,
            AndroidKeyInfo::class.java,
        ) as AndroidKeyInfo

        return KeyInfo(
            id = KEY_ALIAS,
            algorithm = secretKey.algorithm,
            size = keyInfo.keySize,
        )
    }

    override suspend fun createKey(
        size: Int,
        algorithm: String,
    ): SecretKey {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            /* keystoreAlias = */ KEY_ALIAS,
            /* purposes = */ KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).run {
            setKeySize(size)
            setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            build()
        }

        return with(KeyGenerator.getInstance(algorithm, KEYSTORE_PROVIDER)) {
            init(keyGenParameterSpec)
            generateKey()
        }
    }

    override suspend fun importKey(content: ByteArray): SecretKey {
        throw UnsupportedOperationException("Master key cannot be imported.")
    }

    override suspend fun clearKey() {
        loadKeyStore().apply { this.deleteEntry(KEY_ALIAS) }
    }
}
