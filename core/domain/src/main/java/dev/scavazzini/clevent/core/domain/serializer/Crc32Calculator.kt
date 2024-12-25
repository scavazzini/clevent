package dev.scavazzini.clevent.core.domain.serializer

import java.nio.ByteBuffer
import java.util.zip.CRC32
import javax.inject.Inject

/**
 * Calculates CRC using ISO-HDLC algorithm.
 */
class Crc32Calculator @Inject constructor() : CrcCalculator {
    override val sizeInBytes: Int = 4

    override fun calculate(payload: ByteArray): UInt {
        return with (CRC32()) {
            update(payload)
            value.toUInt()
        }
    }

    override fun matches(payload: ByteArray, crc: ByteArray): Boolean {
        return matches(
            payload = payload,
            crc = ByteBuffer.wrap(crc).int.toUInt(),
        )
    }

    override fun matches(payload: ByteArray, crc: UInt): Boolean {
        return calculate(payload) == crc
    }
}
