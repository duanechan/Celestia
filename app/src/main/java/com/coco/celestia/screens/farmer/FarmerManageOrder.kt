@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)

package com.coco.celestia.screens.farmer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.Painter
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.AssignedMember
import com.coco.celestia.viewmodel.model.SpecialRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
        specialRequestViewModel.fetchAssignedProducts(
            userData?.email ?: ""
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statuses = listOf(
                            "All", "Soil Preparation", "Seed Sowing", "Growing",
                            "Pre-Harvest", "Harvesting", "Post-Harvest",
                            "Delivering to Coop", "Calamity Affected"
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
                    ?.asSequence()
                    ?.filter { member ->
                        if (tabName == "Completed") {
                            member.assignedMember.any { it.status == "Completed" }
                        } else {
                            member.status == "In Progress" && member.assignedMember.any { it.email == userData?.email && it.status != "Completed" }
                        }
                    }
                    ?.mapNotNull { member ->
                        if (tabName == "Completed") {
                            val filteredAssigned = member.assignedMember.filter { it.status == "Completed" }
                            if (filteredAssigned.isNotEmpty()) {
                                member.copy(assignedMember = filteredAssigned)
                            } else {
                                null
                            }
                        } else {
                            val filteredNotCompleted = member.assignedMember.filter {
                                it.email == userData?.email && it.status != "Completed"
                            }

                            if (filteredNotCompleted.isNotEmpty()) {
                                member.copy(assignedMember = filteredNotCompleted)
                            } else {
                                null
                            }
                        }
                    }
                    ?.filter { member ->
                        farmerStatus == "All" ||
                                member.assignedMember.any { it.status.equals(farmerStatus, ignoreCase = true)}
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
                    ?.toList()
                    ?.forEach { assigned ->
                        assigned.assignedMember
                            .filter { it.email == userData?.email }
                            .forEach { member ->
                                DisplayRequestCard(
                                    navController,
                                    assigned,
                                    userData?.email ?: "",
                                    member
                                )
                            }
                    }
            }
        }
    }
}

@Composable
fun DisplayRequestCard(
    navController: NavController,
    specialRequest: SpecialRequest,
    farmerEmail: String,
    member: AssignedMember
) {
    val status = member.status
    val product = member.product
    val normalizedStatus = status.uppercase()
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")

    val iconPainter: Painter? = when (normalizedStatus) {
        "SOIL PREPARATION" -> painterResource(id = R.drawable.plant_hand)
        "SEED SOWING" -> painterResource(id = R.drawable.plant)
        "GROWING" -> painterResource(id = R.drawable.planting)
        "PRE-HARVEST" -> painterResource(id = R.drawable.harvest)
        "HARVESTING" -> painterResource(id = R.drawable.harvest_basket)
        "POST-HARVEST" -> painterResource(id = R.drawable.harvested)
        "DELIVERING TO COOP" -> painterResource(id = R.drawable.deliveryicon)
        "COMPLETED" -> painterResource(id = R.drawable.received)
        "CALAMITY AFFECTED" -> painterResource(id = R.drawable.calamity)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                        product
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
            Text(
                text = product,
                fontSize = 24.sp,
                color = Green1
            )
            Text(
                text = specialRequest.name,
                color = Green2
            )

            if (status == "Growing") {
                val latestDateStr = member
                    .farmerTrackRecord
                    .maxByOrNull { LocalDateTime.parse(it.dateTime, dateFormatter) }
                    ?.dateTime

                val latestDate = LocalDateTime.parse(latestDateStr, dateFormatter)
                val currentDate = LocalDateTime.now()
                val daysDifference = ChronoUnit.DAYS.between(latestDate, currentDate)

                Text(
                    text = "Last Updated: $daysDifference Days ago",
                    color = Green2
                )

                if (daysDifference >= 10) {
                    Text(
                        text = "! Your last update is $daysDifference Days ago. The Cooperative is asking for a feedback. Please update your status.",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    }
}