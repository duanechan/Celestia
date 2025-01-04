package com.coco.celestia.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coco.celestia.screens.farmer.FarmerNotification

@Composable
fun Notifications(role: String) {
    when (role) {
        "Farmer" -> {
            FarmerNotification()
        }
        else -> {
            // Placeholder or other role-specific UI for Notifications
            DefaultNotification()
        }
    }
}

@Composable
fun DefaultNotification() {
    // Placeholder UI for other roles
    Text("Default Notification UI")
}