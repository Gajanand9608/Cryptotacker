package com.learning.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learning.cryptotracker.crypto.domain.CoinPrice
import com.learning.cryptotracker.ui.theme.CryptoTrackerTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun LineChart(
    dataPoints: List<DataPoint>,
    style: ChartStyle,
    visibleDataPointIndices: IntRange,
    unit: String,
    modifier: Modifier = Modifier,
    selectedDataPoint: DataPoint? = null,
    onSelectedDataPoint: (DataPoint?) -> Unit = {},
    onXLabelWidthChange: (Float) -> Unit = {},
    showHelperLines: Boolean = true
) {
    val textStyle = LocalTextStyle.current.copy(
        fontSize = style.labelFontSize
    )

    val visibleDataPoints = remember(dataPoints, visibleDataPointIndices) {
        dataPoints.slice(visibleDataPointIndices)
    }

    val maxYValue = remember(visibleDataPoints) {
        visibleDataPoints.maxOfOrNull { it.y } ?: 0f
    }

    val minYValue = remember(visibleDataPoints) {
        visibleDataPoints.minOfOrNull { it.y } ?: 0f
    }

    val measurer = rememberTextMeasurer()
    var xLabelWidth by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(xLabelWidth) {
        onXLabelWidthChange(xLabelWidth)
    }

    val selectedDataPointIndex = remember(selectedDataPoint) {
        visibleDataPoints.indexOf(selectedDataPoint)
    }

    var drawPoints by remember {
        mutableStateOf(listOf<DataPoint>())
    }

    var isShowingDataPoints by remember {
        mutableStateOf(selectedDataPoint != null)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val minLabelSpacingYPx = style.minYLabelSpacing.toPx()
        val verticalPaddingPx = style.verticalPadding.toPx()
        val horizontalPaddingPx = style.horizontalPadding.toPx()
        val xAxisLabelSpacingPx = style.xAxisLabelSpacing.toPx()

        val xLabelTextLayoutResults = visibleDataPoints.map {
            measurer.measure(it.xLabel, style = textStyle.copy(textAlign = TextAlign.Center))
        }

        val maxXLabelWidth = xLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0
        val maxXLabelHeight = xLabelTextLayoutResults.maxOfOrNull { it.size.height } ?: 0
        val maxXLabelLineCount = xLabelTextLayoutResults.maxOfOrNull { it.lineCount } ?: 0
        val xLabelLineHeight = maxXLabelHeight / maxXLabelLineCount


        val viewPortHeightPx =
            size.height - (maxXLabelHeight + 2 * verticalPaddingPx + xLabelLineHeight + xAxisLabelSpacingPx)

        // y label calculation
        val labelViewPortHeightPx = viewPortHeightPx + xLabelLineHeight
        val labelCountExcludingLastLabel =
            (labelViewPortHeightPx / (xLabelLineHeight + minLabelSpacingYPx)).toInt()
        val valueIncrement = (maxYValue - minYValue) / labelCountExcludingLastLabel
        val yLabels = (0..labelCountExcludingLastLabel).map {
            ValueLabel(value = maxYValue - valueIncrement * it, unit = unit)
        }
        val yLabelTextLayoutResults = yLabels.map {
            measurer.measure(text = it.formatted(), style = textStyle)
        }
        val maxYLabelWidth = yLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0


        val viewPortTopY = xLabelLineHeight + verticalPaddingPx + 10f
        val viewPortRightX = size.width
        val viewPortBottomY = viewPortTopY + viewPortHeightPx
        val viewPortLeftX = 2f * horizontalPaddingPx + maxYLabelWidth
        val viewPort = Rect(
            left = viewPortLeftX,
            top = viewPortTopY,
            right = viewPortRightX,
            bottom = viewPortBottomY

        )

        drawRect(color = Color.Green.copy(alpha = 0.3f), topLeft = viewPort.topLeft, size = viewPort.size)

        xLabelWidth = maxXLabelWidth + xAxisLabelSpacingPx
        xLabelTextLayoutResults.forEachIndexed { index, textLayoutResult ->
            val x = viewPortLeftX + xAxisLabelSpacingPx / 2f + xLabelWidth * index
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x,
                    y = viewPortBottomY + xAxisLabelSpacingPx
                ),
                color = if (index == selectedDataPointIndex) style.selectedColor else style.unselectedColor
            )
            if (showHelperLines) {
                drawLine(
                    color = if(index == selectedDataPointIndex) style.selectedColor else style.unselectedColor,
                    start = Offset(x = x + textLayoutResult.size.width/2f, y = viewPortTopY),
                    end = Offset(x = x + textLayoutResult.size.width/2f, y = viewPortBottomY),
                    strokeWidth = if(index == selectedDataPointIndex) style.helperLinesThicknessPx * 1.8f else style.helperLinesThicknessPx
                )
            }
            if (index == selectedDataPointIndex) {
                val valueLabel = ValueLabel(value = visibleDataPoints[index].y, unit = unit)
                val valueResult = measurer.measure(text = valueLabel.formatted(), style = textStyle.copy(color = style.selectedColor), maxLines = 1)
                val textPositionX = if(selectedDataPointIndex == visibleDataPointIndices.last){
                    x - textLayoutResult.size.width
                }else{
                    x - textLayoutResult.size.width/2f
                }  + textLayoutResult.size.width/2f
                val isTextInVisibleRange = (size.width - textPositionX).roundToInt() in 0..size.width.roundToInt()
                if(isTextInVisibleRange){
                    drawText(
                        textLayoutResult = valueResult, topLeft = Offset(x = textPositionX,y = viewPortTopY - valueResult.size.height - 10f)
                    )
                }
            }
        }


        val heightRequiredForLabels = xLabelLineHeight * (labelCountExcludingLastLabel + 1)
        val remainingHeightForLabels = labelViewPortHeightPx - heightRequiredForLabels
        val spaceBetweenLabels = remainingHeightForLabels / labelCountExcludingLastLabel
        yLabelTextLayoutResults.forEachIndexed { index, textLayoutResult ->
            val x = horizontalPaddingPx + maxYLabelWidth - textLayoutResult.size.width.toFloat()
            val y =
                viewPortTopY + (xLabelLineHeight + spaceBetweenLabels) * index - xLabelLineHeight / 2f
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x = x, y = y),
                color = style.unselectedColor
            )
            if (showHelperLines) {
                drawLine(
                    color = style.unselectedColor,
                    start = Offset(x = viewPortLeftX, y = y + textLayoutResult.size.height / 2),
                    end = Offset(x = viewPortRightX, y = y + textLayoutResult.size.height / 2),
                    strokeWidth = style.helperLinesThicknessPx
                )
            }
        }
    }
}

@Preview(widthDp = 2000)
@Composable
fun LineChartPreview() {
    CryptoTrackerTheme {
        val coinHistoryRandomized = remember {
            (1..20).map { it ->
                CoinPrice(
                    priceUsd = Random.nextFloat() * 1000.0,
                    dateTime = ZonedDateTime.now().plusHours(
                        it.toLong()
                    )
                )
            }
        }
        val style = ChartStyle(
            chartLineColor = Color.Black,
            unselectedColor = Color(0xff7c7c7c),
            selectedColor = Color.Black,
            helperLinesThicknessPx = 1f,
            axisLinesThickness = 5f,
            labelFontSize = 14.sp,
            minYLabelSpacing = 25.dp,
            verticalPadding = 8.dp,
            horizontalPadding = 8.dp,
            xAxisLabelSpacing = 8.dp
        )
        val dataPoints = remember {
            coinHistoryRandomized.map {
                DataPoint(
                    x = it.dateTime.toEpochSecond().toFloat(),
                    y = it.priceUsd.toFloat(),
                    xLabel = DateTimeFormatter.ofPattern("ha\nM/d").format(it.dateTime)
                )
            }
        }
        LineChart(
            dataPoints = dataPoints,
            style = style,
            visibleDataPointIndices = 0..19,
            unit = "$",
            modifier = Modifier
                .width(700.dp)
                .height(300.dp)
                .background(Color.White),
            selectedDataPoint = dataPoints[1]
        )
    }
}