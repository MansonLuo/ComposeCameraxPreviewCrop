package com.example.composecameraxpreviewcrop.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * MaskedView(
 *         startPoint = Offset(350f, 350f),
 *         endPoint = Offset(600f, 600f),
 *         cornerRadius = 17.dp,
 *         maskColor = Color.Gray.copy(alpha = 0.5f)
 *     )
 */

@Composable
fun MaskedView(
    startPoint: Offset,
    endPoint: Offset,
    maskColor: Color
) {

    val lineWidthPx = with(LocalDensity.current) {
        1.dp.toPx()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val revealedPath = Path().apply {
                val rect = Rect(
                    left = startPoint.x,
                    top = startPoint.y,
                    right = endPoint.x,
                    bottom = endPoint.y,
                )

                addRect(rect)
            }

            val maskPath = Path().apply {
                addRect(size.toRect())
                op(this, revealedPath, PathOperation.Difference)
            }

            drawPath(
                path = maskPath,
                color = maskColor
            )

            // draw crop box stroke line
            val cropBoxLinePath = Path().apply {
                val rect = Rect(
                    left = startPoint.x - lineWidthPx,
                    top = startPoint.y - lineWidthPx,
                    right = endPoint.x + lineWidthPx,
                    bottom = endPoint.y + lineWidthPx
                )

                addRect(rect)
            }
            drawPath(
                path = cropBoxLinePath,
                color = Color.Red,
                style = Stroke(width = lineWidthPx)
            )
        }
    }
}