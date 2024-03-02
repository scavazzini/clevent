package dev.scavazzini.clevent.domain.core

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FormatDateToStringUseCase @Inject constructor() {
    operator fun invoke(
        date: Date,
        locale: Locale = Locale.getDefault(),
    ): String {
        val dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT, locale,
        )
        return dateFormat.format(date.time)
    }
}
