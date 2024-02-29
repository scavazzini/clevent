package dev.scavazzini.clevent.ui.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF006D44),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89F8BB),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF376A20),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB7F397),
    onSecondaryContainer = Color(0xFF072100),
    tertiary = Color(0xFF00677D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB2EBFF),
    onTertiaryContainer = Color(0xFF001F27),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A),
    outline = Color(0xFF707972),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF404942),
)

@Composable
fun CleventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorPalette,
        typography = Typography,
        content = content,
    )
}
