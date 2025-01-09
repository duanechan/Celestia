package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R


@Composable
fun ClientOrderDetails(
    navController: NavController,
    orderId: String
) {val productName = "Roasted Beans"
    val productImage = R.drawable.greenbeansimg
    val pricePer100Grams = 100
    val totalQuantity = 10
    val totalCost = pricePer100Grams * totalQuantity

    Column {
        OrderSummaryBox(
            productName = productName,
            productImage = productImage,
            pricePer100Grams = pricePer100Grams,
            totalQuantity = totalQuantity,
            totalCost = totalCost
        )
        Spacer(modifier = Modifier.height(16.dp))
        OrderDetailsSection()
        Spacer(modifier = Modifier.height(16.dp))
        TrackOrderSection()
    }
}

@Composable
fun OrderSummaryBox(
    productName: String,
    productImage: Int,
    pricePer100Grams: Int,
    totalQuantity: Int,
    totalCost: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDDE9D9), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = productImage),
                    contentDescription = "$productName Image",
                    modifier = Modifier.size(80.dp).padding(end = 16.dp),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(productName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("100 grams", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                Text("Php $pricePer100Grams", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Price per 100 grams", fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text("Php $pricePer100Grams", fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total Quantity", fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Text("-")
                }
                Text("$totalQuantity", fontWeight = FontWeight.Bold)
                IconButton(onClick = {}) {
                    Text("+")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total Cost", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Php $totalCost", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderDetailsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDDE9D9), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Pickup Information
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pick Up at", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Market Stall", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("  Hangar", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Payment Method
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Payment Method", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Cash", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message to Seller
        Column {
            Text("Message to Seller", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Please pack it by 10 kg. Will be needed it within this week.",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TrackOrderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            "Track Order",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val steps = listOf(
            "Order Placed" to "10 Dec, 2:51",
            "Confirmed" to "10 Dec, 14:30",
            "Seller is preparing your order" to "10 Dec, 14:30",
            "Order is sent to Stall Market or Hangar" to "10 Dec, 14:30"
        )

        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (index == 0) Color.Green else Color.Gray,
                            shape = RoundedCornerShape(50)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(step.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(step.second, fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (index < steps.size - 1) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(1.dp),
                    color = Color.Gray
                )
            }
        }
    }
}