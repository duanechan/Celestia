package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.ui.theme.*

@Composable
fun FarmerDashboard(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(top = 120.dp)
    ) {
        userData?.let { user ->
            Text(
                text = "Welcome, ${user.firstname} ${user.lastname}!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Cocoa
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 10.dp, top = 270.dp)
        ) {
            Text(
                text = "Recent Order Requests",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Cocoa
            )
            Spacer(modifier = Modifier.height(8.dp))

            val latestOrders = orderData
            FarmerManageRequest(
                navController = navController,
                userData = userData,
                orderData = latestOrders,
                orderState = orderState,
                selectedCategory = selectedCategory,
                searchQuery = searchQuery
            )
        }
    }
}