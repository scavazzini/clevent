package dev.scavazzini.clevent.domain.core.crypto

import dev.scavazzini.clevent.domain.core.crypto.exception.DecryptionException
import dev.scavazzini.clevent.domain.core.crypto.exception.EncryptionException

class EncryptedPayload(
    val cipherData: ByteArray,
    val iv: ByteArray? = null,
)

interface SymmetricEncryptor {
    @Throws(EncryptionException::class)
    suspend fun encrypt(data: ByteArray): EncryptedPayload

    @Throws(DecryptionException::class)
    suspend fun decrypt(data: ByteArray, iv: ByteArray? = null): ByteArray
}
