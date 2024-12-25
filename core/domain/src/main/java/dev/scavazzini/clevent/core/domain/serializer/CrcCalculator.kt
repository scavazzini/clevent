package dev.scavazzini.clevent.core.domain.serializer

interface CrcCalculator {
    val sizeInBytes: Int

    fun calculate(payload: ByteArray): UInt
    fun matches(payload: ByteArray, crc: ByteArray): Boolean
    fun matches(payload: ByteArray, crc: UInt): Boolean
}
