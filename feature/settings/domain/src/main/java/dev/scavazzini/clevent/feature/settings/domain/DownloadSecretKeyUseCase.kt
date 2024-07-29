package dev.scavazzini.clevent.feature.settings.domain

import dev.scavazzini.clevent.core.domain.crypto.SecretKeyManager
import javax.crypto.SecretKey
import javax.inject.Inject

class DownloadSecretKeyUseCase @Inject constructor(
    private val secretKeyManager: SecretKeyManager,
) {
    suspend operator fun invoke(): SecretKey? {
        return secretKeyManager.getKey()
    }
}
