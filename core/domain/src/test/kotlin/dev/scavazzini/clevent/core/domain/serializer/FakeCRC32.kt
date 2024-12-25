package dev.scavazzini.clevent.core.domain.serializer

class FakeCrcCalculator : CrcCalculator {
    override val sizeInBytes: Int = 4

    override fun calculate(payload: ByteArray) = 1234.toUInt()
    override fun matches(payload: ByteArray, crc: ByteArray) = true
    override fun matches(payload: ByteArray, crc: UInt) = true
}
