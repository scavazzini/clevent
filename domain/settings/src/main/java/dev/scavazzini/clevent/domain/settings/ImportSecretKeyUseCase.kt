package dev.scavazzini.clevent.domain.settings

import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class ImportSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(keyBytes: ByteArray): SecretKey {
        return secretKeyManager.importKey(keyBytes)
    }

}
