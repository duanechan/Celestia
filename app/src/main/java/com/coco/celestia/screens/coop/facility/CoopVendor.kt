package com.coco.celestia.screens.coop.facility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.model.VendorData
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.VendorState
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel

@Composable
fun Vendors(
    navController: NavController,
    currentEmail: String,
    onAddVendor: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VendorViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Inactive")
    var searchQuery by remember { mutableStateOf("") }

    val vendors by viewModel.vendorData.observeAsState(emptyList())
    val vendorState by viewModel.vendorState.observeAsState(VendorState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
    ) {
        when (facilityState) {
            is FacilityState.LOADING -> {
                LoadingScreen("Loading facilities...")
            }
            is FacilityState.ERROR -> {
                ErrorScreen((facilityState as FacilityState.ERROR).message)
            }
            else -> {
                val userFacility = facilitiesData.find { facility ->
                    facility.emails.contains(currentEmail)
                }

                if (userFacility != null) {
                    LaunchedEffect(selectedTabIndex) {
                        val filter = when (selectedTabIndex) {
                            0 -> "all"
                            1 -> "active"
                            2 -> "inactive"
                            else -> "all"
                        }
                        viewModel.fetchVendors(filter, userFacility.name)
                    }

                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = onAddVendor,
                                containerColor = White1,
                                contentColor = Green1
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Vendor")
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search Vendors") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                                }
                            )

                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color.Transparent,
                                contentColor = Green1,
                                indicator = { tabPositions ->
                                    Box(
                                        Modifier
                                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                            .height(4.dp)
                                            .background(Green1, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    )
                                }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { selectedTabIndex = index },
                                        text = {
                                            Text(
                                                title,
                                                color = if (selectedTabIndex == index) Green1 else Color.Gray
                                            )
                                        }
                                    )
                                }
                            }

                            when (vendorState) {
                                VendorState.LOADING -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                VendorState.EMPTY -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No vendors found for ${userFacility.name}")
                                    }
                                }

                                VendorState.SUCCESS -> {
                                    val filteredVendors = vendors.filter {
                                        it.firstName.contains(searchQuery, ignoreCase = true) ||
                                                it.lastName.contains(searchQuery, ignoreCase = true) ||
                                                it.email.contains(searchQuery, ignoreCase = true)
                                    }

                                    if (filteredVendors.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No results found for \"$searchQuery\"")
                                        }
                                    } else {
                                        LazyColumn {
                                            items(filteredVendors) { vendor ->
                                                VendorItem(
                                                    vendor = vendor,
                                                    onClick = {
                                                        viewModel.fetchVendors("all", "", vendor.facility)
                                                        navController.navigate(Screen.CoopVendorDetails.createRoute(vendor.email))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                is VendorState.ERROR -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (vendorState as VendorState.ERROR).message,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    NoFacilityScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorItem(
    vendor: VendorData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 5.dp, start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = White1
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${vendor.firstName} ${vendor.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Text(
                        text = vendor.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green1
                    )
                    Text(
                        text = if (vendor.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (vendor.isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun VendorDetailsScreen(
    email: String,  // Changed parameter to email
    viewModel: VendorViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vendorState by viewModel.vendorState.observeAsState()
    val vendorData by viewModel.vendorData.observeAsState(emptyList())
    val selectedVendor = vendorData.find { it.email == email }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
    ) {
        when (vendorState) {
            is VendorState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is VendorState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (vendorState as VendorState.ERROR).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is VendorState.SUCCESS -> {
                if (selectedVendor != null) {
                    VendorDetailContent(
                        vendor = selectedVendor,
                        modifier = modifier
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Vendor not found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(
                                onClick = onNavigateUp,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green1
                                )
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
            is VendorState.EMPTY -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No vendor data available")
                }
            }
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...")
                }
            }
        }
    }
}

@Composable
private fun VendorDetailContent(
    vendor: VendorData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("DETAILS", "TRANSACTIONS", "HISTORY")

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Green1,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp)
                        .background(Green1, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Green1 else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> VendorDetailsTab(vendor)
            1 -> TransactionsTab()
            2 -> CommentsHistoryTab()
        }
    }
}

@Composable
private fun VendorDetailsTab(vendor: VendorData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = White1
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Basic Info Section
                Text(
                    text = "${vendor.firstName} ${vendor.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (vendor.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (vendor.isActive) Green1 else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Company Info
                InfoRow(
                    icon = Icons.Default.Info,
                    label = "Company",
                    value = vendor.companyName
                )

                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = vendor.email
                )

                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = vendor.phoneNumber
                )

                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = vendor.address
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // More Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = White1
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                var isExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "More Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Green1
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Remarks",
                            style = MaterialTheme.typography.titleSmall,
                            color = Green1,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = vendor.remarks.ifEmpty { "No remarks available" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green1,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value.ifEmpty { "Not provided" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TransactionsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Transactions Coming Soon")
    }
}

@Composable
private fun CommentsHistoryTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Comments & History Coming Soon")
    }
}