package dev.scavazzini.clevent.domain.core.crypto

import javax.inject.Inject

class FakeEncryptor @Inject constructor() : Encryptor {
    override fun encrypt(data: ByteArray, keySalt: ByteArray?): ByteArray {
        return data
    }

    override fun decrypt(data: ByteArray, keySalt: ByteArray?): ByteArray {
        return data
    }
}
