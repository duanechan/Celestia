package com.coco.celestia.screens.coop.facility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.viewmodel.VendorState
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.ui.theme.*

@Composable
fun Vendors(
    navController: NavController,
    onAddVendor: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VendorViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Inactive")

    var searchQuery by remember { mutableStateOf("") }

    val vendors by viewModel.vendorData.observeAsState(emptyList())
    val vendorState by viewModel.vendorState.observeAsState(VendorState.LOADING)

    LaunchedEffect(selectedTabIndex) {
        val filter = when (selectedTabIndex) {
            0 -> "all"
            1 -> "active"
            2 -> "inactive"
            else -> "all"
        }
        viewModel.fetchVendors(filter)
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
                        Text("No vendors found")
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
                                    onClick = { }
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