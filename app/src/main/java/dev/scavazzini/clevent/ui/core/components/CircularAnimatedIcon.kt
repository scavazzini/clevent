package dev.scavazzini.clevent.ui.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
internal fun CircularAnimatedIcon(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit = { },
) {
    BoxWithConstraints(modifier.size(80.dp)) {

        val degrees by rememberInfiniteTransition(label = "degrees").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            label = "CircularAnimatedIcon",
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 750,
                    easing = LinearEasing
                )
            ),
        )

        val defaultColor = MaterialTheme.colorScheme.primary

        val gradientBrush by remember(colors) {
            val brush = if (colors.size > 1) {
                Brush.horizontalGradient(
                    colors = colors,
                    startX = 0f,
                    endX = constraints.maxWidth.toFloat(),
                    tileMode = TileMode.Repeated,
                )
            } else {
                SolidColor(
                    value = colors.getOrElse(
                        index = 0,
                        defaultValue = { defaultColor },
                    ),
                )
            }

            mutableStateOf(brush)
        }

        val drawBehind: DrawScope.() -> Unit = {
            rotate(degrees) {
                drawCircle(
                    brush = gradientBrush,
                )
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 2 - borderWidth.toPx(),
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind(drawBehind),
            content = content,
        )
    }
}

@Preview
@Composable
private fun CircularIconColorfulPreview() {
    val colors = listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
    )

    CircularAnimatedIcon(
        colors = colors,
        modifier = Modifier.size(120.dp),
    ) {
        Text(
            text = "CircularIconColorfulPreview",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun CircularIconGrayscalePreview() {
    val colors = listOf(
        Color.Black,
        Color.DarkGray,
        Color.Gray,
        Color.LightGray,
    )

    CircularAnimatedIcon(colors)
}

@Preview
@Composable
private fun CircularIconEmptyListOfColorsPreview() {
    CircularAnimatedIcon(colors = emptyList())
}
