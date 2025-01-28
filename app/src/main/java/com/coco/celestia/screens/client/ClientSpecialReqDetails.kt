package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.TrackRecord
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ClientSpecialReqDetails(
    navController: NavController,
    specialRequestViewModel: SpecialRequestViewModel,
    specialRequestUID: String
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    val specialReqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val request = specialReqData.find { it.specialRequestUID == specialRequestUID }
    val trackRecord = remember { mutableStateListOf(*(request?.trackRecord?.toTypedArray() ?: emptyArray())) }
    val toDeliver = remember { mutableStateListOf(*(request?.toDeliver?.toTypedArray() ?: emptyArray())) }
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
                        Row {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Green1),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(horizontal = 2.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${specialReq.status}",
                                    color = White1,
                                    fontSize = 16.sp,
                                    fontFamily = mintsansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
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
                            fontFamily = mintsansFontFamily,
                            fontWeight = FontWeight.Bold
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
                                    text = "${product.quantity} kg" ,
                                    color = Green1,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                        }
                    }
                }

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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (specialReq.collectionMethod.equals("delivery", ignoreCase = true))
                                            R.drawable.delivery else R.drawable.pickupclient
                                    ),
                                    contentDescription = "Collection Method Icon",
                                    tint = Green1,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 5.dp)
                                )

                                Text(
                                    text = specialReq.collectionMethod,
                                    color = Green1,
                                    fontFamily = mintsansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (specialReq.toDeliver.any { it.status == "Delivering to Client" }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = White1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    request.toDeliver.map { product ->
                                        val addTrack = TrackRecord(
                                            description = "Client Received ${product.name}: ${product.quantity}kg",
                                            dateTime = formattedDateTime
                                        )
                                        trackRecord.add(addTrack)
                                    }

                                    toDeliver.clear()
                                    specialRequestViewModel.updateSpecialRequest(
                                        request.copy(
                                            trackRecord = trackRecord,
                                            toDeliver = toDeliver,
                                            status = if (request.assignedMember.all { it.status == "Completed" }) {
                                                "Completed"
                                            } else "In Progress"
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.White,
                                    containerColor = Green1
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                            ) {
                                Text(
                                    text = "Received Products",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
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

                        val sortedRecords = specialReq.trackRecord.sortedByDescending {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(it.dateTime)?.time ?: 0
                        }
                        val mostRecentDate = sortedRecords.firstOrNull()?.dateTime

                        sortedRecords.forEach { record ->
                            val isRecent = record.dateTime == mostRecentDate
                            val textColor = if (isRecent) Green1 else Gray.copy(alpha = 0.6f)
                            val dotColor = if (isRecent) Green2 else Gray.copy(alpha = 0.6f)
                            val lineColor = if (isRecent) Green1 else Gray.copy(alpha = 0.6f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .width(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = dotColor,
                                                shape = CircleShape
                                            )
                                    )
                                    if (sortedRecords.indexOf(record) < sortedRecords.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(33.dp)
                                                .background(lineColor)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = record.description,
                                        fontWeight = if (isRecent) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor,
                                        fontFamily = mintsansFontFamily
                                    )

                                    Text(
                                        text = record.dateTime,
                                        fontSize = 12.sp,
                                        color = textColor,
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                            }
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