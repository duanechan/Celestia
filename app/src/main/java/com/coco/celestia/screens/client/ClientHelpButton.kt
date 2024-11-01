package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClientHelpOverlay(isVisible: MutableState<Boolean>) {
    if (isVisible.value) {
        val currentPage = remember { mutableStateOf(1) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .clickable { isVisible.value = false }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (currentPage.value) {
                            1 -> "How to Navigate the Client Dashboard"
                            else -> "How to Place Your Order"
                        },
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${currentPage.value} / 2",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Page Content
                    Text(
                        text = when (currentPage.value) {
                            1 -> "1. Access your orders through the shopping cart icon and check notifications through the bell icon at the top of the dashboard.\n\n" +
                                    "2. Explore product categories, featured items, and your order history on the dashboard.\n\n" +
                                    "3. Categories display available products for selection.\n\n" +
                                    "4. Featured items show a rotating selection from each category.\n\n" +
                                    "5. Order history helps you review past orders and reorder easily."

                            else -> "1. Navigate to the order section on the main menu.\n\n" +
                                    "2. Select your desired products by browsing categories.\n\n" +
                                    "3. Adjust the quantity for each item you wish to purchase.\n\n" +
                                    "4. Choose your preferred delivery date.\n\n" +
                                    "5. Confirm all details to place your order."
                        },
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (currentPage.value == 2) {
                                Button(
                                    onClick = { currentPage.value = 1 },
                                    modifier = Modifier.size(width = 100.dp, height = 48.dp)
                                ) {
                                    Text("Back")
                                }
                            }
                            if (currentPage.value == 1) {
                                Button(
                                    onClick = { currentPage.value = 2 },
                                    modifier = Modifier.size(width = 100.dp, height = 48.dp)
                                ) {
                                    Text("Next")
                                }
                            } else {
                                Button(
                                    onClick = { isVisible.value = false },
                                    modifier = Modifier.size(width = 100.dp, height = 48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFA726),
                                        contentColor = Color.White
                                    ),
                                ) {
                                    Text("Got it")
                                }
                            }
                        }
                    }
                }
            }
        }
    }