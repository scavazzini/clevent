package dev.scavazzini.clevent.domain.core.crypto.exception

class DecryptionException(
    override val message: String,
) : Exception(message)
