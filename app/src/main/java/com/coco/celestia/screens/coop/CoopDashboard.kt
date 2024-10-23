package com.coco.celestia.screens.coop

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
// Add these imports
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CoopDashboard(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(filter = "", role = "Coop")
    }

    Box(modifier = Modifier
        .background(GreenGradientBrush)
        .fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(118.dp))
            OverviewSummaryBox(orderViewModel, productViewModel)
        }
    }
}

@Composable
fun OverviewSummaryBox(orderViewModel: OrderViewModel, productViewModel: ProductViewModel) {
    Box(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(Color.White)) {
        Column {
            Text(
                text = "Overview Summary",
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.padding(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrderStatusDonutChart(orderViewModel)
                    // call the productrends
                }
//                ProductStockTrendsChart(productViewModel)
            }
        }
    }
}

@Composable
fun OrderStatusDonutChart(orderViewModel: OrderViewModel) {
    val orders by orderViewModel.orderData.observeAsState(emptyList())

    val totalOrders = orders.size.toFloat()
    val pending = orders.count { it.status == "PENDING" }
    val preparing = orders.count { it.status == "PREPARING" }
    val completed = orders.count { it.status == "COMPLETED" }

    val pendingPercentage = if (totalOrders > 0) (pending / totalOrders) * 100 else 0f
    val preparingPercentage = if (totalOrders > 0) (preparing / totalOrders) * 100 else 0f
    val completedPercentage = if (totalOrders > 0) (completed / totalOrders) * 100 else 0f

    // Animation states
    var targetPendingProgress by remember { mutableStateOf(0f) }
    var targetPreparingProgress by remember { mutableStateOf(0f) }
    var targetCompletedProgress by remember { mutableStateOf(0f) }

    // Trigger animations
    LaunchedEffect(pendingPercentage, preparingPercentage, completedPercentage) {
        targetPendingProgress = pendingPercentage
        targetPreparingProgress = preparingPercentage
        targetCompletedProgress = completedPercentage
    }

    // Animated values
    val animatedPendingProgress by animateFloatAsState(
        targetValue = targetPendingProgress,
        animationSpec = tween(durationMillis = 750)
    )
    val animatedPreparingProgress by animateFloatAsState(
        targetValue = targetPreparingProgress,
        animationSpec = tween(durationMillis = 750)
    )
    val animatedCompletedProgress by animateFloatAsState(
        targetValue = targetCompletedProgress,
        animationSpec = tween(durationMillis = 750)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
        ) {
            Canvas(modifier = Modifier.size(85.dp)) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2
                val strokeWidth = 30f
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(strokeWidth)
                )

                var startAngle = 0f
                val statusData = listOf(
                    Triple(animatedPendingProgress, PendingStatus, "Pending"),
                    Triple(animatedPreparingProgress, PreparingStatus, "Preparing"),
                    Triple(animatedCompletedProgress, CompletedStatus, "Completed")
                )

                statusData.forEach { (percentage, color, _) ->
                    val sweepAngle = percentage * 3.6f
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )

                    // Calculate position for percentage text
                    val angleInRadians = Math.toRadians(startAngle + (sweepAngle / 2). toDouble())
                    val textRadius = size.width / 2 * 1.7f
                    val x = center.x + (textRadius * cos(angleInRadians)).toFloat()
                    val y = center.y + (textRadius * sin(angleInRadians)).toFloat()

                    // Draw percentage text
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            textSize = 40f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            this.color = color.toArgb()
                        }
                        drawText(
                            "${String.format("%.1f", percentage)}%",
                            x,
                            y,
                            paint
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            // Total orders in center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "${totalOrders.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Text(
                    text = "Total",
                    fontSize = 12.sp,
                    color = DarkGreen
                )
            }
        }
        Text(
            text = "Order Status Breakdown",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}
//
//@Composable
//fun ProductStockTrendsChart(viewModel: ProductViewModel) {
//    val products by viewModel.productData.observeAsState(emptyList())
//    val productState by viewModel.productState.observeAsState(ProductState.LOADING)
//
//    // Fetch only coffee products when component is first rendered
//    LaunchedEffect(Unit) {
//        viewModel.fetchProductByType("Coffee") // Only fetch coffee products
//    }
//
//    when (productState) {
//        ProductState.LOADING -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        ProductState.EMPTY -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("No coffee data available")
//            }
//        }
//
//        is ProductState.ERROR -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Error: ${(productState as ProductState.ERROR).message}")
//            }
//        }
//
//        ProductState.SUCCESS -> {
//            Box(
//                modifier = Modifier
//                    .padding(5.dp)
//                    .fillMaxWidth()
//                    .height(300.dp)
//                    .background(Color.White, RoundedCornerShape(16.dp))
//                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
//            ) {
//                AndroidView(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(300.dp)
//                        .padding(16.dp),
//                    factory = { context ->
//                        LineChart(context).apply {
//                            description.isEnabled = false
//                            xAxis.position = XAxis.XAxisPosition.BOTTOM
//                            axisRight.isEnabled = false
//
//                            val boldTypeface = Typeface.DEFAULT_BOLD
//                            xAxis.apply {
//                                typeface = boldTypeface
//                                textColor = Color(0xFF6F4E37).toArgb() // Coffee brown color
//                                setDrawGridLines(false)
//                                granularity = 1f
//                                labelRotationAngle = -45f
//                            }
//
//                            axisLeft.apply {
//                                typeface = boldTypeface
//                                textColor = Color(0xFF6F4E37).toArgb() // Coffee brown color
//                                setDrawGridLines(true)
//                                gridLineWidth = 1f
//                                gridColor = Color.LightGray.toArgb()
//                                axisLineWidth = 2f
//                                axisLineColor = Color(0xFF6F4E37).toArgb() // Coffee brown color
//                            }
//
//                            legend.apply {
//                                isEnabled = true
//                                textSize = 12f
//                                typeface = boldTypeface
//                                textColor = Color(0xFF6F4E37).toArgb() // Coffee brown color
//                                verticalAlignment = Legend.LegendVerticalAlignment.TOP
//                                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
//                                orientation = Legend.LegendOrientation.VERTICAL
//                                setDrawInside(true)
//                            }
//
//                            setTouchEnabled(true)
//                            isDragEnabled = true
//                            setScaleEnabled(true)
//                            setPinchZoom(true)
//                            animateX(1500)
//                        }
//                    },
//                    update = { lineChart ->
//                        val dataSets = ArrayList<ILineDataSet>()
//
//                        // Group coffee products by name
//                        val groupedProducts = products.groupBy { it.name }
//
//                        groupedProducts.forEach { (productName, productGroup) ->
//                            val entries = listOf(
//                                Entry(0f, productGroup.firstOrNull()?.quantity?.toFloat() ?: 0f)
//                            )
//
//                            val dataSet = LineDataSet(entries, productGroup .firstOrNull()?.name ?: "")
//                            dataSet.apply {
//                                setDrawCircles(false)
//                                setDrawValues(false)
//                                color = Color(0xFF6F4E37).toArgb() // Coffee brown color
//                                lineWidth = 2f
//                                setDrawFilled(true)
//                                fillDrawable = ColorDrawable(Color(0xFF6F4E37).toArgb()) // Coffee brown color
//                            }
//
//                            dataSets.add(dataSet)
//                        }
//
//                        val lineData = LineData(dataSets)
//                        lineData.apply {
//                            setValueTextColor(Color(0xFF6F4E37).toArgb()) // Coffee brown color
//                            setValueTextSize(10f)
//                        }
//
//                        lineChart.data = lineData
//                        lineChart.invalidate()
//                    }
//                )
//            }
//        }
//    }
//}











