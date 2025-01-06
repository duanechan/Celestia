package com.coco.celestia.screens.coop.facility

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.PurchaseOrderState
import com.coco.celestia.viewmodel.PurchaseOrderViewModel
import com.coco.celestia.viewmodel.model.PurchaseOrder
import com.coco.celestia.viewmodel.model.PurchaseOrderItem
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel

// TODO: filtering options

@Composable
fun CoopPurchases(
    navController: NavController,
    currentEmail: String,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    facilityViewModel: FacilityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    var hasInitialFetch by remember { mutableStateOf(false) }

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
                    LaunchedEffect(userFacility.name, searchQuery) {
                        if (!hasInitialFetch || searchQuery.isNotEmpty()) {
                            purchaseOrderViewModel.fetchPurchaseOrders(
                                filter = if (selectedTab == "Draft") "draft" else "all",
                                searchQuery = searchQuery,
                                facilityName = userFacility.name
                            )
                            hasInitialFetch = true
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { newQuery ->
                                    searchQuery = newQuery
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Search purchase orders...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                        }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    cursorColor = Green1,
                                    focusedBorderColor = Green1,
                                    unfocusedBorderColor = Green1,
                                    focusedLabelColor = Green1,
                                    unfocusedLabelColor = Green1,
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { /* Handle filter click */ },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = White1,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Filter",
                                    tint = Green1
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                val tabs = listOf("All", "Draft")
                                tabs.forEachIndexed { index, tab ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        TextButton(
                                            onClick = { selectedTab = tab },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedTab == tab)
                                                    Green1
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Text(
                                                text = tab,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = selectedTab == tab,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .fillMaxWidth()
                                                    .height(2.dp)
                                                    .background(
                                                        color = Green1,
                                                        shape = RoundedCornerShape(1.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Content
                        val purchaseOrderState = purchaseOrderViewModel.purchaseOrderState.value
                        val purchaseOrders = purchaseOrderViewModel.purchaseOrderData.value

                        val filteredOrders = when (selectedTab) {
                            "Draft" -> purchaseOrders?.filter { it.savedAsDraft }
                            else -> purchaseOrders?.filter { !it.savedAsDraft }
                        }

                        when {
                            purchaseOrderState is PurchaseOrderState.LOADING && purchaseOrders.isNullOrEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Green1)
                                }
                            }
                            !filteredOrders.isNullOrEmpty() -> {
                                LazyColumn {
                                    items(filteredOrders) { purchaseOrder ->
                                        PurchaseOrderCard(
                                            purchaseOrder = purchaseOrder,
                                            onClick = {
                                                if (purchaseOrder.savedAsDraft) {
                                                    navController.navigate(Screen.CoopPurchaseForm.createRouteWithDraft(purchaseOrder.purchaseNumber))
                                                } else {
                                                    navController.navigate(Screen.CoopPurchaseDetails.createRoute(purchaseOrder.purchaseNumber))
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            purchaseOrderState is PurchaseOrderState.EMPTY || filteredOrders?.isEmpty() == true -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (selectedTab == "Draft")
                                            "No draft orders found for ${userFacility.name}"
                                        else
                                            "No purchase orders found for ${userFacility.name}"
                                    )
                                }
                            }
                            purchaseOrderState is PurchaseOrderState.ERROR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(purchaseOrderState.message)
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.CoopPurchaseForm.createRoute()) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = White1,
                        contentColor = Green1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Purchase Order"
                        )
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
fun PurchaseOrderCard(
    purchaseOrder: PurchaseOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = purchaseOrder.vendor.ifBlank { "No Vendor Yet" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "PHP${calculateTotalAmount(purchaseOrder.items)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${purchaseOrder.dateAdded} • ${purchaseOrder.purchaseNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reference #: ${purchaseOrder.referenceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Expected Delivery: ${purchaseOrder.expectedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PurchaseOrderDetailsScreen(
    purchaseNumber: String,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    facilityName: String,
    navController: NavController,
    onNavigateUp: () -> Unit,
) {
    val purchaseOrderState by purchaseOrderViewModel.purchaseOrderState.observeAsState()
    val purchaseOrderData by purchaseOrderViewModel.purchaseOrderData.observeAsState(emptyList())
    val selectedPurchaseOrder = purchaseOrderData.find { it.purchaseNumber == purchaseNumber }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
    ) {
        when (purchaseOrderState) {
            is PurchaseOrderState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }
            is PurchaseOrderState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = (purchaseOrderState as PurchaseOrderState.ERROR).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
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
            is PurchaseOrderState.SUCCESS -> {
                if (selectedPurchaseOrder != null) {
                    PurchaseDetails(
                        purchaseOrder = selectedPurchaseOrder,
                        navController = navController,
                        purchaseOrderViewModel = purchaseOrderViewModel,
                        onDelete = {
                            // Handle delete
                            purchaseOrderViewModel.deletePurchaseOrder(
                                purchaseOrderNumber = selectedPurchaseOrder.purchaseNumber,
                                onSuccess = {
                                    onNavigateUp()
                                }
                            )
                        },
                        onMarkAsCancelled = {
                            purchaseOrderViewModel.updatePurchaseOrderStatus(
                                purchaseOrderNumber = selectedPurchaseOrder.purchaseNumber,
                                newStatus = "cancelled",
                                onSuccess = {
                                    purchaseOrderViewModel.fetchPurchaseOrders(facilityName)
                                }
                            )
                        }
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
                                text = "Purchase order not found",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
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
            is PurchaseOrderState.EMPTY -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No purchase order data available",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
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
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }
        }
    }
}

@Composable
fun PurchaseDetails(
    purchaseOrder: PurchaseOrder,
    navController: NavController,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    onDelete: () -> Unit = {},
    onMarkAsCancelled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("DETAILS", "HISTORY")

    // Delete Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Purchase Order") },
            text = { Text("Are you sure you want to delete this purchase order?") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        showDeleteConfirmation = false
                        purchaseOrderViewModel.deletePurchaseOrder(
                            purchaseOrderNumber = purchaseOrder.purchaseNumber,
                            onSuccess = {
                                isDeleting = false
                                onDelete()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                isDeleting = false
                                errorMessage = error
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error Dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Loading Dialog
    if (isDeleting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Deleting Purchase Order") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White2)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White1)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = purchaseOrder.purchaseNumber,
                    style = MaterialTheme.typography.titleLarge,
                    color = Green1
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Green1
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                navController.navigate(Screen.CoopPurchaseForm.createRouteForEdit(purchaseOrder.purchaseNumber))
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showDeleteConfirmation = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark as Cancelled") },
                            onClick = {
                                onMarkAsCancelled()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Mark as Cancelled"
                                )
                            }
                        )
                    }
                }
            }

            Text(
                text = if (purchaseOrder.vendor.isNotBlank()) purchaseOrder.vendor else "No Vendor Yet",
                style = MaterialTheme.typography.titleLarge,
                color = Green1,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = purchaseOrder.status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = Green1,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Total Amount",
                style = MaterialTheme.typography.titleMedium,
                color = Green1,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "PHP${calculateTotalAmount(purchaseOrder.items)}",
                style = MaterialTheme.typography.headlineMedium,
                color = Green1
            )

            // Reference Number Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reference#",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
                Text(
                    text = purchaseOrder.referenceNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
            }

            // Order Date Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order Date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
                Text(
                    text = purchaseOrder.dateAdded,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
            }

            // Expected Delivery Date Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expected Delivery Date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
                Text(
                    text = purchaseOrder.expectedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Green1
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = White1,
            contentColor = Green1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clipToBounds(),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Green1,
                    height = 2.dp
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedTab == index) Green1
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> DetailsTab(purchaseOrder)
            1 -> CommentsHistoryTab()
        }
    }
}

@Composable
private fun DetailsTab(purchaseOrder: PurchaseOrder) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // More Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White1
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ExpandableSection(
                        title = "More Information",
                        initiallyExpanded = false
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Shipment preference",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Green1
                            )
                            Text(
                                text = purchaseOrder.shipmentPreference,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Green1,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Items Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),  // Added bottom padding here
                colors = CardDefaults.cardColors(
                    containerColor = White1
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Items",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Green4)
                    purchaseOrder.items.forEach { item ->
                        ItemRow(item)
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Green4)
                    }

                    // Totals
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sub Total",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "PHP${calculateTotalAmount(purchaseOrder.items)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "PHP${calculateTotalAmount(purchaseOrder.items)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ItemRow(item: PurchaseOrderItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${item.quantity} x PHP${String.format("%.2f", item.rate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "PHP${String.format("%,.2f", item.quantity * item.rate)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            content()
        }
    }
}

@Composable
private fun CommentsHistoryTab() {
    // Placeholder for Comments & History tab content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("History Content")
    }
}

@SuppressLint("DefaultLocale")
private fun calculateTotalAmount(items: List<PurchaseOrderItem>): String {
    val total = items.sumOf { it.quantity * it.rate }
    return String.format("%,.2f", total)
}