package dev.scavazzini.clevent.domain.core.serializer

import dev.scavazzini.clevent.data.core.model.Customer

interface CustomerSerializer {
    fun serialize(customer: Customer): ByteArray
    fun deserialize(data: ByteArray): Customer
}
