package com.nobody.campick.views.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nobody.campick.resources.theme.AppColors
import kotlin.math.*

@Composable
fun RangeSlider(
    range: ClosedFloatingPointRange<Double>,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double,
    onRangeChange: (ClosedFloatingPointRange<Double>) -> Unit,
    modifier: Modifier = Modifier
) {
    var lowerValue by remember { mutableDoubleStateOf(range.start) }
    var upperValue by remember { mutableDoubleStateOf(range.endInclusive) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val thumbRadius = with(density) { 10.dp.toPx() }
    val trackHeight = with(density) { 4.dp.toPx() }

    LaunchedEffect(range) {
        lowerValue = range.start
        upperValue = range.endInclusive
    }

    Box(
        modifier = modifier
            .height(20.dp)
            .fillMaxWidth()
            .onSizeChanged { size = it }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        val width = size.width.toFloat()
                        val percent = (x / width).coerceIn(0f, 1f)
                        val newValue = bounds.start + percent * (bounds.endInclusive - bounds.start)
                        val steppedValue = round(newValue / step) * step

                        // Determine which thumb is closer
                        val lowerPercent = (lowerValue - bounds.start) / (bounds.endInclusive - bounds.start)
                        val upperPercent = (upperValue - bounds.start) / (bounds.endInclusive - bounds.start)
                        val lowerDistance = abs(percent - lowerPercent)
                        val upperDistance = abs(percent - upperPercent)

                        if (lowerDistance < upperDistance) {
                            // Move lower thumb
                            lowerValue = minOf(steppedValue, upperValue).coerceIn(bounds.start, bounds.endInclusive)
                        } else {
                            // Move upper thumb
                            upperValue = maxOf(steppedValue, lowerValue).coerceIn(bounds.start, bounds.endInclusive)
                        }

                        onRangeChange(lowerValue..upperValue)
                    }
                }
        ) {
            val width = size.width.toFloat()
            val height = size.height.toFloat()
            val centerY = height / 2f

            val lowerPercent = ((lowerValue - bounds.start) / (bounds.endInclusive - bounds.start)).toFloat()
            val upperPercent = ((upperValue - bounds.start) / (bounds.endInclusive - bounds.start)).toFloat()

            val lowerX = lowerPercent * width
            val upperX = upperPercent * width

            drawLine(
                color = AppColors.brandWhite20,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            drawLine(
                color = AppColors.brandOrange,
                start = Offset(lowerX, centerY),
                end = Offset(upperX, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = AppColors.brandOrange,
                radius = thumbRadius,
                center = Offset(lowerX, centerY)
            )

            drawCircle(
                color = AppColors.background,
                radius = thumbRadius,
                center = Offset(lowerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = AppColors.brandOrange,
                radius = thumbRadius,
                center = Offset(upperX, centerY)
            )

            drawCircle(
                color = AppColors.background,
                radius = thumbRadius,
                center = Offset(upperX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }
    }
}