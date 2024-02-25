package dev.scavazzini.clevent.utilities.extensions

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun Date.formatted(): String {
    val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT, Locale.getDefault(),
    )
    return dateFormat.format(time)
}
