package dev.scavazzini.clevent.domain.settings

import android.security.keystore.KeyInfo
import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class GetSecretKeyInfoUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(secretKey: SecretKey): KeyInfo? {
        return secretKeyManager.getKeyInfo(secretKey)
    }
}
