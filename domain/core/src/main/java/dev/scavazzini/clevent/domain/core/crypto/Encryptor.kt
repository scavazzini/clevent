package dev.scavazzini.clevent.domain.core.crypto

import dev.scavazzini.clevent.domain.core.crypto.exception.DecryptionException

interface Encryptor {
    fun encrypt(data: ByteArray): ByteArray

    @Throws(DecryptionException::class)
    fun decrypt(data: ByteArray): ByteArray
}
