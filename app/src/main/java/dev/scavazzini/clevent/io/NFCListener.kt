package dev.scavazzini.clevent.io

import android.nfc.Tag
import dev.scavazzini.clevent.data.models.Customer

interface NFCListener {
    fun onTagRead(tag: Tag, customer: Customer)
    fun onInvalidTagRead()
}
