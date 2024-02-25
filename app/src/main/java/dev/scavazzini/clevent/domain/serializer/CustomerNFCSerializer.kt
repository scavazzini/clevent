package dev.scavazzini.clevent.domain.serializer

import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.domain.serializer.exception.DeserializationException
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.ceil

/**
 *
 *   Serialized Customer:
 * ```
 *   +-----------+---------------------+----------------------------------
 *   | 0xFF 0xFF | 0xFF 0xFF 0xFF 0xFF | 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF ...
 *   +-----------+---------------------+----------------------------------
 *     Region 1         Region 2                    Region 3
 *    (Checksum)       (Balance)                  (Item list)
 * ```
 *   - Region 1 (2 bytes): 1 byte for balance checksum and 1 byte for item list checksum.
 *   - Region 2 (4 bytes): Balance as an integer
 *   - Region 3 (3 bytes per item): Each item uses 3B, 2B for item ID (short) and 1B for quantity.
 *
 */
class CustomerNFCSerializer @Inject constructor() : CustomerSerializer {

    override fun serialize(customer: Customer): ByteArray {
        val productEntriesQuantity = customer.products.map {
            ceil(it.value / Byte.MAX_VALUE.toDouble()).toInt() }.sum()

        val outputStream = ByteArrayOutputStream()
        DataOutputStream(outputStream).use { dataOutputStream ->
            dataOutputStream.write(customer.balance % 0xFF)
            dataOutputStream.write(productEntriesQuantity % 0xFF)
            dataOutputStream.writeInt(customer.balance)

            for ((product, quantity) in customer.products) {
                var remainingToAdd = quantity

                while (remainingToAdd > 0) {
                    val toBeAdded = if (remainingToAdd > Byte.MAX_VALUE) Byte.MAX_VALUE.toInt() else remainingToAdd
                    dataOutputStream.writeShort(product.id.toInt())
                    dataOutputStream.writeByte(toBeAdded)
                    remainingToAdd -= toBeAdded
                }
            }
        }
        return outputStream.toByteArray()
    }

    @Throws(DeserializationException::class)
    override fun deserialize(data: ByteArray): Customer {

        if (data.size < 6) throw DeserializationException()

        val byteBuffer = ByteBuffer.wrap(data)

        val balanceChecksum = byteBuffer.get()
        val productsChecksum = byteBuffer.get()
        val balance = byteBuffer.int

        var productEntriesQuantity = 0
        val products: MutableMap<Product, Int> = mutableMapOf()

        while (byteBuffer.remaining() >= 3) {
            val productId = byteBuffer.short
            val quantity = byteBuffer.get().toInt()
            if (productId > 0) {
                val product = Product(productId, "", 0, "")
                products[product] = products[product]?.plus(quantity) ?: quantity
                productEntriesQuantity++
            }
        }

        val balanceChecksumMatched = (balance % 0xFF).toByte() == balanceChecksum
        val productsChecksumMatched = (productEntriesQuantity % 0xFF).toByte() == productsChecksum

        if (!balanceChecksumMatched || !productsChecksumMatched) {
            throw DeserializationException()
        }

        return Customer(balance, products)
    }

}
