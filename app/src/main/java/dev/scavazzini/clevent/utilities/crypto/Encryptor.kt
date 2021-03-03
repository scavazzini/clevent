package dev.scavazzini.clevent.utilities.crypto

import dev.scavazzini.clevent.exceptions.DecryptionException

interface Encryptor {
    fun encrypt(data: ByteArray, keySalt: ByteArray?): ByteArray

    @Throws(DecryptionException::class)
    fun decrypt(data: ByteArray, keySalt: ByteArray?): ByteArray
}
