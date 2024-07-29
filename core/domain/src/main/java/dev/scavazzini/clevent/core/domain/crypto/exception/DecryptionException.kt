package dev.scavazzini.clevent.core.domain.crypto.exception

class DecryptionException(
    override val message: String,
) : Exception(message)
