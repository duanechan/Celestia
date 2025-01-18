package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel

@Composable
fun ClientSpecialReqDetails(
    navController: NavController,
    specialRequestViewModel: SpecialRequestViewModel,
    specialRequestUID: String
) {
    val specialReqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val request = specialReqData.find { it.specialRequestUID == specialRequestUID }

    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchSpecialRequests(
            filter = "",
            orderBy = "Requested",
            ascending = false
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            request?.let { specialReq ->
                // Header with Subject and Status
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = specialReq.subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Status: ${specialReq.status}",
                            color = Green1,
                            fontSize = 16.sp,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }

                // Dates Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Request Information",
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Date Requested: ${specialReq.dateRequested}",
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Target Date: ${specialReq.targetDate}",
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }

                // Description Card
                if (!specialReq.description.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = White1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                color = Green1,
                                fontFamily = mintsansFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = specialReq.description,
                                color = Green1,
                                fontFamily = mintsansFontFamily
                            )
                        }
                    }
                }

                // Products Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Products",
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        specialReq.products.forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product.name,
                                    color = Green1,
                                    fontFamily = mintsansFontFamily
                                )
                                Text(
                                    text = "${product.quantity}",
                                    color = Green1,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                        }
                    }
                }

                // Collection Method Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Collection Method",
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = specialReq.collectionMethod,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }

                // Additional Requests Card (if any)
                if (!specialReq.additionalRequest.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = White1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Additional Requests",
                                fontWeight = FontWeight.Bold,
                                color = Green1,
                                fontFamily = mintsansFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = specialReq.additionalRequest,
                                color = Green1,
                                fontFamily = mintsansFontFamily
                            )
                        }
                    }
                }

                // Track Record Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Track Order",
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        specialReq.trackRecord.forEach { record ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .background(Green1, CircleShape)
                                        .align(Alignment.CenterVertically)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(
                                        text = record.description,
                                        color = Green1,
                                        fontFamily = mintsansFontFamily
                                    )
                                    Text(
                                        text = record.dateTime,
                                        fontSize = 12.sp,
                                        color = Green1.copy(alpha = 0.7f),
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }
        }
    }
}