package dev.scavazzini.clevent.core.domain.serializer

class FakeCrcCalculator(
    private val crcResult: UInt = 1234.toUInt(),
    private val matchResult: Boolean = true,
) : CrcCalculator {
    override val sizeInBytes: Int = 4

    override fun calculate(payload: ByteArray) = crcResult
    override fun matches(payload: ByteArray, crc: ByteArray) = matchResult
    override fun matches(payload: ByteArray, crc: UInt) = matchResult
}
