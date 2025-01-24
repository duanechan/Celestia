package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FarmerDashboard(
    navController: NavController,
    specialRequestViewModel: SpecialRequestViewModel,
    userViewModel: UserViewModel,
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val farmerData by userViewModel.userData.observeAsState()
    val assignedProducts by specialRequestViewModel.specialReqData.observeAsState()
    val orderData = remember { mutableStateListOf<OrderData>() }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault()) }
    val today = dateFormat.format(Date())

    val scrollState = rememberScrollState()

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
        specialRequestViewModel.fetchAssignedProducts(
            farmerData?.email ?: ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(scrollState)
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        // Greeting and date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .background(White1, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = today,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    color = DarkGreen
                )

                farmerData?.let { user ->
                    Text(
                        text = "$greeting, ${user.firstname} ${user.lastname}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = DarkGreen,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // orders overview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .background(White1, shape = RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                }
                FarmerOrderOverview(
                    orders = assignedProducts?.map { assigned ->
                        OrderData(
                            orderId = assigned.specialRequestUID,
                            client = assigned.name,
                            status = assigned.assignedMember.firstOrNull { it.email == farmerData?.email }?.status ?: "UNKNOWN"
                        )
                    } ?: emptyList()
                )
            }
        }

        // Order Listings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .background(White1, shape = RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 5.dp, end = 10.dp, bottom = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Order Requests",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    TextButton(onClick = { navController.navigate("farmer_manage_order") }) {
                        Text(
                            text = "See All",
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                assignedProducts
                    ?.filter { it.status == "In Progress" }
                    ?.filter { member ->
                        member.assignedMember.any { it.status.isEmpty() }
                    }
                    ?.sortedByDescending { assigned ->
                        assigned.trackRecord
                            .filter { it.description.contains("assigned", ignoreCase = true) }
                            .maxByOrNull { it.dateTime }
                            ?.dateTime
                    }
                    ?.forEach { assigned ->
                        DisplayRequestCard(
                            navController,
                            assigned,
                            farmerData?.email ?: ""
                        )
                    }
            }
        }
    }
}

@Composable
fun FarmerOrderOverview(orders: List<OrderData>) {
    val statuses = listOf(
        "Soil Preparation", "Seed Sowing", "Growing", "Pre-Harvest",
        "Harvesting", "Post-Harvest", "Picked Up By Coop",
        "Completed", "Calamity Affected"
    )

    val statusCounts = statuses.associateWith { status ->
        orders.count { it.status.equals(status, ignoreCase = true) }
    }

    val statusIcons = mapOf(
        "Soil Preparation" to R.drawable.plant_hand,
        "Seed Sowing" to R.drawable.plant,
        "Growing" to R.drawable.planting,
        "Pre-Harvest" to R.drawable.harvest,
        "Harvesting" to R.drawable.harvest_basket,
        "Post-Harvest" to R.drawable.harvested,
        "Picked Up By Coop" to R.drawable.deliveryicon,
        "Completed" to R.drawable.received,
        "Calamity Affected" to R.drawable.calamity
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Orders Overview",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            statuses.chunked(3).forEach { rowStatuses ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowStatuses.forEach { status ->
                        val count = statusCounts[status] ?: 0
                        val icon = statusIcons[status] ?: Icons.Default.Info
                        StatusBox(
                            status = status,
                            count = count,
                            icon = icon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowStatuses.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBox(status: String, count: Int, icon: Any, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White2)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            when (icon) {
                is Int -> {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "$status Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = DarkGreen
                    )
                }
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$status Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = DarkGreen
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Default Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = DarkGreen
                    )
                }
            }

            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = status,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}