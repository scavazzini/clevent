package dev.scavazzini.clevent.domain.core.crypto

import javax.inject.Inject

class FakeSymmetricEncryptor @Inject constructor() : SymmetricEncryptor {
    override suspend fun encrypt(data: ByteArray): EncryptedPayload {
        return EncryptedPayload(data)
    }

    override suspend fun decrypt(data: ByteArray, iv: ByteArray?): ByteArray {
        return data
    }
}
