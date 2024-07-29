package dev.scavazzini.clevent.core.domain.serializer

import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.model.Product
import dev.scavazzini.clevent.core.domain.serializer.exception.DeserializationException
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import javax.inject.Inject

/**
 *
 *   Serialized Customer:
 * ```
 *   +---------------------+-----------------------------------+---------------------+
 *   | 0xFF 0xFF 0xFF 0xFF | 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF ... | 0xFF 0xFF 0xFF 0xFF |
 *   +---------------------+-----------------------------------+---------------------+
 *     Region 1 (Balance)          Region 2 (Item list)            Region 3 (CRC)
 * ```
 *   - Region 1 (4 bytes): Balance as an integer value
 *   - Region 2 (3 bytes per item): Each item uses 3 bytes, 2 for item ID and 1 for quantity.
 *   - Region 3 (4 bytes): CRC as an integer value
 *
 */
class CustomerNFCSerializer @Inject constructor(private val crc: CRC32) : CustomerSerializer {

    override fun serialize(customer: Customer): ByteArray {
        val outputStream = ByteArrayOutputStream()
        DataOutputStream(outputStream).use { dataOutputStream ->
            dataOutputStream.writeInt(customer.balance)

            for ((product, quantity) in customer.products) {
                var remainingToAdd = quantity

                while (remainingToAdd > 0) {
                    val toBeAdded = remainingToAdd.coerceAtMost(Byte.MAX_VALUE.toInt())
                    dataOutputStream.writeShort(product.id.toInt())
                    dataOutputStream.writeByte(toBeAdded)
                    remainingToAdd -= toBeAdded
                }
            }

            val crcValue = outputStream.toByteArray().calculateCrc()
            dataOutputStream.writeInt(crcValue)
        }
        return outputStream.toByteArray()
    }

    private fun ByteArray.calculateCrc(): Int {
        crc.update(this)
        val crcValue = crc.value.toInt()
        crc.reset()

        return crcValue
    }

    @Throws(DeserializationException::class)
    override fun deserialize(data: ByteArray): Customer {

        if (data.size < 8 || data.isCorrupted()) {
            throw DeserializationException()
        }

        val byteBuffer = ByteBuffer.wrap(data.getPayloadBytes())

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

        return Customer(balance, products)
    }

    private fun ByteArray.isCorrupted(): Boolean {
        val calculatedCrc = getPayloadBytes().calculateCrc()
        val receivedCrc = ByteBuffer.wrap(getCrcBytes()).getInt()

        return calculatedCrc != receivedCrc
    }

    private fun ByteArray.getCrcBytes(): ByteArray {
        return takeLast(Int.SIZE_BYTES).toByteArray()
    }

    private fun ByteArray.getPayloadBytes(): ByteArray {
        return sliceArray(0 until size - Int.SIZE_BYTES)
    }

}
