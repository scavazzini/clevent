package dev.scavazzini.clevent.domain.settings

import dev.scavazzini.clevent.domain.core.crypto.KeyInfo
import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import javax.inject.Inject

class GetSecretKeyInfoUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(): KeyInfo? {
        return secretKeyManager.getKeyInfo()
    }
}
