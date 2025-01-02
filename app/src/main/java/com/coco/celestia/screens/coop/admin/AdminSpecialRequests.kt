@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop.admin

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.SpecialReqState
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.SpecialRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AdminSpecialRequests(
    specialRequestViewModel: SpecialRequestViewModel,
    status: String
) {
    val reqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val reqState by specialRequestViewModel.specialReqState.observeAsState(OrderState.LOADING)
    var numReq by remember { mutableIntStateOf(0) }
    val keywords by remember { mutableStateOf(status) }

    LaunchedEffect(keywords) {
        specialRequestViewModel.fetchSpecialRequests(status)
    }

    Column(
        modifier = Modifier.background(CoopBackground)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            numReq = reqData.size
            Text("All($numReq)")

            Row {
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(R.drawable.filter2),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box (modifier = Modifier
            .fillMaxWidth()
            .background(White2)
            .padding(4.dp)
        )

        when (reqState) {
            is SpecialReqState.LOADING -> LoadingOrders()
            is SpecialReqState.ERROR -> OrdersError(errorMessage = (reqState as SpecialReqState.ERROR).message)
            is SpecialReqState.EMPTY -> EmptyOrders()
            is SpecialReqState.SUCCESS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White2)
                        .semantics { testTag = "android:id/OrderList" },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(reqData) { _, request ->
                        DisplayRequestItem(
                            request
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayRequestItem(
    requests: SpecialRequest
) {
    var profilePicture by remember { mutableStateOf<Uri?>(null) }
    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(requests.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    LaunchedEffect(Unit) {
        ImageService.fetchProfilePicture(requests.uid) { uri ->
            profilePicture = uri
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        Row {
            Image(
                painter = rememberImagePainter(
                    data = profilePicture),
                contentDescription = "profile_pic",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
            )

            Column (
                modifier = Modifier
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ){
                Text(
                    text = requests.name
                )

                Text(
                    text = requests.status,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
            }
        }

        Text(
            text = "Date Requested: $date"
        )

        Text(
            text = if (requests.dateAccepted.isEmpty()) "Date Accepted: N/A" else "Date Accepted: ${requests.dateAccepted}"
        )

        Text(
            text = if (requests.dateCompleted.isEmpty()) "Date Completed: N/A" else "Date Completed: ${requests.dateCompleted}"
        )

        Text(
            text = requests.subject,
            fontWeight = FontWeight.Bold,
            color = Green1,
            modifier = Modifier
                .padding(top = 6.dp)
        )
    }
}

@Composable
fun LoadingOrders() {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/LoadingOrders" }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OrdersError(errorMessage: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/OrdersError" }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error: $errorMessage",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun EmptyOrders() {
    Box(modifier = Modifier
        .fillMaxSize()
        .semantics { testTag = "android:id/EmptyOrders" }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Empty orders",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
