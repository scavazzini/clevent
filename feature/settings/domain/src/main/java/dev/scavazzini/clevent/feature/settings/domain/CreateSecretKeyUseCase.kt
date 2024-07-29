package dev.scavazzini.clevent.feature.settings.domain

import dev.scavazzini.clevent.core.domain.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class CreateSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(): SecretKey {
        return secretKeyManager.createKey()
    }
}
