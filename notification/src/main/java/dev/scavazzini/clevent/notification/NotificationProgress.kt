package dev.scavazzini.clevent.notification

interface NotificationProgress {
    val max: Int
    val progress: Int
    val indefinite: Boolean

    data object Indefinite : NotificationProgress {
        override val max = 0
        override val progress = 0
        override val indefinite = true
    }

    data class Definite(
        override val max: Int,
        override val progress: Int,
    ) : NotificationProgress {
        override val indefinite = false
    }
}
