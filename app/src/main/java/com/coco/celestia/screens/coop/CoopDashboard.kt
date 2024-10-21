package com.coco.celestia.screens.coop

import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.coco.celestia.R
import com.coco.celestia.ui.theme.GreenGradientBrush
import com.github.anastr.speedviewlib.components.indicators.Indicator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


@Preview
@Composable
fun CoopDashboard() {
    Box(modifier = Modifier
        .background(GreenGradientBrush)
        .fillMaxSize()){
        Column() {
            Spacer(modifier = Modifier.height(118.dp))
            OverviewSummaryBox()
        }

    }


}

@Composable
fun OverviewSummaryBox(){
    Box(modifier = Modifier
        .padding(8.dp)
        .background(androidx.compose.ui.graphics.Color.White)
        .fillMaxWidth()){
        Text(text = "Overview Summary", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(5.dp))
        Row {
            Column {
                TimeToMarketGaugeChart(avgTimeToMarket, maxTimeToMarket)
                DefectRatesPieChart(defectRates)
            }
            TotalProductionVolumeLineChart(productionData)
        }

    }
}


val productionData = listOf(
    Entry(0f, 50f),
    Entry(1f, 60f),
    Entry(2f, 70f),
    Entry(3f, 80f),
    Entry(4f, 90f)
)

// Sample Data
val defectRates = listOf(
    PieEntry(40f, "Quality Defects"),
    PieEntry(30f, "Roasting Errors"),
    PieEntry(30f, "Packaging Defects")
)

// Example usage
val avgTimeToMarket = 7f
val maxTimeToMarket = 10f

@Composable
fun TotalProductionVolumeLineChart(dataPoints: List<Entry>) {
    Box(modifier = Modifier
        .padding(5.dp)
        .width(250.dp)
        .height(250.dp)){

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
                }
            },
            update = { lineChart ->
                val dataSet = LineDataSet(dataPoints, "Production Volume").apply {
                    color = Color.GREEN
                    valueTextColor = Color.BLACK
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                }
                lineChart.data = LineData(dataSet)
                lineChart.invalidate()
            }
        )
    }
}

@Composable
fun DefectRatesPieChart(entries: List<PieEntry>) {

    Box(modifier = Modifier
        .padding(5.dp)
        .width(150.dp)
        .height(150.dp)){

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                PieChart(context).apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = false
                    setUsePercentValues(true)
                    legend.isEnabled = true
                }
            },
            update = { pieChart ->
                val dataSet = PieDataSet(entries, "Defect Rates").apply {
                    setColors(
                        Color.RED,
                        Color.YELLOW,
                        Color.BLUE
                    )
                    valueTextColor = Color.WHITE
                }
                pieChart.data = PieData(dataSet)
                pieChart.invalidate() // Refresh chart with new data
            }
        )
    }

}

@Composable
fun TimeToMarketGaugeChart(averageTime: Float, maxTime: Float) {
    val progress = (averageTime / maxTime) * 100

    Box(
        modifier = Modifier
            .padding(5.dp)
            .size(150.dp)
    ) {
        // Using AndroidView to integrate SpeedView into Jetpack Compose
        AndroidView(
            factory = { context ->
                com.github.anastr.speedviewlib.SpeedView(context).apply {
                    // Set max to 100 (or your max progress)
                    maxSpeed = 100f

                    // Set current progress with animation duration
                    speedTo(progress, 1000)

                    // Disable speedometer trembling for simplicity
                    withTremble = false

                    // Customize indicator style
                    setIndicator(indicator = Indicator.Indicators.NormalIndicator)

//                    // Set colors
//                    speedometerColor = Color.Transparent
//                    needleColor = MaterialTheme.colors.primary
//                    needlePointColor = MaterialTheme.colors.primary
//                    needlePointRadius = 10f
//                    needleWidth = 10f
//                    needleRadius = 60f
//
//                    // Set text properties
//                    textColor = MaterialTheme.colors.onBackground
//                    textSize = 20f
//                    textTypeface = Typeface.DEFAULT_BOLD
                }
            },
            update = { speedView ->
                // Update speed dynamically
                speedView.speedTo(progress, 1000)
            },
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Avg. Time to Market: $averageTime Days",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${progress.toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}










