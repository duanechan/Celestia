@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CoopDashboard(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    role: String,

    ) {
    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(filter = "", role = role)
    }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .background(BGGradientBrush)
        .fillMaxSize()
        .semantics { testTag = "android:id/CoopDashboardBox" }) {
        Column (modifier = Modifier.verticalScroll(scrollState)) {
            OverviewSummaryBox(orderViewModel, productViewModel, role)
            OrderStatusSummary(orderViewModel)
            StockLevelsBarGraph(productViewModel, role)
        }
    }
}

@Composable
fun OrderStatusSummary (
    orderViewModel: OrderViewModel
) {
    val orders by orderViewModel.orderData.observeAsState(emptyList())

    val pending = orders.count { it.status == "PENDING" }
    val preparing = orders.count { it.status == "PREPARING" }
    val completed = orders.count { it.status == "COMPLETED" }
    val delivering = orders.count { it.status == "DELIVERING" }

    Box (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        Column {
            Text(
                text = "Order Status Overview",
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(8.dp)
            )

            Row (
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OrderStatusCard("Pending", pending, "pending")
                OrderStatusCard("Preparing", preparing, "preparing")
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OrderStatusCard("Delivering", delivering, "delivering")
                OrderStatusCard("Completed", completed, "completed")
            }
        }
    }
}

@Composable
fun OrderStatusCard (label: String, value: Int, status: String) {
    val color = when (status) {
        "pending" -> PendingStatus
        "preparing" -> PreparingStatus
        "completed" -> CompletedStatus
        "delivering" -> DeliveringStatus
        else -> Color.Transparent
    }
    Box (
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Card (
            modifier = Modifier
                .width(135.dp)
                .height(140.dp)
                .padding(bottom = 10.dp)
        ) {
            Column (
                modifier = Modifier
                    .background(color)
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = rememberImagePainter(R.drawable.box),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Product Image",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(50.dp)
                    )
                    Text(
                        value.toString(),
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                Text(
                    label,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun OverviewSummaryBox(orderViewModel: OrderViewModel, productViewModel: ProductViewModel, role: String) {
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
                    ProductStockTrendsChart(productViewModel, role)
                    OrderStatusDonutChart(orderViewModel)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OrderStatusDonutChart(orderViewModel: OrderViewModel) {
    val orders by orderViewModel.orderData.observeAsState(emptyList())

    val totalOrders = orders.size.toFloat()
    val pending = orders.count { it.status == "PENDING" }
    val preparing = orders.count { it.status == "PREPARING" }
    val completed = orders.count { it.status == "COMPLETED" }
    val delivering = orders.count { it.status == "DELIVERING" }

    // Calculate raw percentages
    val pendingPercentage = if (totalOrders > 0) pending / totalOrders else 0f
    val preparingPercentage = if (totalOrders > 0) preparing / totalOrders else 0f
    val completedPercentage = if (totalOrders > 0) completed / totalOrders else 0f
    val deliveringPercentage = if (totalOrders > 0) delivering / totalOrders else 0f

    // Calculate total percentage and scale each proportion to ensure the circle is filled
    val totalPercentage = pendingPercentage + preparingPercentage + completedPercentage + deliveringPercentage
    val scaleFactor = if (totalPercentage > 0) 100 / totalPercentage else 0f

    val scaledPending = pendingPercentage * scaleFactor
    val scaledPreparing = preparingPercentage * scaleFactor
    val scaledCompleted = completedPercentage * scaleFactor
    val scaledDelivering = deliveringPercentage * scaleFactor

    val animatedPendingProgress = remember { Animatable(0f) }
    val animatedPreparingProgress = remember { Animatable(0f) }
    val animatedCompletedProgress = remember { Animatable(0f) }
    val animatedDeliveringProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            animatedPendingProgress.animateTo(scaledPending, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
            animatedPreparingProgress.animateTo(scaledPreparing, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
            animatedCompletedProgress.animateTo(scaledCompleted, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
            animatedDeliveringProgress.animateTo(scaledDelivering, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
        }
    }

    val statusData = listOf(
        Triple(animatedPendingProgress.value, PendingStatus, "Pending"),
        Triple(animatedPreparingProgress.value, PreparingStatus, "Preparing"),
        Triple(animatedCompletedProgress.value, CompletedStatus, "Completed"),
        Triple(animatedDeliveringProgress.value, DeliveringStatus, "Delivering")
    )

    Row(
        modifier = Modifier.padding(16.dp)
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
                        val sweepAngle = percentage * 3.6f // Scale each to fill the circle
                        if (percentage > 0) {
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )

                            // Calculate a consistent position for the percentage text
                            val angleInRadians = Math.toRadians(startAngle + (sweepAngle / 2).toDouble())
                            val textRadius = radius + 55f // Fixed distance from the circle for all labels
                            val x = center.x + (textRadius * cos(angleInRadians)).toFloat()
                            val y = center.y + (textRadius * sin(angleInRadians)).toFloat()

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    textSize = 33f
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
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            statusData.forEach { (percentage, color, label) ->
                if (percentage > 0) { // Only show legend for non-zero proportions
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
fun ProductStockTrendsChart(productViewModel: ProductViewModel, role: String) {
    val orderViewModel: OrderViewModel = viewModel()
    val orders by orderViewModel.orderData.observeAsState(emptyList())
    val products by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)


    LaunchedEffect(products, orders) {
        when (role) {
            "CoopMeat" -> {
                orderViewModel.fetchAllOrders("", role)
                productViewModel.fetchProducts("", role)
            }
            "CoopCoffee" -> {
                orderViewModel.fetchAllOrders("", role)
                productViewModel.fetchProducts("", role)
            }
        }
    }

    val lastSevenDays = (0..6).map { offset ->
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }.time
    }.sortedBy { it }
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    val formattedLastSevenDays = lastSevenDays.map { dateFormatter.format(it) }

    // Filter data based on the role
    val filteredProducts = products.filter { product ->
        (role == "CoopMeat" && product.type == "CoopMeat") || (role == "CoopCoffee" && product.type == "CoopCoffee")
    }

    val joinedData = filteredProducts.map { product ->
        product to orders.filter { order ->
            val orderDate = dateFormatter.parse(order.orderDate)
            order.orderData.name == product.name && orderDate != null && dateFormatter.format(orderDate) in formattedLastSevenDays
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
                Text("No data available for the selected role")
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
                    text = "Ordered Product Trends (last 7 days)",
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
                                axisMinimum = 0f  // Ensures no negative values are displayed
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
                            val entries = formattedLastSevenDays.mapIndexed { index, date ->
                                val quantity = matchedOrders.find { dateFormatter.format(dateFormatter.parse(it.orderDate)) == date }
                                    ?.orderData?.quantity ?: 0
                                Entry(index.toFloat(), quantity.coerceAtLeast(0).toFloat())  // Coerce to avoid negatives
                            }

                            val color = when (product.name) {
                                "Green Beans" -> GreenBeans.toArgb()
                                "Roasted Beans" -> RoastedBeans.toArgb()
                                "Packaged Beans" -> Packed.toArgb()
                                "Sorted Beans" -> Sorted.toArgb()
                                "Raw Meat" -> RawMeat.toArgb()
                                "Pork" -> Pork.toArgb()
                                "Kiniing" -> Kiniing.toArgb()
                                else -> Color.Gray.toArgb()
                            }

                            LineDataSet(entries, product.name).apply {
                                this.color = color
                                lineWidth = 2f
                                setDrawCircles(true)
                                circleRadius = 4f
                                setCircleColor(color)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = color
                                valueTextSize = 10f
                            }
                        }

                        lineChart.apply {
                            data = LineData(dataSets)
                            xAxis.valueFormatter = IndexAxisValueFormatter(formattedLastSevenDays.toTypedArray())
                            animateX(1000)
                            invalidate()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StockLevelsBarGraph(productViewModel: ProductViewModel, role: String) {
    val products by productViewModel.productData.observeAsState(emptyList())  // List of products
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        when (role) {
            "CoopMeat" -> productViewModel.fetchProductByType("Meat")
            "CoopCoffee" -> productViewModel.fetchProductByType("Coffee")
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
                Text("No data available for the selected role")
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
                    .padding(8.dp)
                    .fillMaxSize()
                    .height(300.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(start = 15.dp, top = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    ) {
                        Text(
                            text = "Stock Levels",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            fontSize = 13.sp
                        )

                        // Check for low stock products
                        val lowStockProducts = products.filter { it.quantity <= 0 }
                        if (lowStockProducts.isNotEmpty()) {
                            Text(
                                text = "⚠️ Low stock alert for: ${lowStockProducts.joinToString { it.name }}",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp),
                        factory = { context ->
                            BarChart(context).apply {
                                description.isEnabled = false
                                setTouchEnabled(true)
                                isDragEnabled = true
                                setScaleEnabled(true)
                                setPinchZoom(true)

                                axisLeft.apply {
                                    textColor = Color(0xFF6F4E37).toArgb()
                                    setDrawGridLines(true)
                                    gridColor = Color.LightGray.toArgb()
                                    axisMinimum = 0f  // Ensures no negative values are displayed
                                }

                                xAxis.apply {
                                    position = XAxis.XAxisPosition.BOTTOM
                                    textColor = Color(0xFF6F4E37).toArgb()
                                    setDrawGridLines(false)
                                    granularity = 1f
                                    labelRotationAngle = -45f
                                }

                                axisRight.isEnabled = false
                                legend.isEnabled = false
                            }
                        },
                        update = { barChart ->
                            // Extract stock or quantity from product data based on JSON structure
                            val entries = products.mapIndexed { index, product ->
                                val stockQuantity = product.quantity  // Ensure quantity is not null
                                BarEntry(index.toFloat(), stockQuantity.toFloat().coerceAtLeast(0f)) // Ensure no negative values
                            }

                            val colors = products.map { product ->
                                when (product.name) {
                                    "Green Beans" -> GreenBeans.toArgb()
                                    "Roasted Beans" -> RoastedBeans.toArgb()
                                    "Packaged Beans" -> Packed.toArgb()
                                    "Sorted Beans" -> Sorted.toArgb()
                                    "Raw Meat" -> RawMeat.toArgb()
                                    "Pork" -> Pork.toArgb()
                                    "Kiniing" -> Kiniing.toArgb()
                                    else -> Color.Gray.toArgb()
                                }
                            }

                            val barDataSet = BarDataSet(entries, "").apply {
                                setColors(colors)
                                valueTextColor = Color(0xFF6F4E37).toArgb()
                                valueTextSize = 10f
                                setDrawValues(true)
                            }

                            val barData = BarData(barDataSet).apply {
                                barWidth = 0.9f
                            }

                            barChart.apply {
                                data = barData
                                xAxis.valueFormatter = IndexAxisValueFormatter(products.map { it.name }.toTypedArray())
                                animateY(1000)  // Animation for Y-axis
                                invalidate()
                            }
                        }
                    )
                }
            }
        }
    }
}


