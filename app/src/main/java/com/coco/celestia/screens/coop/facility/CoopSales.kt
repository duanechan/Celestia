package com.coco.celestia.screens.coop.facility

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class SaleItem(
    val id: String,
    val item: String,
    val amount: Double,
    val date: String
)

@Composable
fun CoopSales(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val sampleSales = remember {
        listOf(
            SaleItem("Sale #001", "Rice", 50.0, "2024-01-01"),
            SaleItem("Sale #002", "Corn", 30.0, "2024-01-01"),
            SaleItem("Sale #003", "Fertilizer", 100.0, "2024-01-02")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales Records",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = { /* TODO: Add new sale */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Sale")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sampleSales) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sale.id, style = MaterialTheme.typography.titleSmall)
                            Text(sale.item, style = MaterialTheme.typography.bodyMedium)
                            Text(sale.date, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "₱${sale.amount}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}