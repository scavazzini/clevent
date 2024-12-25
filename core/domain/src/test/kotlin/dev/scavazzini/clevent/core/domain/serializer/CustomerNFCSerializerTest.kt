package dev.scavazzini.clevent.core.domain.serializer

import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.model.Product
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CustomerNFCSerializerTest {

    private val customer: Customer = Customer()
    private val product1 = Product(1.toShort(), "p1", 2500, "cat1")
    private val product2 = Product(50.toShort(), "p2", 1000, "cat2")

    private val expectedSerializedBalance = byteArrayOf(0x0, 0x0, 0x1D, 0x4C)
    private val expectedSerializedProducts = byteArrayOf(0x0, 0x1, 0x3, 0x0, 0x32, 0xF)
    private val expectedSerializedCrc = byteArrayOf(0x0, 0x0, 0x4, 0xD2.toByte())
    private val expectedSerializedCustomer = expectedSerializedBalance + expectedSerializedProducts + expectedSerializedCrc

    private val customerSerializer = CustomerNFCSerializer(crcCalculator = FakeCrcCalculator())

    @Before
    fun setUp() {
        customer.recharge(30000)
        customer.addProduct(product1, 3)
        customer.addProduct(product2, 15)
    }

    @Test
    fun shouldSerializeCustomer() {
        val serializedCustomer = customerSerializer.serialize(customer)

        assertArrayEquals(serializedCustomer, expectedSerializedCustomer)
    }

    @Test
    fun shouldDeserializeCustomer() {
        val deserializedCustomer = customerSerializer.deserialize(expectedSerializedCustomer)

        assertEquals(deserializedCustomer, customer)
    }

    @Test
    fun shouldSerializeAndDeserializeCustomerWithALotOfProducts() {
        val customer = Customer()
        customer.recharge(12500000)
        customer.addProduct(product1, 5000)

        val serializedCustomer = customerSerializer.serialize(customer)
        val deserializedCustomer = customerSerializer.deserialize(serializedCustomer)

        assertEquals(customer.balance, deserializedCustomer.balance)
        assertEquals(5000, deserializedCustomer.products.values.sum())
    }
}
