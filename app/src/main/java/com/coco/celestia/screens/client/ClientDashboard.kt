package com.coco.celestia.screens.client

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.NotificationService
import com.coco.celestia.ui.theme.BABText
import com.coco.celestia.ui.theme.BAButton
import com.coco.celestia.ui.theme.CLGText
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.ContainerLO
import com.coco.celestia.ui.theme.CDText
import com.coco.celestia.ui.theme.CDarkOrange
import com.coco.celestia.ui.theme.LGContainer
import com.coco.celestia.ui.theme.SoftCOrange
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val featuredProducts by productViewModel.featuredProducts.observeAsState(emptyList())
    var notifications = remember { mutableListOf<Notification>() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        NotificationService.pushNotifications(
            uid = uid,
            onComplete = {
                notifications.clear()
                notifications.addAll(it)
            },
            onError = {

            }
        )
        Log.d("Notifs", notifications.toString())
        orderViewModel.fetchAllOrders("", "Client")
        Log.d("OrderData", "Observed orders: ${orderData.size}")
        delay(1000)
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchFeaturedProducts()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
            .semantics { testTag = "android:id/ClientDashboardScreen" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 0.dp)
                .semantics { testTag = "android:id/ClientDashboardColumn" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 27.dp,
                        bottom = 8.dp,
                        start = 25.dp,
                        end = 25.dp
                    )
                    .semantics { testTag = "android:id/DashboardHeaderRow" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                userData.let { user ->
                    Text(
                        text = "Welcome, ${user.firstname} ${user.lastname}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CDText,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/WelcomeText" }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (notifications.isNotEmpty()) {
                                    showDialog = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No new notifications",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LGContainer)
                                    .padding(17.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_icon),
                                    contentDescription = "Notification Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))


        }
    }
    if (showDialog) {
        ClientNotification(notifications = notifications, onDismiss = { showDialog = false })
    }
}

