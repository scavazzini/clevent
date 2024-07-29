package dev.scavazzini.clevent.core.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer

@Composable
fun OnNewIntentHandler(onNewIntent: (Intent) -> Unit) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findComponentActivity()

        val listener = Consumer<Intent> { onNewIntent(it) }
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
        "OnNewIntentHandler should be called in the context of a ComponentActivity",
    )
}
