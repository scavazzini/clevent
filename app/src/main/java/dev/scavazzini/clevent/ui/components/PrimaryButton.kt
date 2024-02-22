package dev.scavazzini.clevent.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.ui.components.PrimaryButtonState.DISABLED
import dev.scavazzini.clevent.ui.components.PrimaryButtonState.ENABLED
import dev.scavazzini.clevent.ui.components.PrimaryButtonState.LOADING
import dev.scavazzini.clevent.ui.theme.CleventTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: PrimaryButtonState = ENABLED,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = modifier.height(48.dp),
        colors = colors,
        enabled = when (state) {
            ENABLED -> true
            else -> false
        },
    ) {
        if (state == LOADING) {
            return@Button CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (text.isNotBlank()) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

enum class PrimaryButtonState {
    ENABLED,
    DISABLED,
    LOADING,
}

@Preview("Enabled")
@Composable
private fun PrimaryButtonEnabledPreview() {
    CleventTheme {
        PrimaryButton(
            text = "Place order",
            onClick = { },
            state = ENABLED,
        )
    }
}

@Preview("Disabled")
@Composable
private fun PrimaryButtonDisabledPreview() {
    CleventTheme {
        PrimaryButton(
            text = "Place order",
            onClick = { },
            state = DISABLED,
        )
    }
}

@Preview("Loading with text")
@Composable
private fun PrimaryButtonLoadingPreview() {
    CleventTheme {
        PrimaryButton(
            text = "Placing order",
            onClick = { },
            state = LOADING,
        )
    }
}

@Preview("Loading without text")
@Composable
private fun PrimaryButtonBlankTextPreview() {
    CleventTheme {
        PrimaryButton(
            text = "",
            onClick = { },
        )
    }
}
