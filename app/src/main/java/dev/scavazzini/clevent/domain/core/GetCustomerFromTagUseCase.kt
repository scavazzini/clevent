package dev.scavazzini.clevent.domain.core

import android.content.Intent
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.repositories.TagRepository
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
import javax.inject.Inject

class GetCustomerFromTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
) {
    operator fun invoke(intent: Intent): Customer {
        val payload = tagRepository.read(intent)

        if (payload.all { it == 0x0.toByte() }) {
            return Customer()
        }

        return customerSerializer.deserialize(payload)
    }
}
