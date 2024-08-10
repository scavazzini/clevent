package dev.scavazzini.clevent.nfc

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.nfc.NfcAdapter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer

private val NFC_INTENT_ACTIONS = arrayOf(
    NfcAdapter.ACTION_NDEF_DISCOVERED,
    NfcAdapter.ACTION_TECH_DISCOVERED,
    NfcAdapter.ACTION_TAG_DISCOVERED,
)

@Composable
fun OnTagDiscoveredHandler(onTagDiscovered: (Intent) -> Unit) {
    val context = LocalContext.current

    DisposableEffect(onTagDiscovered) {
        val activity = context.findComponentActivity()

        val listener = Consumer<Intent> {
            if (it.action in NFC_INTENT_ACTIONS) {
                onTagDiscovered(it)
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose { activity.removeOnNewIntentListener(listener) }
    }
}

private fun Context.findComponentActivity(): ComponentActivity {
    var context = this

    while (context is ContextWrapper) {
        if (context is ComponentActivity) {
            return context
        }
        context = context.baseContext
    }

    throw IllegalStateException(
        "OnTagDiscoveredHandler should be called in the context of a ComponentActivity",
    )
}
