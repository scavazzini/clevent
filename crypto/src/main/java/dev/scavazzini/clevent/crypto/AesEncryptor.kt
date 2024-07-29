package dev.scavazzini.clevent.crypto

import dev.scavazzini.clevent.crypto.exception.DecryptionException
import dev.scavazzini.clevent.crypto.exception.EncryptionException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class AesEncryptor @Inject constructor() : SymmetricEncryptor {

    @Throws(EncryptionException::class)
    override suspend fun encrypt(
        data: ByteArray,
        key: SecretKey,
        cipherTransformation: String,
    ): EncryptedPayload {
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
        key: SecretKey,
        cipherTransformation: String,
    ): ByteArray {
        val cipher = Cipher.getInstance(cipherTransformation).apply {
            val params = if (iv != null) IvParameterSpec(iv) else null
            init(Cipher.DECRYPT_MODE, key, params)
        }

        return cipher.doFinal(data)
    }

}
