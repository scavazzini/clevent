package dev.scavazzini.clevent.core.domain.crypto

data class KeyInfo(
    val id: String,
    val algorithm: String,
    val size: Int,
)
