package dev.scavazzini.clevent.domain.core

import android.content.Intent
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.repository.TagRepository
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
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
