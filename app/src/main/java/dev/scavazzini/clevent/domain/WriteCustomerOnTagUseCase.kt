package dev.scavazzini.clevent.domain

import android.content.Intent
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.repositories.TagRepository
import dev.scavazzini.clevent.domain.serializer.CustomerSerializer
import javax.inject.Inject

class WriteCustomerOnTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
    private val customerSerializer: CustomerSerializer,
) {
    suspend operator fun invoke(
        customer: Customer,
        intent: Intent,
    ) {
        tagRepository.write(
            payload = customerSerializer.serialize(customer),
            intent = intent,
        )
    }
}
