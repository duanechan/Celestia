package com.coco.celestia.screens.client

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    var notifications = remember { mutableListOf<Notification>() }
    var showDialog by remember { mutableStateOf(false) }
    var showRequestPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", "Client")
        productViewModel.fetchFeaturedProducts()
        delay(1000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 0.dp)
        ) {
            // Welcome Text and Notification Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                userData.let { user ->
                    Text(
                        text = "Welcome, ${user.firstname} ${user.lastname}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CDText,
                        modifier = Modifier.weight(1f)
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
                                    .background(Color.LightGray)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BoxButton(text = "Request an Order", onClick = {navController.navigate(Screen.ClientSpecialReq.route)})
                BoxButton(text = "View Order History", onClick = {})
                BoxButton(text = "View Contacts", onClick = {})
                BoxButton(text = "To Receive", onClick = {})
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun BoxButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Green2, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

