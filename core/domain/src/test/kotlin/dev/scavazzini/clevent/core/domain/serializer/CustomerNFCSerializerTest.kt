package dev.scavazzini.clevent.core.domain.serializer

import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.model.Product
import dev.scavazzini.clevent.core.domain.serializer.exception.DeserializationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer

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

    @Test(expected = DeserializationException::class)
    fun shouldThrowDeserializationExceptionIfDataHasLessThan4Bytes() {
        customerSerializer.deserialize(data = byteArrayOf(0xF, 0x0, 0x5))
    }

    @Test(expected = DeserializationException::class)
    fun shouldThrowDeserializationExceptionIfCrcMismatches() {
        val customerSerializer = CustomerNFCSerializer(
            crcCalculator = FakeCrcCalculator(matchResult = false),
        )
        customerSerializer.deserialize(expectedSerializedCustomer)
    }

    @Test
    fun shouldNotDeserializeProductsWithNegativeIds() {
        // Creates a serialized Customer with a positive balance and two products,
        // however one of them has an invalid ID that should be ignored from deserialization.
        val positiveBalance = ByteBuffer.allocate(4).putInt(50000).array()
        val validProduct = ByteBuffer.allocate(3).putShort(20).put(3).array()
        val invalidProduct = ByteBuffer.allocate(3).putShort(-10).put(1).array()
        val crc = ByteBuffer.allocate(4).putInt(1234).array()

        val customer = customerSerializer.deserialize(
            data = positiveBalance + validProduct + invalidProduct + crc
        )

        // Assert that only the valid product were deserialized
        assertEquals(1, customer.products.size)
        assertEquals(20.toShort(), customer.products.keys.first().id)
        assertEquals(3, customer.products.values.first())
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
