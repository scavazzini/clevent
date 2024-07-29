package dev.scavazzini.clevent.feature.settings.domain

import dev.scavazzini.clevent.crypto.KeyInfo
import dev.scavazzini.clevent.crypto.SecretKeyManager
import javax.inject.Inject

class GetSecretKeyInfoUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(): KeyInfo? {
        return secretKeyManager.getKeyInfo()
    }
}
