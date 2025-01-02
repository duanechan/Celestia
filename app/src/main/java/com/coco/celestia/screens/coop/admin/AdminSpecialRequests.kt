@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop.admin

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AdminSpecialRequests(
    specialRequestViewModel: SpecialRequestViewModel,
    status: String
) {
    val reqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val reqState by specialRequestViewModel.specialReqState.observeAsState(OrderState.LOADING)

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
            // Change to total number of orders
            Text("All(0)")

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

        when (reqState) {
            is SpecialReqState.LOADING -> LoadingOrders()
            is SpecialReqState.ERROR -> OrdersError(errorMessage = (reqState as SpecialReqState.ERROR).message)
            is SpecialReqState.EMPTY -> EmptyOrders()
            is SpecialReqState.SUCCESS -> {
                LazyColumn(modifier = Modifier.semantics { testTag = "android:id/OrderList" }) {
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

    LaunchedEffect(Unit) {
        ImageService.fetchProfilePicture(requests.uid) { uri ->
            profilePicture = uri
        }
    }

    Column {
        Row {
            Image(
                painter = rememberImagePainter(
                    data = profilePicture),
                contentDescription = "profile_pic",
                modifier = Modifier.size(40.dp)
            )

            Column {
                Text(
                    text = requests.name
                )

                Text(
                    text = requests.status
                )
            }
        }

        Text(
            text = "Date"
        )

        Text(
            text = "Date"
        )

        Text(
            text = "Date"
        )

        Text(
            text = "Subject"
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
