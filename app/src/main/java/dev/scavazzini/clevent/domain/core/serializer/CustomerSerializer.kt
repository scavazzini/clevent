package dev.scavazzini.clevent.domain.core.serializer

import dev.scavazzini.clevent.data.models.Customer

interface CustomerSerializer {
    fun serialize(customer: Customer): ByteArray
    fun deserialize(data: ByteArray): Customer
}
