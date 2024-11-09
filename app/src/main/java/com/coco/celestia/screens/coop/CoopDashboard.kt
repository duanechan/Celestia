package com.coco.celestia.screens.coop

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
// Add these imports
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
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
import java.util.Calendar
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
        .fillMaxSize()
        .semantics { testTag = "android:id/CoopDashboardBox" }) {
        Column {
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
        .background(Color.White)
        .semantics { testTag = "android:id/OverviewSummaryBox" }) {
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
                    ProductStockTrendsChart(productViewModel)
                    OrderStatusDonutChart(orderViewModel)
                }
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
    val delivering = orders.count { it.status == "DELIVERING" }

    // Calculate percentages
    val pendingPercentage = if (totalOrders > 0) (pending / totalOrders) * 100 else 0f
    val preparingPercentage = if (totalOrders > 0) (preparing / totalOrders) * 100 else 0f
    val completedPercentage = if (totalOrders > 0) (completed / totalOrders) * 100 else 0f
    val deliveringPercentage = if (totalOrders > 0) (delivering / totalOrders) * 100 else 0f

    // Animation states
    var targetPendingProgress by remember { mutableStateOf(0f) }
    var targetPreparingProgress by remember { mutableStateOf(0f) }
    var targetCompletedProgress by remember { mutableStateOf(0f) }
    var targetDeliveringProgress by remember { mutableStateOf(0f) }

    // Trigger animations
    LaunchedEffect(pendingPercentage, preparingPercentage, completedPercentage, deliveringPercentage) {
        targetPendingProgress = pendingPercentage
        targetPreparingProgress = preparingPercentage
        targetCompletedProgress = completedPercentage
        targetDeliveringProgress = deliveringPercentage
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
    val animatedDeliveringProgress by animateFloatAsState(
        targetValue = targetDeliveringProgress,
        animationSpec = tween(durationMillis = 750)
    )

    // Prepare status data for both the chart and the legend
    val statusData = listOf(
        Triple(animatedPendingProgress, PendingStatus, "Pending"),
        Triple(animatedPreparingProgress, PreparingStatus, "Preparing"),
        Triple(animatedCompletedProgress, CompletedStatus, "Completed"),
        Triple(animatedDeliveringProgress, DeliveringStatus, "Delivering")
    )

    Row(
        modifier = Modifier
            .padding(16.dp)
            .semantics { testTag = "android:id/OrderStatusDonutChartRow" }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
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

                    statusData.forEach { (percentage, color, _) ->
                        val sweepAngle = percentage * 3.6f
                        if (percentage > 0) { // Only draw if percentage is greater than zero
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            // Calculate position for percentage text
                            val angleInRadians = Math.toRadians(startAngle + (sweepAngle / 2).toDouble())
                            val textRadius = size.width / 2 * 1.5f
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
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        // Legend Column
        Column(
            modifier = Modifier
                .padding(start = 0.dp)
                .align(Alignment.CenterVertically)
        ) {
            statusData.forEach { (percentage, color, label) ->
                if (percentage > 0) { // Only show legend for non-zero percentages
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(color)
                        )
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = DarkGreen,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStockTrendsChart(productViewModel: ProductViewModel) {
    val orderViewModel: OrderViewModel = viewModel()
    val orders by orderViewModel.orderData.observeAsState(emptyList())  // List of orders
    val products by productViewModel.productData.observeAsState(emptyList())  // List of products
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("Coffee", "Coop")
        productViewModel.fetchProductByType("Coffee")  // Only fetch coffee products
    }

    val lastFiveDays = (0..4).map { offset ->
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }.time
    }.sortedBy { it }
    val dateFormatter = SimpleDateFormat("MM/dd", Locale.US)
    val formattedLastFiveDays = lastFiveDays.map { dateFormatter.format(it) }
    val joinedData = products.map { product ->
        product to orders.filter { order ->
            order.orderData.name == product.name && order.orderDate in formattedLastFiveDays
        }
    }

    when (productState) {
        ProductState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ProductState.EMPTY -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No coffee data available")
            }
        }

        is ProductState.ERROR -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${(productState as ProductState.ERROR).message}")
            }
        }

        ProductState.SUCCESS -> {
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "Product Stock Trends",
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.padding(start = 15.dp, top = 5.dp),
                    fontSize = 10.sp
                )
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            axisRight.isEnabled = false

                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)

                            xAxis.apply {
                                textColor = Color(0xFF6F4E37).toArgb()
                                setDrawGridLines(false)
                                granularity = 1f
                                labelRotationAngle = -45f
                            }

                            axisLeft.apply {
                                textColor = Color(0xFF6F4E37).toArgb()
                                setDrawGridLines(true)
                                gridColor = Color.LightGray.toArgb()
                            }

                            legend.apply {
                                textColor = Color(0xFF6F4E37).toArgb()
                                textSize = 12f
                                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                                orientation = Legend.LegendOrientation.VERTICAL
                                setDrawInside(true)
                            }
                        }
                    },
                    update = { lineChart ->
                        val dataSets = joinedData.map { (product, matchedOrders) ->
                            val entries = formattedLastFiveDays.mapIndexed { index, date ->
                                val quantity = matchedOrders.find { it.orderDate == date }?.orderData?.quantity ?: 0
                                Entry(index.toFloat(), quantity.toFloat())
                            }

                            LineDataSet(entries, product.name).apply {
                                color = Color(0xFF6F4E37).toArgb()
                                lineWidth = 2f
                                setDrawCircles(true)
                                circleRadius = 4f
                                setCircleColor(Color(0xFF6F4E37).toArgb())
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = Color(0xFF6F4E37).toArgb()
                            }
                        }

                        lineChart.apply {
                            data = LineData(dataSets)
                            xAxis.valueFormatter = IndexAxisValueFormatter(formattedLastFiveDays.toTypedArray())
                            animateX(1000)
                            invalidate()
                        }
                    }
                )
            }
        }
    }
}