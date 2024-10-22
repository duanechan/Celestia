package com.coco.celestia.screens.admin

import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.model.UserData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun AdminDashboard(userData: UserData?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            val dateFormat = SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())
            Spacer(modifier = Modifier.height(100.dp))
            userData?.let { user ->
                Text(
                    text = "Welcome, ${user.firstname} ${user.lastname}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontFamily = mintsansFontFamily,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = today,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Start,
                fontFamily = mintsansFontFamily,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(50.dp))
            SummaryDashboard()
        }
    }
}


@Composable
fun SummaryDashboard(){
    Box(modifier = Modifier
        .padding(1.dp)
        .fillMaxWidth()
        .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(Color.White)){
        Text(text = "Overview Summary",
            fontWeight = FontWeight.Bold,
            color = DarkBlue,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp))
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.padding(5.dp)) {
            Column {
                TestAdminChart(avgTimeToMarket, maxTimeToMarket)
                InventoryPieChart(PieChartItem)
            }
            TotalProductionVolumeLineChart(productionData)
        }

    }
}

//Sample Data
val productionData = listOf(
    Entry(0f, 50f),
    Entry(1f, 60f),
    Entry(2f, 70f),
    Entry(3f, 80f),
    Entry(4f, 90f)
)

// Sample Data
val PieChartItem = listOf(
    PieEntry(40f, "Meat"),
    PieEntry(30f, "Coffee"),
)

// Example usage
val avgTimeToMarket = 7f
val maxTimeToMarket = 10f

@Composable
fun TotalProductionVolumeLineChart(dataPoints: List<Entry>) {
    Box(
        modifier = Modifier
            .padding(5.dp)
            .width(300.dp)
            .height(300.dp)
    ) {

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    axisRight.isEnabled = false
                    legend.isEnabled = true

                    val boldTypeface = Typeface.DEFAULT_BOLD
                    xAxis.typeface = boldTypeface
                    axisLeft.typeface = boldTypeface

                    axisLeft.setDrawGridLines(true)
                    axisLeft.gridLineWidth = 2f
                    axisRight.isEnabled = false

                    // Legend
                    legend.isEnabled = true
                }
            },
            update = { lineChart ->
                val dataSet = LineDataSet(dataPoints, "Production Volume").apply {
                    color = DarkBlue.toArgb()
                    valueTextColor = android.graphics.Color.BLACK
                    lineWidth = 4f
                    circleRadius = 6f

                    setDrawCircleHole(false)
                }
                lineChart.data = LineData(dataSet)
                lineChart.invalidate()
            }
        )
    }
}

@Composable
fun InventoryPieChart(entries: List<PieEntry>) {

    Box(modifier = Modifier
        .width(160.dp)
        .height(160.dp)){

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            factory = { context ->
                PieChart(context).apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.isEnabled = true
                    legend.textColor = android.graphics.Color.BLACK
                    legend.textSize = 12f
                }
            },

            update = { pieChart ->
                val dataSet = PieDataSet(entries, "Inventory this month").apply {
                    setColors(
                        RedMeat.toArgb(),
                        BrownCoffee.toArgb()
                    )
                    valueTextColor = android.graphics.Color.BLACK

                }
                pieChart.data = PieData(dataSet)
                pieChart.invalidate()
            }
        )
    }
}

@Composable
fun TestAdminChart(averageTime: Float, maxTime: Float) {
    var targetProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        targetProgress = (averageTime / maxTime) * 100
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        modifier = Modifier
            .padding(top = 15.dp)
            .size(150.dp)
    ) {
        // Circular progress indicator
        CircularProgressIndicator(
            progress = animatedProgress / 100f, // Normalize to [0, 1]
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp),
            color = DarkBlue,
            strokeWidth = 15.dp
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Time to Market:",
                fontSize = 10.sp,
            )
            Text(
                text = "${averageTime.toInt()} Days", // Display average time as integer
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

