package dev.scavazzini.clevent.core.domain.crypto.exception

class EncryptionException(
    override val message: String,
) : Exception(message)
