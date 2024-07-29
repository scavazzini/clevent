package dev.scavazzini.clevent.crypto

import javax.crypto.SecretKey
import javax.inject.Inject

class FakeSymmetricEncryptor @Inject constructor() : SymmetricEncryptor {
    override suspend fun encrypt(
        data: ByteArray,
        key: SecretKey,
        cipherTransformation: String,
    ): EncryptedPayload {
        return EncryptedPayload(data)
    }

    override suspend fun decrypt(
        data: ByteArray,
        iv: ByteArray?,
        key: SecretKey,
        cipherTransformation: String,
    ): ByteArray {
        return data
    }
}
