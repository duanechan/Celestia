package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

//TODO: need backend to connect order requests notifications etc.,
//boolean = true will show placeholder notification content
//boolean = false will show placeholder no notification content
@Composable
fun FarmerNotification(hasNotifications: Boolean = true, onRefresh: () -> Unit = {}) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(4.dp),
        ) {
            // Screen Title
//            Text(
//                text = "Notifications",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))

            // Conditional UI for Notifications
            if (hasNotifications) {
                // Display notifications if they exist
                NotificationItem(
                    title = "Pending Order",
                    message = "There is a pending request for an order from the Cooperative",
                    timestamp = "2 hours ago"
                )

                Spacer(modifier = Modifier.height(8.dp))

                NotificationItem(
                    title = "Order Cancelled",
                    message = "The order of 50kg of potatoes has been cancelled.",
                    timestamp = "1 day ago"
                )
            } else {
                // Display placeholder UI if there are no notifications
                NoNotificationsPlaceholder(onRefresh = onRefresh)
            }
        }
    }
}

@Composable
fun NotificationItem(title: String, message: String, timestamp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Notification Icon
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Notification Timestamp
        Text(
            text = timestamp,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun NoNotificationsPlaceholder(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder Content
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Notifications Yet",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Refresh Button
        Button(
            onClick = { onRefresh() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Refresh",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}