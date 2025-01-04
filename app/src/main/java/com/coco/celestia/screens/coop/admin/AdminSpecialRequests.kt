@file:OptIn(ExperimentalCoilApi::class, ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop.admin

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.UserData
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

    LaunchedEffect(keywords) {
        specialRequestViewModel.fetchSpecialRequests(status)
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
    val dateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    LaunchedEffect(Unit) {
        ImageService.fetchProfilePicture(request.uid) { uri ->
            profilePicture = uri
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                if (request.status == "To Review") {
                    navController.navigate(Screen.AdminSpecialRequestsDetails.createRoute(request.specialRequestUID))
                }
            }
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
                    text = request.name
                )

                Text(
                    text = request.status,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
            }
        }

        Text(
            text = "Date Requested: $date"
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

@Composable
fun SpecialRequestDetails(
    navController: NavController,
    userViewModel: UserViewModel,
    specialRequestViewModel: SpecialRequestViewModel,
    request: SpecialRequest
) {
    val usersData by userViewModel.usersData.observeAsState()
    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    var showDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    val user = usersData?.let { user ->
        user.find { it.email == request.email }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Date of Request: $date"
            )
        }

        Row (
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = "Subject",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = request.subject,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(2f)
            )
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp)
        )

        Row (
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = request.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(2f)
            )
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp)
        )

        DisplayRequestDetails(
            user,
            request
        )

        Box (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row (
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        action = "Decline"
                        showDialog = true
                    }
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = {
                        action = "Accept"
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(Green4)
                ) {
                    Text("Accept")
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false},
                title = {
                    Text("Confirmation")
                },
                text = {
                    Text("Are you sure you want to $action this request?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            if (action == "Accept") {
                                specialRequestViewModel.updateSpecialRequest(request.copy(status = "In Progress"))
                                navController.navigate(Screen.AdminSpecialRequests.createRoute("In Progress"))
                            } else {
                                specialRequestViewModel.updateSpecialRequest(request.copy(status = "Turned Down"))
                                navController.navigate(Screen.AdminSpecialRequests.createRoute("Turned Down"))
                            }
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun DisplayRequestDetails(
    user: UserData?,
    request: SpecialRequest
) {
    Text(
        text = "Request Details",
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(8.dp)
    )

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp))
            .background(Green4)
    ) {
        Text(
            text = "Product/s and Quantity",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 4.dp)
        )

        request.products.forEachIndexed { index, product ->
            Row (
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            ){
                Text(
                    text = "${index + 1}. ${product.name}: ${product.quantity} kg"
                )
            }
        }

        Text(
            text = "Target Delivery Date: ${request.targetDate}",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 4.dp)
        )

        Text(
            text = "Collection Method: ${request.collectionMethod}",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 4.dp)
        )

        if (request.collectionMethod == "Deliver") {
            Text(
                text = "Delivery Location:",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = "${user?.streetNumber}, ${user?.barangay}",
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )
        } else {
            Text(
                text = "Pick Up Location:",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = "City Vet Office, Baguio City",
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )
        }

        Text(
            text = "Additional Request/s:",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 4.dp)
        )

        Text(
            text = request.additionalRequest.ifEmpty { "N/A" },
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .padding(2.dp)
                .padding(bottom = 6.dp)
        )
    }
}
