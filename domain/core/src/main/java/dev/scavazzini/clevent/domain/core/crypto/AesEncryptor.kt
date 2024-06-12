package dev.scavazzini.clevent.domain.core.crypto

import dev.scavazzini.clevent.domain.core.crypto.exception.DecryptionException
import dev.scavazzini.clevent.domain.core.crypto.exception.EncryptionException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class AesEncryptor @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) : SymmetricEncryptor {

    @Throws(EncryptionException::class)
    override suspend fun encrypt(
        data: ByteArray,
        cipherTransformation: String,
    ): EncryptedPayload {
        val key = secretKeyManager.getKey()
            ?: throw EncryptionException("No Secret Key available to encrypt data")

        val cipher = Cipher.getInstance(cipherTransformation).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

        return EncryptedPayload(
            cipherData = cipher.doFinal(data),
            iv = cipher.iv,
        )
    }

    @Throws(DecryptionException::class)
    override suspend fun decrypt(
        data: ByteArray,
        iv: ByteArray?,
        cipherTransformation: String,
    ): ByteArray {
        val key = secretKeyManager.getKey()
            ?: throw DecryptionException("No Secret Key available to decrypt data")

        val cipher = Cipher.getInstance(cipherTransformation).apply {
            val params = if (iv != null) IvParameterSpec(iv) else null
            init(Cipher.DECRYPT_MODE, key, params)
        }

        return cipher.doFinal(data)
    }

}
