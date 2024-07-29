package dev.scavazzini.clevent.core.domain

import android.content.Intent
import com.google.protobuf.ByteString
import dev.scavazzini.clevent.EncryptedCustomerOuterClass
import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.repository.TagRepository
import dev.scavazzini.clevent.crypto.SecretKeyManager
import dev.scavazzini.clevent.crypto.SymmetricEncryptor
import dev.scavazzini.clevent.core.domain.serializer.CustomerSerializer
import javax.crypto.SecretKey
import javax.inject.Inject

class WriteCustomerOnTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
    private val keyManager: SecretKeyManager,
    private val encryptor: SymmetricEncryptor,
) {
    suspend operator fun invoke(
        customer: Customer,
        intent: Intent,
    ) {
        val serializedCustomer = customerSerializer.serialize(customer)
        val secretKey = keyManager.getKey()

        val payload = if (secretKey != null) {
            encryptCustomerBytes(serializedCustomer, secretKey)
        } else {
            serializedCustomer
        }

        val tag = tagRepository.getTag(intent)
        tagRepository.write(payload, tag)
    }

    private suspend fun encryptCustomerBytes(
        serializedCustomer: ByteArray,
        secretKey: SecretKey,
    ): ByteArray {
        val encryptedPayload = encryptor.encrypt(serializedCustomer, secretKey)

        val encryptedCustomerBytes = encryptedPayload.cipherData
        val encryptedCustomerIvBytes = encryptedPayload.iv

        val customerSerializerBuilder = EncryptedCustomerOuterClass.EncryptedCustomer.newBuilder()

        return with(customerSerializerBuilder) {
            this.data = ByteString.copyFrom(encryptedCustomerBytes)
            this.iv = ByteString.copyFrom(encryptedCustomerIvBytes)
            build()
        }.toByteArray()
    }
}
