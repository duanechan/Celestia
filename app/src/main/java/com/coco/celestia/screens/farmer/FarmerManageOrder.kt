@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package com.coco.celestia.screens.farmer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun FarmerManageOrder(
    navController: NavController,
    userViewModel: UserViewModel,
    specialRequestViewModel: SpecialRequestViewModel
) {
    val userData by userViewModel.userData.observeAsState()
    val assignedProducts by specialRequestViewModel.specialReqData.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    var searchQuery by remember { mutableStateOf("") }
    var tabName by remember { mutableStateOf("In Progress") }
    var farmerStatus by remember { mutableStateOf("All") }

    LaunchedEffect(tabName) {
        userViewModel.fetchUser(uid)
        specialRequestViewModel.fetchAssignedProducts(
            userData?.email ?: "",
            tabName
        )
    }

    Spacer(modifier = Modifier.width(30.dp))

    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        val pagerState = rememberPagerState (
            pageCount = { 2 }
        )
        val coroutineScope = rememberCoroutineScope()

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Green4,
            contentColor = Green1,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = Green1
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                text = {
                    Text(text = "In Progress")
                },
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                text = {
                    Text(text = "Completed")
                },
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = BgColor)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search orders...", color = DarkGreen) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Cocoa
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .background(color = White2, shape = RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, color = DarkGreen), shape = RoundedCornerShape(16.dp)),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedTextColor = Cocoa,
                            unfocusedTextColor = DarkGreen
                        )
                    )
                }

                if (page == 0) {
                    // Filter section for "In Progress" tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statuses = listOf(
                            "All", "Soil Preparation", "Seed Sowing", "Growing",
                            "Pre-Harvest", "Harvesting", "Post-Harvest", "Picked up by Coop"
                        )

                        statuses.forEach { status ->
                            Button(
                                onClick = {
                                    farmerStatus = status
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (farmerStatus == status) Green3 else Green5,
                                    contentColor = if (farmerStatus == status) Color.White else DarkGreen
                                )
                            ) {
                                Text(text = status)
                            }
                        }
                    }
                }

                tabName = if (page == 0) {
                    "In Progress"
                } else {
                    "Completed"
                }

                assignedProducts
                    ?.filter { member ->
                        farmerStatus == "All" ||
                                member.assignedMember.any { it.status.equals(farmerStatus, ignoreCase = true) }
                    }
                    ?.filter { member ->
                        searchQuery.isEmpty() ||
                                member.subject.contains(searchQuery, ignoreCase = true) ||
                                member.name.contains(searchQuery, ignoreCase = true) ||
                                member.assignedMember.any { it.status.contains(searchQuery, ignoreCase = true) } ||
                                member.products.any { it.name.contains(searchQuery, ignoreCase = true) }
                    }
                    ?.sortedByDescending { assigned ->
                        assigned.trackRecord
                            .filter { it.description.contains("assigned", ignoreCase = true) }
                            .maxByOrNull { it.dateTime }
                            ?.dateTime
                    }
                    ?.forEach { assigned ->
                        DisplayRequestCard(
                            navController,
                            assigned,
                            userData?.email ?: ""
                        )
                    }
            }
        }
    }
}

@Composable
fun DisplayRequestCard(
    navController: NavController,
    specialRequest: SpecialRequest,
    farmerEmail: String
) {
    val assignedMember = specialRequest.assignedMember.find { it.email == farmerEmail }
    val status = assignedMember?.status ?: ""
    val normalizedStatus = status.uppercase()

    val backgroundColor = when (normalizedStatus) {
        "SOIL PREPARATION" -> Green6
        "SEED SOWING" -> Green7
        "GROWING" -> Green9
        "PRE-HARVEST" -> Green8
        "HARVESTING" -> Green10
        "POST-HARVEST" -> Green11
        "PICKED UP BY COOP" -> Green
        "COMPLETED" -> SageGreen.copy(alpha = 0.7f)
        "CALAMITY AFFECTED" -> SolidRed
        "CANCELLED" -> Copper3
        else -> {
            Color.Gray
        }
    }

    val iconPainter: Painter? = when (normalizedStatus) {
        "SOIL PREPARATION" -> painterResource(id = R.drawable.plant_hand)
        "SEED SOWING" -> painterResource(id = R.drawable.plant)
        "GROWING" -> painterResource(id = R.drawable.planting)
        "PRE-HARVEST" -> painterResource(id = R.drawable.harvest)
        "HARVESTING" -> painterResource(id = R.drawable.harvest_basket)
        "POST-HARVEST" -> painterResource(id = R.drawable.harvested)
        "PICKED UP BY COOP" -> painterResource(id = R.drawable.deliveryicon)
        "COMPLETED" -> painterResource(id = R.drawable.received)
        "CANCELLED" -> painterResource(id = R.drawable.cancelled)
        "CALAMITY AFFECTED" -> painterResource(id = R.drawable.calamity)
        else -> null
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        border = BorderStroke(1.dp, Green2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true)
            ) {
                navController.navigate(
                    Screen.FarmerRequestCardDetails.createRoute(
                        specialRequest.specialRequestUID,
                        farmerEmail,
                        assignedMember?.product ?: ""
                    )
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (iconPainter != null) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = status,
                                modifier = Modifier.size(24.dp),
                                tint = Cocoa
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status.replace("_", " "),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa
                        )
                    }
                }
            }
            Text(
                text = specialRequest.products.joinToString(", ") { it.name },
                fontSize = 24.sp,
                color = Green1
            )
            Text(
                text = specialRequest.name,
                color = Green2
            )
        }
    }
}

@Composable
fun DisplayRequestDetails (
    specialRequestViewModel: SpecialRequestViewModel,
    specialRequestUID: String,
    farmerEmail: String,
    product: String
) {
    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchSpecialRequests("", "", true)
    }

    val specialRequests by specialRequestViewModel.specialReqData.observeAsState()
    val specialRequest = specialRequests?.find { it.specialRequestUID == specialRequestUID }

    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(specialRequest?.dateRequested ?: "", inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    var checked by remember { mutableStateOf(true) }
    var updateStatusDialog by remember { mutableStateOf(false) }

    val trackRecord = remember { mutableStateListOf(*specialRequest?.trackRecord!!.toTypedArray()) }
    val farmerTrackRecord = remember {
        val farmer = specialRequest?.assignedMember?.find { it.email == farmerEmail }
        mutableStateListOf(*(farmer?.farmerTrackRecord?.toTypedArray() ?: emptyArray()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        Row(
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
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text(
                        text = "Date of Request: ",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = date
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "Request ID: ",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = specialRequest?.specialRequestUID?.split("-")?.take(4)
                            ?.joinToString("-") ?: ""
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Details",
                fontWeight = FontWeight.Bold
            )

            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier
                    .graphicsLayer(
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

        if (checked) {
            if (specialRequest != null) {
                DisplayDetails(
                    specialRequest = specialRequest
                )
            }
        }

        Text(
            text = "Assigned to you",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )

        specialRequest?.assignedMember?.forEach { member ->
            if (member.email == farmerEmail && member.product == product) {
                Row(
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
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = member.product,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    Text(
                        text = "${member.quantity}kg",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Assign Milestone
                Button(
                    onClick = {
                        updateStatusDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Green1
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Assign Milestone",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Notify Unforeseen Circumstances
                Button(
                    onClick = {
                        // Handle "Notify Unforeseen Circumstances" action
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Green1
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Notify Unforeseen Circumstances",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Track Progress
            Button(
                onClick = {
                    // Track order action
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = Green1
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Track Progress")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.deliveryicon),
                        contentDescription = "Track Order Icon",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (updateStatusDialog) {
        if (specialRequest != null) {
            DisplayUpdateStatus(
                product = product,
                onConfirm = { status, description ->
                    specialRequest.assignedMember.map { member ->
                        if (member.email == farmerEmail) {
                            val addTrack = TrackRecord(
                                description = "Farmer ${member.name} status: $status",
                                dateTime = formattedDateTime
                            )
                            val addFarmerTrack = TrackRecord(
                                description = "In Progress ($status): $description",
                                dateTime = formattedDateTime
                            )
                            farmerTrackRecord.add(addFarmerTrack)
                            trackRecord.add(addTrack)
                        }
                    }

                    specialRequestViewModel.updateSpecialRequest(
                        specialRequest.copy(
                            assignedMember = specialRequest.assignedMember.map { member ->
                                if (member.email == farmerEmail && member.product == product) {
                                    member.copy(
                                        status = status,
                                        farmerTrackRecord = farmerTrackRecord
                                    )
                                } else {
                                    member
                                }
                            },
                            trackRecord = trackRecord
                        )
                    )


                    updateStatusDialog = false
                },
                onDismiss = { updateStatusDialog = false}
            )
        }
    }
}

//Changed Status to Milestone
@Composable
fun DisplayUpdateStatus (
    product: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }
    val statusList = listOf("Soil Preparation","Seed Sowing", "Growing", "Pre-Harvest", "Harvesting",
        "Post-Harvest", "Picked Up by Coop", "Calamity-Affected")


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Milestone For:") },
        text = {
            Column {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = Green2,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    label = { Text("Product") },
                    value = product,
                    onValueChange = { },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    enabled = false,
                )

                if (product.isNotEmpty()) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .border(
                                width = 2.dp,
                                color = Green2,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { statusExpanded = !statusExpanded },
                        label = { Text("Select Milestone") },
                        value = status,
                        onValueChange = {
                            status = it
                            statusExpanded = true
                        },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = !statusExpanded }) {
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
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false}
                    ) {
                        statusList.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    status = it
                                    statusExpanded = false
                                },
                                text = {
                                    Text(text = it)
                                }
                            )
                        }
                    }
                }

                if (status.isNotEmpty()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Update Progress") },
                        placeholder = { Text("Update the cooperative with your progress...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedIndicatorColor = Green1,
                            unfocusedIndicatorColor = Green4
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(status, description) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DisplayDetails (
    specialRequest: SpecialRequest
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
                text = specialRequest.subject,
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
                text = specialRequest.description,
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            specialRequest.products.forEachIndexed { index, product ->
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            ) {
                Text(
                    text = "Target Delivery Date: ",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
                Text(
                    text = specialRequest.targetDate
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Collection Method: ",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
                Text(
                    text = specialRequest.collectionMethod,
                )
            }
            if (specialRequest.collectionMethod == Constants.COLLECTION_DELIVERY) {
                Text(
                    text = "Delivery Location:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "Pick Up Location:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            }

            Text(
                text = specialRequest.deliveryAddress,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )

            Text(
                text = "Additional Request/s:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = specialRequest.additionalRequest.ifEmpty { "N/A" },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

////DON'T REMOVE ATM
//@Composable
//fun FarmerManageRequest(
//    navController: NavController,
//    userData: UserData?,
//    orderData: List<OrderData>,
//    orderState: OrderState,
//    searchQuery: String,
//    selectedCategory: String
//) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        when (orderState) {
//            is OrderState.LOADING -> {
//                Text("Loading pending orders...")
//            }
//            is OrderState.ERROR -> {
//                Text("Failed to load orders: ${orderState.message}")
//            }
//            is OrderState.EMPTY -> {
//                Text("No pending orders available.")
//            }
//            is OrderState.SUCCESS -> {
//                val filteredOrders = orderData
//                    .filter { order ->
//                        order.status.equals("PENDING", ignoreCase = true) ||
//                                (order.status == "PARTIALLY_FULFILLED" &&
//                                        order.orderData[0].quantity - order.partialQuantity != 0)
//                    }
//                    .filter { order ->
//                        order.orderId.contains(searchQuery, ignoreCase = true) &&
//                                (selectedCategory.isEmpty() || order.orderData[0].name.equals(selectedCategory, ignoreCase = true))
//                    }
//
//                if (filteredOrders.isEmpty()) {
//                    Text("No pending orders available.")
//                } else {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    ) {
//                        filteredOrders.forEach { order ->
//                            if (userData == null) {
//                                CircularProgressIndicator()
//                            } else {
//                                RequestCards(navController, order)
//                                Spacer(modifier = Modifier.height(8.dp))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

//@Composable
//fun ManageOrderCards(
//    navController: NavController,
//    order: OrderData,
//    farmerName: String,
//    cardHeight: Dp = 150.dp,
//    showStatus: Boolean = true
//) {
//    val clientName = order.client
//    val orderId = order.orderId
//    var displayStatus by remember { mutableStateOf("") }
//    val fulfilledByFarmer = order.fulfilledBy.find { it.farmerName == farmerName }
//
//    if (order.status == "PARTIALLY_FULFILLED") {
//        if (fulfilledByFarmer != null) {
//            displayStatus = fulfilledByFarmer.status
//        }
//    } else {
//        displayStatus = if (order.status == "RECEIVED") "COMPLETED" else order.status
//    }
//
//    val backgroundColor = when (displayStatus) {
////        "ACCEPTED" -> SageGreen
//        "PLANTING" -> Tangerine
//        "PLANTED" -> DeepTangerine
//        "GROWING" -> BrownTangerine
//        "READY TO HARVEST" -> Brown2
//        "HARVESTING" -> Brown3
//        "HARVESTED" -> Brown1
//        "PICKED UP BY COOP" -> Green
////        "PICKUP" -> Blue
//        "COMPLETED" -> SageGreen.copy(alpha = 0.7f)
//        "CALAMITY AFFECTED" -> SolidRed
//        "REJECTED" -> NylonRed.copy(alpha = 0.4f)
//        "CANCELLED" -> Copper3
////        "HARVESTING_MEAT" -> Brown1
//        else -> Color.Gray
//    }
//
//    val iconPainter: Painter? = when (displayStatus) {
////        "ACCEPTED" -> painterResource(id = R.drawable.preparing)
//        "PLANTING" -> painterResource(id = R.drawable.plant_hand)
//        "PLANTED" -> painterResource(id = R.drawable.plant)
//        "GROWING" -> painterResource(id = R.drawable.planting)
//        "READY TO HARVEST" -> painterResource(id = R.drawable.harvest)
//        "HARVESTING" -> painterResource(id = R.drawable.harvest_basket)
//        "HARVESTED" -> painterResource(id = R.drawable.harvested)
////        "HARVESTING_MEAT" -> painterResource(id = R.drawable.cow_animal)
//        "PICKED UP BY COOP" -> painterResource(id = R.drawable.deliveryicon)
////        "PICKUP" -> painterResource(id = R.drawable.pickup)
//        "CANCELLED" -> painterResource(id = R.drawable.cancelled)
//        "CALAMITY AFFECTED" -> painterResource(id = R.drawable.calamity)
//        else -> null
//    }
//
//    val iconVector: ImageVector = when (displayStatus) {
//        "REJECTED" -> Icons.Default.Clear
//        "COMPLETED" -> Icons.Default.CheckCircle
//        else -> Icons.Default.Warning
//    }
//
//    Row(
//        modifier = Modifier
//            .padding(horizontal = 16.dp, vertical = 2.dp)
//            .semantics { testTag = "android:id/manageOrderRow_${order.orderId}" }
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(cardHeight)
//                .semantics { testTag = "android:id/manageOrderCard+${order.orderId}" },
//            shape = RoundedCornerShape(12.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = Color.Transparent
//            ),
//            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .background(
//                        Brush.horizontalGradient(
//                            colors = listOf(Green3, Green4)
//                        )
//                    )
//                    .fillMaxSize()
//                    .clickable {
//                        navController.navigate(Screen.FarmerOrderDetails.createRoute(order.orderId))
//                    }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .padding(20.dp)
//                        .fillMaxSize(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(end = 8.dp),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.Start
//                    ) {
//                        if (showStatus) {
//                            Box(
//                                modifier = Modifier
//                                    .background(backgroundColor, shape = RoundedCornerShape(16.dp))
//                                    .padding(horizontal = 8.dp, vertical = 4.dp)
//                                    .semantics { testTag = "android:id/orderStatusRow_${order.orderId}" }
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    if (iconPainter != null) {
//                                        Icon(
//                                            painter = iconPainter,
//                                            contentDescription = displayStatus,
//                                            modifier = Modifier
//                                                .size(24.dp)
//                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" },
//                                            tint = Cocoa
//                                        )
//                                    } else {
//                                        Icon(
//                                            imageVector = iconVector,
//                                            contentDescription = displayStatus,
//                                            tint = Cocoa,
//                                            modifier = Modifier
//                                                .size(24.dp)
//                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" }
//                                        )
//                                    }
//
//                                    Spacer(modifier = Modifier.width(6.dp))
//
//                                    Text(
//                                        text = displayStatus.replace("_", " "),
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Cocoa,
//                                        modifier = Modifier
//                                            .semantics { testTag = "android:id/statusText_${order.orderId}" }
//                                    )
//                                }
//                            }
//                        }
//                        Spacer(modifier = Modifier.height(10.dp))
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 4.dp, start = 10.dp)
//                                .semantics { testTag = "android:id/orderInfoColumn_${order.orderId}" },
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Text(
//                                text = "Order ID: $orderId",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Cocoa,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                modifier = Modifier.semantics { testTag = "android:id/orderIdText_${order.orderId}" }
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = "Client Name: $clientName",
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Cocoa,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                modifier = Modifier.semantics { testTag = "android:id/clientNameText_${order.orderId}" }
//                            )
//                        }
//                    }
//
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "Arrow Icon",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .padding(end = 8.dp)
//                            .align(Alignment.CenterVertically),
//                        tint = Cocoa
//                    )
//                }
//            }
//        }
//        Spacer(modifier = Modifier.width(8.dp))
//    }
//}

//@Composable
//fun RequestCards(
//    navController: NavController,
//    order: OrderData,
//) {
//    val clientName = order.client
//    val orderId = order.orderId.substring(6, 10).uppercase()
//    val displayStatus = when (order.status) {
//        "PENDING" -> "PENDING"
//        "PARTIALLY_FULFILLED" -> "PARTIALLY FULFILLED"
//        else -> "Unknown"
//    }
//
//    val backgroundColor = when (order.status) {
//        "PENDING" -> Sand2
//        "PARTIALLY_FULFILLED" -> Tangerine
//        else -> Color.Gray
//    }
//
//    Row(
//        modifier = Modifier
//            .padding(horizontal = 16.dp, vertical = 2.dp)
//            .semantics { testTag = "android:id/requestRow_${order.orderId}" }
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(180.dp)
//                .semantics { testTag = "android:id/requestCard_${order.orderId}" },
//            shape = RoundedCornerShape(12.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = Color.Transparent
//            ),
//            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .background(
//                        Brush.horizontalGradient(
//                            colors = listOf(PaleGold, GoldenYellow)
//                        )
//                    )
//                    .fillMaxSize()
//                    .clickable {
//                        navController.navigate(Screen.FarmerRequestDetails.createRoute(order.orderId))
//                    }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .padding(20.dp)
//                        .fillMaxSize(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(end = 8.dp),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.Start
//                    ) {
//
//                        Box(
//                            modifier = Modifier
//                                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                                .semantics { testTag = "android:id/requestStatusBox_${order.orderId}" }
//                        ) {
//                            Text(
//                                text = displayStatus,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Cocoa,
//                                modifier = Modifier
//                                    .semantics { testTag = "android:id/requestStatusText_${order.orderId}" }
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(10.dp))
//
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 4.dp, start = 10.dp)
//                                .semantics { testTag = "android:id/requestInfoColumn_${order.orderId}" },
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Text(
//                                text = "Order ID: $orderId",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Cocoa,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                modifier = Modifier.semantics { testTag = "android:id/requestOrderIdText_${order.orderId}" }
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = "Client Name: $clientName",
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Cocoa,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                modifier = Modifier.semantics { testTag = "android:id/requestClientNameText_${order.orderId}" }
//                            )
//                        }
//                    }
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "Arrow Forward",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .padding(end = 8.dp)
//                            .align(Alignment.CenterVertically),
//                        tint = Cocoa
//                    )
//                }
//            }
//        }
//        Spacer(modifier = Modifier.width(8.dp))
//    }