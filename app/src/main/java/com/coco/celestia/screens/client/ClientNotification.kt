package com.coco.celestia.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import com.coco.celestia.R
import com.coco.celestia.viewmodel.TransactionViewModel

@Composable
fun ClientNotification(notifications: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(notifications) { notification ->
            val status = extractStatus(notification)
//            val iconResId = getIconResourceId(status)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
//                    Icon(
////                        painter = painterResource(id = iconResId),
//                        contentDescription = status,
//                        tint = Color(0xFF5A7F54),
//                        modifier = Modifier.size(50.dp)
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

fun extractStatus(notification: String): String {
    return when {
        "pending" in notification.lowercase() -> "Pending"
        "prepared" in notification.lowercase() -> "Preparing"
        "rejected" in notification.lowercase() -> "Rejected"
        "on the way" in notification.lowercase() -> "Delivering"
        "received" in notification.lowercase() -> "Completed"
        else -> "Unknown"
    }
}

//fun getIconResourceId(status: String): Int {
//    return when (status) {
//        "Pending" -> R.drawable.pending_icon
//        "Preparing" -> R.drawable.preparing
//        "Rejected" -> R.drawable.rejected_icon
//        "Delivering" -> R.drawable.deliveryicon
//        "Completed" -> R.drawable.completed_icon
//        else -> R.drawable.default_icon
//    }
//}

@Composable
fun NotificationScreen(viewModel: TransactionViewModel) {
    val notifications: List<String> = viewModel.notifications.observeAsState(initial = emptyList()).value
    ClientNotification(notifications = notifications)
}
