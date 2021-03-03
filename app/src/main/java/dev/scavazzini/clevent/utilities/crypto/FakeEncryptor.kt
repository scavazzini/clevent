package dev.scavazzini.clevent.utilities.crypto

class FakeEncryptor : Encryptor {
    override fun encrypt(data: ByteArray, keySalt: ByteArray?): ByteArray {
        return data
    }

    override fun decrypt(data: ByteArray, keySalt: ByteArray?): ByteArray {
        return data
    }
}
