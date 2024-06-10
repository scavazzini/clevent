package dev.scavazzini.clevent.domain.core

import android.content.Intent
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.repository.TagRepository
import dev.scavazzini.clevent.domain.core.crypto.Encryptor
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
import javax.inject.Inject

class WriteCustomerOnTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
    private val encryptor: Encryptor,
) {
    suspend operator fun invoke(
        customer: Customer,
        intent: Intent,
    ) {
        val serializedCustomer = customerSerializer.serialize(customer)
        val encryptedPayload = encryptor.encrypt(serializedCustomer)

        tagRepository.write(
            payload = encryptedPayload,
            intent = intent,
        )
    }
}
