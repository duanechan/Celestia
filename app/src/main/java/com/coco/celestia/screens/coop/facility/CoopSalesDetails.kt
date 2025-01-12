package com.coco.celestia.screens.coop.facility

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.SalesState
import com.coco.celestia.viewmodel.SalesViewModel

@Composable
fun CoopSalesDetails(
    navController: NavController,
    userEmail: String,
    salesViewModel: SalesViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel()
) {
    val salesNumber = remember {
        navController.currentBackStackEntry?.arguments?.getString("salesNumber") ?: ""
    }

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    val salesState by salesViewModel.salesState.observeAsState(SalesState.LOADING)
    val salesData by salesViewModel.salesData.observeAsState(emptyList())
    val userFacility = facilitiesData.find { facility ->
        facility.emails.contains(userEmail)
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    LaunchedEffect(userFacility) {
        userFacility?.let { facility ->
            salesViewModel.fetchSales(facility = facility.name)
        }
    }

    val currentSale = salesData.find { it.salesNumber == salesNumber }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            facilityState is FacilityState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }
            facilityState is FacilityState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((facilityState as FacilityState.ERROR).message)
                }
            }
            userFacility == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No facility found for user")
                }
            }
            else -> {
                when (salesState) {
                    SalesState.LOADING -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Green1)
                        }
                    }
                    is SalesState.ERROR -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((salesState as SalesState.ERROR).message)
                        }
                    }
                    else -> {
                        currentSale?.let { sale ->
//                            InStoreSalesDetails() //hindi ko mapass ung object
                            OnlineSalesDetails()
                        }
                    }
                }
            }

        }
    }
}

//ONLINE
@Composable
fun OnlineSalesDetails(){
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
            .verticalScroll(rememberScrollState())
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Client Name", //sale.productName
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Green1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OID-YYYYDDMM-Count",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Timestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Details Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White1)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        //Product Order Details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Items (2)",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OnlineItemCard()
                OnlineItemCard()

                Text(
                    text = "Total: PHP 200",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        //TODO: Update
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CollectionMethod()
            }
        }

        //Payment Method - TBC
        //TODO: Update
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod()
            }
        }

        //Order Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White1)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track Order",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Update Button
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Green1)
                ) {
                    Text(text = "Update", color = Color.White)
                }
            }

            //TODO: Recent ones should be on top
            // SAMPLE
            OrderStatus(
                status = "Confirmed",
                statusDescription = "Your item is now confirmed.",
                dateTime = "Jan 12 2025 03:30 PM",
            )
            OrderStatus(
                status = "Pending",
                statusDescription = "Your item is to be confirmed.",
                dateTime = "Jan 12 2025 03:20 PM",
            )

            // Dialog for Update Button
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(text = "Update Order Status")
                    },
                    text = {
                        UpdateStatusCard(
                            status = "Confirmed",
                            statusDescription = "Your item is now confirmed.",
                            dateTime = "Jan 12 2025 03:30 PM",
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text(text = "Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnlineItemCard(){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = White2)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Image Box
                    Card(
                        modifier = Modifier
                            .size(60.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Add\nImage", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall )
                        }
                    }

                    // Product Name and Price
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Potato",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "10 kg x PHP 10",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "PHP 100",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CollectionMethod(){ //Online
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Collection Method",
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = White2)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Product Name and Price
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pick Up",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethod() { //Online
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = White2)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Product Name and Price
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cash",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable // Online
fun OrderStatus(status: String, statusDescription: String, dateTime: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Circle indicator only
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color.Black, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Action details
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun UpdateStatusCard(status: String, statusDescription: String, dateTime: String) {
    var statusValue by remember { mutableStateOf(status) }
    var statusDescriptionValue by remember { mutableStateOf(statusDescription) }
    var dateTimeValue by remember { mutableStateOf(dateTime) }
    var expanded by remember { mutableStateOf(false) }

    // Sample options with descriptions
    //TODO: add to db with desc
    val statusOptions = mapOf(
        "Pending" to "Your order is pending confirmation.",
        "Confirmed" to "Your order has been confirmed.",
        "Shipped" to "Your order is on the way.",
        "Delivered" to "Your order has been delivered.",
        "Cancelled" to "Your order has been cancelled."
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                OutlinedTextField(
                    value = statusValue,
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    statusOptions.forEach { (option, description) ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                statusValue = option // Update selected value
                                statusDescriptionValue = description // Update description
                                expanded = false // Close dropdown
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = statusDescriptionValue,
                onValueChange = {},
                label = { Text("Description") },
                readOnly = false,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dateTimeValue,
                onValueChange = { dateTimeValue = it },
                readOnly = true,
                label = { Text("Date and Time") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}





//INSTORE
@Composable
fun InStoreSalesDetails(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Name", //sale.productName
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Green1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SO-YYYYDDMM-Count",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Timestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InStorePriceInfoColumn(
                    title = "Selling Price",
                    price = 1.00,
                    weightUnit = "weight unit"
                )
                InStorePriceInfoColumn(
                    title = "Purchase Cost",
                    price = 1.00,
                    weightUnit = "weight unit"
                )
                Card(
                    modifier = Modifier.size(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Green4)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Add\nImage", textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Details Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White1)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        InStoreDetailsCard()
        NotesCard()
    }
}

@Composable
private fun InStoreDetailsCard(){
    // Items Section
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Sale",
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                    Text(
                        text = "Date and time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = Green4, thickness = 1.dp)

                // Item Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "QTY x Price",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "1 x PHP1.00",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "PHP1.00",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InStorePriceInfoColumn(
    title: String,
    price: Double,
    weightUnit: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "PHP ",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "per ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesCard(){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Divider(color = Green4, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Display notes here",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}