package dev.scavazzini.clevent.domain.settings

import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import javax.inject.Inject

class DeleteSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke() {
        return secretKeyManager.clearKey()
    }
}
