package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.LightOrange

@Composable
fun ClientHelpOverlay(isVisible: MutableState<Boolean>) {
    if (isVisible.value) {
        val currentPage = remember { mutableStateOf(1) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .clickable { isVisible.value = false }
                .semantics { testTag = "android:id/HelpOverlay" }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.6f)
                    .semantics { testTag = "android:id/HelpDialogBox" }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .semantics { testTag = "android:id/HelpDialogContent" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = when (currentPage.value) {
                            1 -> "How to Navigate the Client Dashboard"
                            2 -> "How to Place Your Order"
                            else -> "Accessing Your Order Details"
                        },
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .semantics { testTag = "android:id/HelpDialogTitle" }
                    )
                    Text(
                        text = "${currentPage.value} / 3",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .semantics { testTag = "android:id/PageIndicator" }
                    )

                    // Page Content
                    Text(
                        text = when (currentPage.value) {
                            //Client Dashboard
                            1 -> "1. Access your orders through the shopping cart icon and check notifications through the bell icon at the top of the dashboard.\n\n" +
                                    "2. Explore product categories, featured items, and your order history on the dashboard.\n\n" +
                                    "3. Categories display available products for selection.\n\n" +
                                    "4. Featured products are shown in random from each category.\n\n" +
                                    "5. Order history helps you review past orders and reorder easily."
                            //Ordering
                            2 -> "1. Navigate to the order section on the main menu.\n\n" +
                                    "2. Select your desired products by browsing categories.\n\n" +
                                    "3. Adjust the quantity for each item you wish to purchase.\n\n" +
                                    "4. Input your preferred kilograms(kg) from the products.\n\n" +
                                    "5. Choose your preferred delivery date.\n\n" +
                                    "6. Confirm all details to place your order.\n\n" +
                                    "7. After confirming, you can choose to order again or track your order."
                            //Order Details
                            else -> "1. You can access your order details by clicking the order card from the order tab.\n\n" +
                                    "2. In the order details, you can see your Order Status, Items Ordered, Product Quantity, Date Ordered, Location, and Target Date for your delivery.\n\n" +
                                    "3. Tracking orders is also included in the order details.\n\n" +
                                    "4. There is a button for Cancelling Orders which will only appear when your order status is PENDING.\n\n" +
                                    "5. There is also a button for Receiving Orders which will only appear when your order status is COMPLETED."
                        },
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .semantics { testTag = "android:id/HelpDialogContentText" }
                    )



                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .semantics { testTag = "android:id/NavigationButtonsRow" },
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (currentPage.value > 1) {
                            Button(
                                onClick = { currentPage.value -= 1 },
                                modifier = Modifier
                                    .size(width = 100.dp, height = 48.dp)
                                    .semantics { testTag = "android:id/BackButton" }
                            ) {
                                Text("Back")
                            }
                        }
                        if (currentPage.value < 3) {
                            Button(
                                onClick = { currentPage.value += 1 },
                                modifier = Modifier
                                    .size(width = 100.dp, height = 48.dp)
                                    .semantics { testTag = "android:id/NextButton" }
                            ) {
                                Text("Next")
                            }
                        } else {
                            Button(
                                onClick = { isVisible.value = false },
                                modifier = Modifier
                                    .size(width = 100.dp, height = 48.dp)
                                    .semantics { testTag = "android:id/GotItButton" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LightOrange,
                                    contentColor = Color.White
                                ),
                            ) {
                                Text("Got it")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}