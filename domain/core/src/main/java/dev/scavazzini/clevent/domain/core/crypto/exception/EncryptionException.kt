package dev.scavazzini.clevent.domain.core.crypto.exception

class EncryptionException(
    override val message: String,
) : Exception(message)
