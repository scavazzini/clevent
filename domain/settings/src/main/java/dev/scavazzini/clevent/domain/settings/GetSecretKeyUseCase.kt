package dev.scavazzini.clevent.domain.settings

import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class GetSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(): SecretKey? {
        return secretKeyManager.getKey()
    }
}
