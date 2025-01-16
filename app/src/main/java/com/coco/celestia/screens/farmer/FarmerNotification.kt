//package com.coco.celestia.screens.farmer
//
//import android.util.Log
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.coco.celestia.R
//import com.coco.celestia.service.NotificationService
//import com.coco.celestia.ui.theme.Cinnabar
//import com.coco.celestia.ui.theme.Green1
//import com.coco.celestia.ui.theme.mintsansFontFamily
//import com.coco.celestia.viewmodel.model.Notification
//import com.coco.celestia.viewmodel.model.OrderData
//import com.google.firebase.auth.FirebaseAuth
//
//@Composable
//fun FarmerNotification() {
//    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
//    val notifications = remember { mutableStateListOf<Notification>() }
//
//    LaunchedEffect(Unit) {
//        NotificationService.observeUserNotifications(
//            uid = uid,
//            onNotificationsChanged = {
//                notifications.clear()
//                notifications.addAll(it)
//            },
//            onError = { error ->
//                Log.e("FarmerNotification", "Error fetching notifications", error.toException())
//            }
//        )
//    }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(top = 3.dp)
//    ) {
//        if (notifications.isNotEmpty()) {
//            items(notifications) { notification ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 7.dp, start = 10.dp, end = 10.dp)
//                        .clickable {
//                        },
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(12.dp)
//                    ) {
//                        // Notification Icon
//                        Icon(
//                            painter = painterResource(R.drawable.notifcon),
//                            contentDescription = "Notification Icon",
//                            modifier = Modifier
//                                .size(40.dp)
//                                .align(Alignment.CenterVertically),
//                            tint = Green1
//                        )
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Column(
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Text(
//                                text = notification.message,
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                fontFamily = mintsansFontFamily,
//                                color = MaterialTheme.colorScheme.onBackground
//                            )
//
//                            Text(
//                                text = (notification.details as? OrderData)?.orderData?.get(0)?.name ?: "No details",
//                                fontSize = 14.sp,
//                                fontFamily = mintsansFontFamily,
//                                color = MaterialTheme.colorScheme.onBackground,
//                                modifier = Modifier.padding(top = 4.dp)
//                            )
//
//                            Text(
//                                text = notification.timestamp,
//                                fontSize = 14.sp,
//                                fontFamily = mintsansFontFamily,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                                modifier = Modifier
//                                    .align(Alignment.End)
//                                    .padding(top = 8.dp)
//                            )
//                        }
//
//                        if (!notification.hasRead) {
//                            Text(
//                                text = "⬤",
//                                fontSize = 20.sp,
//                                color = Cinnabar,
//                                modifier = Modifier.align(Alignment.CenterVertically)
//                            )
//                        }
//                    }
//                }
//            }
//        } else {
//            item {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "No notifications",
//                        fontSize = 16.sp,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                }
//            }
//        }
//    }
//}