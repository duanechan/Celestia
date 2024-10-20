package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.R
import com.coco.celestia.ui.theme.LightGray
import com.coco.celestia.ui.theme.RavenBlack
import com.coco.celestia.ui.theme.VeryDarkGreen

@Preview
@Composable
fun ClientDashboard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 75.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome!", // + client first name
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = RavenBlack,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {},
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notification_icon),
                        contentDescription = "Notification Icon",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            BrowseCategories()

            Spacer(modifier = Modifier.height(16.dp))
            FeaturedProducts()

            Spacer(modifier = Modifier.height(16.dp))
            OrderHistory()
        }
    }
}

//TODO: show all products here(?)
@Composable
fun BrowseCategories() {
    Box(
        modifier = Modifier
            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Browse Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

//TODO: show featured products of coffee, meat, and vegetable
@Composable
fun FeaturedProducts() {
    Box(
        modifier = Modifier
            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Featured Products",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

//TODO: show orders that are already delivered
@Composable
fun OrderHistory() {
    Box(
        modifier = Modifier
            .background(VeryDarkGreen, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Order History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}