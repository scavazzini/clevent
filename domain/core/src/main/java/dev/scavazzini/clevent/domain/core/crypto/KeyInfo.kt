package dev.scavazzini.clevent.domain.core.crypto

data class KeyInfo(
    val id: String,
    val algorithm: String,
    val size: Int,
)
