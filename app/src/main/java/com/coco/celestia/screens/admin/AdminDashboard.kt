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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/welcomeMessage" },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/currentDate" },
                color = Color.White
            )
            Spacer(modifier = Modifier.height(30.dp))
            SummaryDashboard()
        }
    }
}

@Composable
fun SummaryDashboard() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .semantics { testTag = "android:id/summaryDashboard" }
    ) {
        item{
        Box(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
                .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
                .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        ) {
            Text(
                text = "Inventory Overview",
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier
                    .padding(start = 20.dp, top =15.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.padding(5.dp)) {
                Column {
                    InventoryPieChart(PieChartItem)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth()
                    .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .semantics { testTag = "android:id/userManagementOverviewBox" }
            ) {
                Text(
                    text = "User Management Overview",
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 15.dp)
                )
                Row(modifier = Modifier.padding(5.dp)) {
                    Column {
                        UserManagementDashboard()
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Sample Data
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


@Composable
fun InventoryPieChart(entries: List<PieEntry>) {
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { testTag = "android:id/inventoryPieChartContainer" }
    ) {
        // Alerts Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/alertsCard" }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Green beans: 15 kg left", fontSize = 14.sp, color = Color.Red)
                Text("• Kiniing: 5 kg left", fontSize = 14.sp, color = Color.Red)
            }
        }

        // Inventory Pie Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .semantics { testTag = "android:id/inventoryPieChart" },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = false
                        setUsePercentValues(true)
                        legend.isEnabled = false
                    }
                },
                update = { pieChart ->
                    val dataSet = PieDataSet(entries, "").apply {
                        setColors(
                            RedMeat.toArgb(),
                            BrownCoffee.toArgb()
                        )
                        valueTextColor = android.graphics.Color.WHITE
                        valueTextSize = 12f
                    }
                    pieChart.data = PieData(dataSet)
                    pieChart.invalidate()
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(BrownCoffee)
                        .semantics { testTag = "android:id/coffeeLegend" }
                )
                Text(
                    text = " Coffee",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(RedMeat)
                        .semantics { testTag = "android:id/meatLegend" }
                )
                Text(
                    text = " Meat",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
            }
        }
    }
}


@Composable
fun UserManagementDashboard() {
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { testTag = "android:id/userManagementDashboard" }
    ) {
        // Total Users Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/totalUsersCard" }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Total Users",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.semantics { testTag = "android:id/totalUsersLabel" }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "20",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.semantics { testTag = "android:id/totalUsersCount" }
                )
            }
        }

        // Active Users Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/activeUsersCard" }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Active Users",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.semantics { testTag = "android:id/activeUsersLabel" }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "7",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green,
                    modifier = Modifier.semantics { testTag = "android:id/activeUsersCount" }
                )
            }
        }

        // Recent Activity Section
        Text(
            text = "Recent Activity",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/recentActivityLabel" }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .semantics { testTag = "android:id/recentActivityList" }
        ) {
            Text("• User A updated inventory - 2 mins ago", fontSize = 14.sp)
            Text("• User B added a new order - 10 mins ago", fontSize = 14.sp)
            Text("• User C logged in - 30 mins ago", fontSize = 14.sp)
        }
    }
}


