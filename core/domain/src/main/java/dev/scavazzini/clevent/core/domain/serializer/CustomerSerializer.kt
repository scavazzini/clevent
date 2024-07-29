package dev.scavazzini.clevent.core.domain.serializer

import dev.scavazzini.clevent.core.data.model.Customer

interface CustomerSerializer {
    fun serialize(customer: Customer): ByteArray
    fun deserialize(data: ByteArray): Customer
}
