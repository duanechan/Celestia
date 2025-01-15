package com.coco.celestia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.farmer.FarmerNotification
import com.coco.celestia.service.NotificationService
import com.coco.celestia.ui.theme.Cinnabar
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Notifications(
    role: String,
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
//            ClientNotification(
//                notifications = notifications,
//                onDismiss = {
//                    showDialog = false
//                    onDismiss()
//                }
//            )
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
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var notifications = remember { mutableStateListOf<Notification>() }

    LaunchedEffect(Unit) {
        NotificationService.observeUserNotifications(
            uid = uid,
            onNotificationsChanged = {
                notifications.clear()
                notifications.addAll(it)
            },
            onError = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (notifications.isNotEmpty()) {
            for (notification in sortNotificationsByTimestamp(notifications)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = notification.timestamp,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (!notification.hasRead) {
                                    Text(
                                        text = "⬤",
                                        fontSize = 15.sp,
                                        color = Cinnabar
                                    )
                                }

                            }
                            Text(
                                text = notification.message,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = (notification.details as OrderData).orderData[0].name,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No notifications",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

fun sortNotificationsByTimestamp(notifications: List<Notification>): List<Notification> {
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")

    return notifications.sortedByDescending { notification ->
        LocalDateTime.parse(notification.timestamp, formatter)
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