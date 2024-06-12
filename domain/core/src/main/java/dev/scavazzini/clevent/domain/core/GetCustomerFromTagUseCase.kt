package dev.scavazzini.clevent.domain.core

import android.content.Intent
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.repository.TagRepository
import dev.scavazzini.clevent.domain.core.crypto.SymmetricEncryptor
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
import javax.inject.Inject

class GetCustomerFromTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
    private val encryptor: SymmetricEncryptor,
) {
    suspend operator fun invoke(intent: Intent): Customer {
        val payload = tagRepository.read(intent)

        if (payload.all { it == 0x0.toByte() }) {
            return Customer()
        }

        val decryptedPayload = encryptor.decrypt(payload)
        return customerSerializer.deserialize(decryptedPayload)
    }
}
