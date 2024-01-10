package dev.scavazzini.clevent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.scavazzini.clevent.ui.components.PrimaryButtonState.*

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: PrimaryButtonState = ENABLED,
) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = when (state) {
            ENABLED -> true
            else -> false
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state == LOADING) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(
                    modifier = Modifier.width(16.dp),
                )
            }
            if (text.isNotBlank()) {
                Text(
                    text = text.uppercase(),
                    fontSize = 16.sp,
                )
            }
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
fun PrimaryButtonEnabledPreview() {
    PrimaryButton(
        text = "Place order",
        onClick = { },
        state = ENABLED,
    )
}

@Preview("Disabled")
@Composable
fun PrimaryButtonDisabledPreview() {
    PrimaryButton(
        text = "Place order",
        onClick = { },
        state = DISABLED,
    )
}

@Preview("Loading with text")
@Composable
fun PrimaryButtonLoadingWithTextPreview() {
    PrimaryButton(
        text = "Placing order",
        onClick = { },
        state = LOADING,
    )
}

@Preview("Loading without text")
@Composable
fun PrimaryButtonLoadingWithoutTextPreview() {
    PrimaryButton(
        text = "",
        onClick = { },
        state = LOADING,
    )
}
