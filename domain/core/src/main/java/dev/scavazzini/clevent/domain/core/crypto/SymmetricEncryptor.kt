package dev.scavazzini.clevent.domain.core.crypto

import dev.scavazzini.clevent.domain.core.crypto.exception.DecryptionException
import dev.scavazzini.clevent.domain.core.crypto.exception.EncryptionException

class EncryptedPayload(
    val cipherData: ByteArray,
    val iv: ByteArray? = null,
)

private const val DEFAULT_CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"

interface SymmetricEncryptor {
    @Throws(EncryptionException::class)
    suspend fun encrypt(
        data: ByteArray,
        cipherTransformation: String = DEFAULT_CIPHER_TRANSFORMATION,
    ): EncryptedPayload

    @Throws(DecryptionException::class)
    suspend fun decrypt(
        data: ByteArray,
        iv: ByteArray? = null,
        cipherTransformation: String = DEFAULT_CIPHER_TRANSFORMATION,
    ): ByteArray
}
