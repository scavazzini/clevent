package dev.scavazzini.clevent.core.domain

import android.content.Intent
import dev.scavazzini.clevent.EncryptedCustomerOuterClass
import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.repository.TagRepository
import dev.scavazzini.clevent.crypto.SecretKeyManager
import dev.scavazzini.clevent.crypto.SymmetricEncryptor
import dev.scavazzini.clevent.core.domain.serializer.CustomerSerializer
import javax.crypto.SecretKey
import javax.inject.Inject

class GetCustomerFromTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
    private val keyManager: SecretKeyManager,
    private val encryptor: SymmetricEncryptor,
) {
    suspend operator fun invoke(intent: Intent): Customer {
        val tag = tagRepository.getTag(intent)
        val payload = tagRepository.read(tag)
        val secretKey = keyManager.getKey()

        if (payload.isEmpty()) {
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
