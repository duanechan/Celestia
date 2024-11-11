package com.coco.celestia.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.viewmodel.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientNotification(notifications: List<Notification>, onDismiss: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Notifications") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .semantics { testTag = "android:id/NotificationList" }
            ) {
                items(notifications.sortedByDescending { notification ->
                    dateFormat.parse(notification.timestamp) ?: Date(0)
                }) { notification ->
                    NotificationItem(notification)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier.semantics { testTag = "android:id/DismissButton" }
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun NotificationItem(notification: Notification) {
    val statusColor = when (notification.status.lowercase()) {
        "rejected" -> Color.Red
        "pending" -> LightOrange
        "preparing" -> Color.Blue
        "delivering" -> Color.Black
        "completed" -> Color.Green
        "incomplete" -> Color.Cyan
        "received" -> Color.Yellow
        "cancelled" -> Color.Magenta
        else -> Color.Gray
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .semantics { testTag = "android:id/NotificationItem_${notification.timestamp}" }
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(24.dp)
                .semantics { testTag = "android:id/NotificationIcon_${notification.status}" }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.semantics { testTag = "android:id/NotificationContent" }
        ) {
            Text(
                text = notification.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.semantics { testTag = "android:id/NotificationTimestamp" }
            )
            Text(
                text = notification.message,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { testTag = "android:id/NotificationMessage" }
            )
        }
    }
}
