package dev.scavazzini.clevent.feature.settings.domain

import dev.scavazzini.clevent.core.domain.crypto.SecretKeyManager
import javax.inject.Inject

class DeleteSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke() {
        return secretKeyManager.clearKey()
    }
}
