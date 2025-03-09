@file:OptIn(ExperimentalCoilApi::class, ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop.admin

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
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
    navController: NavController,
    status: String
) {
    val reqData by specialRequestViewModel.specialReqData.observeAsState(emptyList())
    val reqState by specialRequestViewModel.specialReqState.observeAsState(OrderState.LOADING)
    var numReq by remember { mutableIntStateOf(0) }
    val keywords by remember { mutableStateOf(status) }
    var orderBy by remember { mutableStateOf("") }
    var ascending by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val statusList = listOf("To Review", "In Progress", "Cancelled", "Turned Down")
    val filteredList = statusList.filterNot { it == status }

    LaunchedEffect(keywords, orderBy, ascending) {
        orderBy = if (status == "To Review") {
            "Requested"
        } else {
            "Accepted"
        }
        specialRequestViewModel.fetchSpecialRequests(status, orderBy, ascending)
    }

    Column {
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
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            ascending = !ascending
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    Box (
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { expanded = true }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.filter2),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredList.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    navController.navigate(Screen.AdminSpecialRequests.createRoute(status))
                                }
                            )
                        }
                    }
                }
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
                            request,
                            navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayRequestItem(
    request: SpecialRequest,
    navController: NavController
) {
    var profilePicture by remember { mutableStateOf<Uri?>(null) }
    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val reqDateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val requestDate = reqDateTime.format(dateFormatter)
    var currentStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        ImageService.fetchProfilePicture(request.uid) { uri ->
            profilePicture = uri
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(Green4.copy(alpha = 0.5f))
            .clickable {
                navController.navigate(Screen.AdminSpecialRequestsDetails.createRoute(request.specialRequestUID))
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        Row {
            currentStatus = if (request.assignedMember.isEmpty()) {
                "Need to Assign Farmers"
            } else if (request.assignedMember.any { it.status == "Completed" }) {
                "Partially Fulfilled"
            } else {
                "Farmers Assigned"
            }

            Image(
                painter = rememberImagePainter(
                    data = profilePicture ?: R.drawable.profile),
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
                    text = request.name
                )

                Row {
                    Text(
                        text = if (request.status == "In Progress") "${request.status}:" else request.status,
                        fontWeight = FontWeight.Bold,
                        color = Green1
                    )

                    if (request.status == "In Progress") {
                        Text(
                            text = currentStatus,
                            fontWeight = FontWeight.Bold,
                            color = Green1,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Date Requested: $requestDate"
        )

        Text(
            text = if (request.dateAccepted.isEmpty()) "Date Accepted: N/A" else "Date Accepted: ${request.dateAccepted}"
        )

        Text(
            text = if (request.dateCompleted.isEmpty()) "Date Completed: N/A" else "Date Completed: ${request.dateCompleted}"
        )

        Text(
            text = request.subject,
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
