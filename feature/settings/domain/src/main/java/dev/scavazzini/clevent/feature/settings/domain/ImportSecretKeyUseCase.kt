package dev.scavazzini.clevent.feature.settings.domain

import dev.scavazzini.clevent.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class ImportSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(keyBytes: ByteArray): SecretKey {
        return secretKeyManager.importKey(keyBytes)
    }

}
