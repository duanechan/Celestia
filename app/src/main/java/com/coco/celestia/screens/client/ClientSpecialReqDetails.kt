package com.coco.celestia.screens.client

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.ProductStatus
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ClientSpecialReqDetails(
    specialRequestViewModel: SpecialRequestViewModel,
    transactionViewModel: TransactionViewModel,
    clientEmail: String,
    specialRequestUID: String
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)
    val descColumnHeight = remember { mutableStateMapOf<String, Int>() }

    val specialReqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val request = specialReqData.find { it.specialRequestUID == specialRequestUID }
    val trackRecord = remember { mutableStateListOf(*(request?.trackRecord?.toTypedArray() ?: emptyArray())) }
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
                                    text = specialReq.status,
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
                if (specialReq.description.isNotEmpty()) {
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

                if (specialReq.toDeliver.any { it.status == "Delivering to Client" || it.status == "Ready for Pick Up"}) {
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
                                    Log.d("SpecialRequest", "Received Products button clicked")

                                    request.toDeliver.map { product ->
                                        if (product.status != "Delivered") {
                                            val addTrack = TrackRecord(
                                                description = "Client Received ${product.name}: ${product.quantity}kg",
                                                dateTime = formattedDateTime
                                            )
                                            trackRecord.add(addTrack)
                                            Log.d("SpecialRequest", "Added track record for ${product.name}")
                                        }
                                    }

                                    val mergeExisting = request.toDeliver
                                        .groupBy { it.name }
                                        .map { (name, products) ->
                                            val totalQuantity = products.sumOf { it.quantity }
                                            val productId = products.firstOrNull()?.productId
                                            ProductStatus(
                                                name = name,
                                                quantity = totalQuantity,
                                                status = "Delivered",
                                                productId = productId
                                            )
                                        }

                                    val requestStatus = if (request.assignedMember.all { it.status == "Completed" }) {
                                        "Completed"
                                    } else {
                                        "In Progress"
                                    }

                                    val updatedRequest = request.copy(
                                        trackRecord = trackRecord,
                                        toDeliver = mergeExisting,
                                        status = requestStatus
                                    )
                                    Log.d("SpecialRequest", "Recording transactions for delivered products")
                                    recordSpecialRequestTransaction(
                                        specialReq = updatedRequest,
                                        clientEmail = clientEmail,
                                        transactionViewModel = transactionViewModel,
                                        formattedDateTime = formattedDateTime
                                    )
                                    specialRequestViewModel.updateSpecialRequest(updatedRequest)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.White,
                                    containerColor = Green1
                                ),
                                modifier = Modifier.height(52.dp)
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
                if (specialReq.additionalRequest.isNotEmpty()) {
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
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Track Order",
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val sortedRecords = specialReq.trackRecord.sortedByDescending {
                            SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault()).parse(it.dateTime)?.time ?: 0
                        }

                        val mostRecentDate = sortedRecords.firstOrNull()?.dateTime


                        sortedRecords.forEachIndexed { index, record ->
                            val isRecent = record.dateTime == mostRecentDate

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    contentAlignment = Alignment.TopCenter,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .size(12.dp)
                                    ) {
                                        drawCircle(color = Green2)

                                        if (sortedRecords.indexOf(record) < sortedRecords.size - 1) {
                                            val dynamicLineHeight = descColumnHeight[sortedRecords.getOrNull(index + 1)?.dateTime] ?: 0
                                            drawLine(
                                                color = Green1,
                                                start = Offset(size.width / 2, size.height),
                                                end = Offset(size.width / 2, size.height + dynamicLineHeight.toFloat()),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onGloballyPositioned { coordinates ->
                                            descColumnHeight[record.dateTime] =
                                                coordinates.size.height
                                        },
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = record.description,
                                        fontWeight = if (isRecent) FontWeight.Bold else FontWeight.Normal,
                                        color = Green1,
                                        fontFamily = mintsansFontFamily
                                    )

                                    Text(
                                        text = record.dateTime,
                                        fontWeight = if (isRecent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Green1,
                                        fontFamily = mintsansFontFamily
                                    )

                                    // Add image if it exists
                                    record.imageUrl?.let { url ->
                                        var imageUri by remember(url) { mutableStateOf<Uri?>(null) }

                                        LaunchedEffect(url) {
                                            ImageService.fetchStatusImage(url) { uri ->
                                                imageUri = uri
                                            }
                                        }

                                        imageUri?.let { uri ->
                                            Image(
                                                painter = rememberImagePainter(
                                                    data = uri,
                                                    builder = {
                                                        crossfade(true)
                                                    }
                                                ),
                                                contentDescription = "Status update image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .padding(top = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
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
fun encodeEmail(email: String): String {
    return email.replace(".", ",")
}

fun formatDateTime(dateTimeStr: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateTimeStr, inputFormatter)
        val outputFormatter = DateTimeFormatterBuilder()
            .appendPattern("dd MMM yyyy")
            .toFormatter(Locale.ENGLISH)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        dateTimeStr
    }
}

fun recordSpecialRequestTransaction(
    specialReq: SpecialRequest,
    clientEmail: String,
    transactionViewModel: TransactionViewModel,
    formattedDateTime: String
) {
    val deliveredProducts = specialReq.toDeliver
    if (deliveredProducts.isEmpty()) return

    val formattedDate = formatDateTime(formattedDateTime)
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val encodedClient = encodeEmail(clientEmail)

    deliveredProducts.forEach { product ->
        try {
            val assignedFarmer = specialReq.assignedMember.find { it.product == product.name }
            val farmerName = assignedFarmer?.name ?: "Unassigned"

            val transaction = TransactionData(
                transactionId = specialReq.specialRequestUID,
                type = "Non-Retail Sale",
                date = formattedDate,
                description = "Fulfilled special request of ${product.quantity} kg of ${product.name} by $farmerName for client: $clientEmail",
                status = "COMPLETED",
                productName = product.name,
                productId = product.productId ?: "SR-${System.currentTimeMillis()}",
                facilityName = "Special Request",
                vendorName = farmerName
            )
            transactionViewModel.recordTransaction(
                uid = currentUserUid,
                transaction = transaction
            )
        } catch (_: Exception) {}
    }
}