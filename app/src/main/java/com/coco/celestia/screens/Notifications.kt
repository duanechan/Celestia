package com.coco.celestia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.coco.celestia.screens.client.ClientNotification
import com.coco.celestia.screens.farmer.FarmerNotification
import com.coco.celestia.viewmodel.model.Notification

// TODO: To add backend for notifications

@Composable
fun Notifications(
    role: String,
    notifications: List<Notification> = emptyList(),
    onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }

    when {
        role == "Admin" -> {
            AdminNotification()
        }
        role.startsWith("Coop") -> {
            FacilityNotification()
        }
        role == "Farmer" -> {
            FarmerNotification()
        }
        role == "Client" && showDialog -> {
            ClientNotification(
                notifications = notifications,
                onDismiss = {
                    showDialog = false
                    onDismiss()
                }
            )
        }
        else -> {
            DefaultNotification()
        }
    }
}

@Composable
fun AdminNotification() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No notifications",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun FacilityNotification() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No notifications",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun DefaultNotification() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No notifications",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}