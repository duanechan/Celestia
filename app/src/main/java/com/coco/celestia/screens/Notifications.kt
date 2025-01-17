package com.coco.celestia.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.service.NotificationService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 3.dp)
    ) {
        if (notifications.isNotEmpty()) {
            items(sortNotificationsByTimestamp(notifications)) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, start = 10.dp, end = 10.dp)
                        .clickable {
                            // Create an action when a notification card is clicked
                        },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Notification Icon
                        Icon(
                            painter = painterResource(R.drawable.notificon),
                            contentDescription = "Notification Icon",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterVertically),
                            tint = Green1
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = notification.message,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = (notification.details as OrderData).orderData[0].name,
                                fontSize = 14.sp,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = notification.timestamp,
                                fontSize = 14.sp,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            )
                        }

                        if (!notification.hasRead) {
                            Text(
                                text = "⬤",
                                fontSize = 20.sp,
                                color = Cinnabar,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerNotification() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val notifications = remember { mutableStateListOf<Notification>() }

    if (notifications.isEmpty()) {
        notifications.add(
            Notification(
                timestamp = "January 17, 2025 10:00AM",
                sender = "System",
                message = "New Order Request by Diwata Pares!",
                details = OrderData(
                    orderId = "12345",
                    orderDate = "January 19, 2023",
                    targetDate = "January 24, 2023",
                    status = "Pending",
                    orderData = listOf(ProductData(name = "Beef", quantity = 1)),
                    client = "Jane Doe",
                    barangay = "Sample Barangay",
                    street = "Sample Street"
                ),
                type = NotificationType.Notice,
                hasRead = false
            )
        )
    }

    LaunchedEffect(Unit) {
        NotificationService.observeUserNotifications(
            uid = uid,
            onNotificationsChanged = {
                notifications.clear()
                notifications.addAll(it)
            },
            onError = { error ->
                Log.e("FarmerNotification", "Error fetching notifications", error.toException())
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 3.dp)
    ) {
        if (notifications.isNotEmpty()) {
            items(notifications) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, start = 10.dp, end = 10.dp)
                        .clickable {
                        },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Notification Icon
                        Icon(
                            painter = painterResource(R.drawable.notificon),
                            contentDescription = "Notification Icon",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterVertically),
                            tint = Green1
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = notification.message,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = (notification.details as? OrderData)?.orderData?.get(0)?.name ?: "No details",
                                fontSize = 14.sp,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = notification.timestamp,
                                fontSize = 14.sp,
                                fontFamily = mintsansFontFamily,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            )
                        }

                        if (!notification.hasRead) {
                            Text(
                                text = "⬤",
                                fontSize = 20.sp,
                                color = Cinnabar,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
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