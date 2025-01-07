@file:OptIn(ExperimentalCoilApi::class, ExperimentalCoilApi::class)

package com.coco.celestia.screens.coop.admin

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.coco.celestia.viewmodel.model.AssignedMember
import com.coco.celestia.viewmodel.model.Constants
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
    var orderBy by remember { mutableStateOf("") }
    var ascending by remember { mutableStateOf(true) }

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
    val reqDateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val requestDate = reqDateTime.format(dateFormatter)

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
                navController.navigate(Screen.AdminSpecialRequestsDetails.createRoute(request.specialRequestUID))
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        Row {
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

                Text(
                    text = request.status,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
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

@Composable
fun SpecialRequestDetails(
    navController: NavController,
    userViewModel: UserViewModel,
    specialRequestViewModel: SpecialRequestViewModel,
    request: SpecialRequest
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    var showDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var action by remember { mutableStateOf("") }
    val usersData by userViewModel.usersData.observeAsState()
    var assignedMember = remember { mutableStateListOf(AssignedMember()) }

    var text by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    var memberEmpty by remember { mutableStateOf(false) }
    var productEmpty by remember { mutableStateOf(false) }
    var quantityEmpty by remember { mutableStateOf(false) }
    var quantityExceeded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    val user = usersData.let { user ->
        user?.find { it.email == request.email }
    }

    val filteredUsers = usersData?.filter {
        (it.role == "CoopCoffee" || it.role == "CoopMeat")
    }?.map { "${it.firstname} ${it.lastname}" } ?: emptyList()

    val productList = request.products.map { it.name }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ){
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
        //Request ID etc.
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Green4,
                    shape = RoundedCornerShape(12.dp)
                )
        ){
            Text(
                text = "Request ID",
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            )

            Text(
                text = request.specialRequestUID.split("-").take(4).joinToString("-"),
                modifier = Modifier
                    .padding(16.dp)
                    .weight(2f)
            )
        }
        // Toggle Button
        if (request.status != "To Review") {
            Row (
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Details",
                    fontWeight = FontWeight.Bold
                )

                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it},
                    modifier = Modifier
                        .graphicsLayer (
                            scaleX = 0.6f,
                            scaleY = 0.6f
                        ),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Green4,
                        checkedTrackColor = Green1,
                        uncheckedThumbColor = Green2,
                        uncheckedTrackColor = Green4
                    )
                )
            }
        }

        if (checked) {
            DisplayRequestDetails(
                user,
                request
            )
        }

        if (request.assignedFarmer.isEmpty()) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = Green4,
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Assign a Member",
                    modifier = Modifier
                        .padding(16.dp)
                )

                Image(
                    painter = painterResource(R.drawable.add),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showAddMemberDialog = true }
                )
            }
        }
        // Accept/ Decline
        if (request.status == "To Review") {
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
        } else {
            // Add track request and update request if needed
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
                                specialRequestViewModel.updateSpecialRequest(
                                    request.copy(
                                        status = "In Progress",
                                        dateAccepted = formattedDateTime
                                    )
                                )
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

        if (showAddMemberDialog) {
            AlertDialog(
                onDismissRequest = { showAddMemberDialog = false},
                title = {
                    Text("Assign a Member")
                },
                text = {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable (
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    expanded = false
                                }
                            )
                    ){
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Member",
                                modifier = Modifier.padding(2.dp)
                            )

                            if (memberEmpty) {
                                Text(
                                    text = "Member cannot be Empty",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }

                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 2.dp,
                                        color = Green2,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                value = text,
                                onValueChange = {
                                    text = it
                                    expanded = true
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            AnimatedVisibility(visible = expanded) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    LazyColumn (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        if (text.isNotEmpty()) {
                                            items(
                                                filteredUsers.filter {
                                                    it.lowercase().contains(text.lowercase())
                                                }
                                                    .sorted()
                                            ) {
                                                MemberItems(title = it) { title ->
                                                    text = title
                                                    expanded = false
                                                }
                                            }
                                        } else {
                                            items(
                                                filteredUsers.sorted()
                                            ) {
                                                MemberItems(title = it) { title ->
                                                    text = title
                                                    expanded = false
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Product",
                                modifier = Modifier.padding(2.dp)
                            )
                            if (productEmpty) {
                                Text(
                                    text = "Product cannot be Empty",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }

                        Column {
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 2.dp,
                                        color = Green2,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { productExpanded = !productExpanded },
                                value = product,
                                onValueChange = {
                                    product = it
                                    productExpanded = true
                                },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { productExpanded = !productExpanded }) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledContainerColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                                enabled = false,
                            )

                            DropdownMenu(
                                expanded = productExpanded,
                                onDismissRequest = { productExpanded = false}
                            ) {
                                productList.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            product = it
                                            productExpanded = false
                                        },
                                        text = {
                                            Text(text = it)
                                        }
                                    )
                                }
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quantity",
                                modifier = Modifier.padding(2.dp)
                            )

                            if (quantityEmpty) {
                                Text(
                                    text = "Quantity cannot be Empty",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }

                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = Green2,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            value = if (quantity == 0) "" else quantity.toString(),
                            onValueChange = { newValue ->
                                quantity = newValue.toIntOrNull() ?: 0
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            memberEmpty = text.isEmpty()
                            productEmpty = product.isEmpty()
                            quantityEmpty = quantity == 0

                            val member = AssignedMember(

                            )

                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            text = ""
                            product = ""
                            quantity = 0
                            showAddMemberDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MemberItems (
    title: String,
    onSelect: (String) -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                onSelect(title)
            }
            .padding(4.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}

@Composable
fun DisplayRequestDetails(
    user: UserData?,
    request: SpecialRequest
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
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
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Green4,
                    shape = RoundedCornerShape(12.dp)
                )
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

            if (request.collectionMethod == Constants.COLLECTION_DELIVERY) {
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
}
