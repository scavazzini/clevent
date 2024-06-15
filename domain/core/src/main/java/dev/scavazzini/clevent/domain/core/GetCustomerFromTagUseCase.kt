package dev.scavazzini.clevent.domain.core

import android.content.Intent
import dev.scavazzini.clevent.EncryptedCustomerOuterClass
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.repository.TagRepository
import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import dev.scavazzini.clevent.domain.core.crypto.SymmetricEncryptor
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
import javax.crypto.SecretKey
import javax.inject.Inject

class GetCustomerFromTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
    private val keyManager: SecretKeyManager,
    private val encryptor: SymmetricEncryptor,
) {
    suspend operator fun invoke(intent: Intent): Customer {
        val payload = tagRepository.read(intent)
        val secretKey = keyManager.getKey()

        if (payload.all { it == 0x0.toByte() }) {
            return Customer()
        }

        val serializedCustomer = if (secretKey != null) {
            decryptCustomerBytes(payload, secretKey)
        } else {
            payload
        }

        return customerSerializer.deserialize(serializedCustomer)
    }

    private suspend fun decryptCustomerBytes(payload: ByteArray, secretKey: SecretKey): ByteArray {
        val deserializedPayload = EncryptedCustomerOuterClass.EncryptedCustomer.parseFrom(payload)

        return encryptor.decrypt(
            data = deserializedPayload.data.toByteArray(),
            iv = deserializedPayload.iv.toByteArray(),
            key = secretKey,
        )
    }
}
