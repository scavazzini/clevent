package dev.scavazzini.clevent.crypto

import android.content.SharedPreferences
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


class SecretKeyManagerImpl @Inject constructor(
    private val masterKeyManager: MasterKeyManager,
    private val encryptor: SymmetricEncryptor,
    private val preferences: SharedPreferences,
) : SecretKeyManager {

    companion object {
        private const val KEY_ALIAS = "CleventSecretKey"
        private const val KEY_IV = "CleventSecretKeyIv"
    }

    override suspend fun getKey(): SecretKey? {
        val encryptedSecretKeyBytes = getBytesFromSharedPreferences(KEY_ALIAS) ?: return null
        val encryptedSecretKeyIvBytes = getBytesFromSharedPreferences(KEY_IV) ?: return null
        val masterKey = masterKeyManager.getKey() ?: return null

        val secretKeyBytes = encryptor.decrypt(
            data = encryptedSecretKeyBytes,
            iv = encryptedSecretKeyIvBytes,
            key = masterKey,
        )

        return SecretKeySpec(secretKeyBytes, "AES")
    }

    private fun getBytesFromSharedPreferences(key: String): ByteArray? {
        return preferences.getString(key, null)
            ?.decodeFromBase64()
            ?: return null
    }

    private fun String.decodeFromBase64(): ByteArray {
        return Base64.decode(this, Base64.NO_WRAP)
    }

    override suspend fun getKeyInfo(): KeyInfo? {
        val secretKey = getKey() ?: return null

        val secretKeyHash = MessageDigest.getInstance("SHA-1").run {
            update(secretKey.encoded, 0, secretKey.encoded.size)
            digest().toHex()
        }

        return KeyInfo(
            id = secretKeyHash.take(10),
            algorithm = secretKey.algorithm,
            size = secretKey.encoded.size * 8,
        )
    }

    private fun ByteArray.toHex(): String {
        return joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    override suspend fun createKey(
        size: Int,
        algorithm: String,
    ): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(algorithm).apply { init(size) }

        return keyGenerator.generateKey().also {
            persistSecretKey(it)
        }
    }

    private suspend fun persistSecretKey(key: SecretKey) {
        val masterKey = masterKeyManager.createKey()
        val encryptedSecretKey = encryptor.encrypt(key.encoded, masterKey)

        withContext(Dispatchers.IO) {
            with(preferences.edit()) {
                putString(KEY_ALIAS, encryptedSecretKey.cipherData.toBase64())
                putString(KEY_IV, encryptedSecretKey.iv?.toBase64())
                commit()
            }
        }
    }

    private fun ByteArray.toBase64(): String {
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    override suspend fun importKey(content: ByteArray): SecretKey {
        if (content.size !in arrayOf(16, 24, 32)) {
            throw IllegalArgumentException("Key should have a length of 128, 192 or 256 bits")
        }

        return SecretKeySpec(content, "AES").also {
            persistSecretKey(it)
        }
    }

    override suspend fun clearKey() {
        withContext(Dispatchers.IO) {
            with(preferences.edit()) {
                remove(KEY_ALIAS)
                remove(KEY_IV)
                commit()
            }
            masterKeyManager.clearKey()
        }
    }
}
