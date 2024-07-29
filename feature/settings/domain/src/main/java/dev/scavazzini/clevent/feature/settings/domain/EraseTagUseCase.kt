package dev.scavazzini.clevent.feature.settings.domain

import android.content.Intent
import dev.scavazzini.clevent.core.data.repository.TagRepository
import javax.inject.Inject

class EraseTagUseCase @Inject constructor(
    private val tagRepository: TagRepository,
) {
    suspend operator fun invoke(intent: Intent) {
        val tag = tagRepository.getTag(intent)
        tagRepository.erase(tag)
    }
}
