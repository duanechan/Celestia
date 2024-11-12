package com.coco.celestia.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.util.calculateMonthlyInventory
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.MonthlyInventory
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboard(
    userData: UserData?,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    navController: NavController
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    var monthlyInventory by remember { mutableStateOf<List<MonthlyInventory>>(emptyList()) }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(filter = "", role = "Admin")
        orderViewModel.fetchAllOrders("", "Admin")
    }

    LaunchedEffect(orderData, productData) {
        monthlyInventory = calculateMonthlyInventory(orderData, productData)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            val dateFormat = SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())
            userData?.let { user ->
                Text(
                    text = today,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    fontFamily = mintsansFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/currentDate" },
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Welcome, ${user.firstname} ${user.lastname}!",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontFamily = mintsansFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/welcomeMessage" },
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            SummaryDashboard(navController, monthlyInventory)
        }
    }
}

@Composable
fun SummaryDashboard(navController: NavController, monthlyInventory: List<MonthlyInventory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp)
            .semantics { testTag = "android:id/summaryDashboard" }
    ) {
        item{
        Box(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
                .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
                .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        ) {
            Text(
                text = "Inventory Overview",
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier
                    .padding(start = 20.dp, top =15.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.padding(5.dp)) {
                Column {
                    InventoryOverview(monthlyInventory)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth()
                    .border(BorderStroke(3.dp, Color.White), shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .semantics { testTag = "android:id/userManagementOverviewBox" }
            ) {
                Text(
                    text = "User Management Overview",
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    color = DarkBlue,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 15.dp)
                )
                Row(modifier = Modifier.padding(5.dp)) {
                    Column {
                        UserManagementDashboard(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryOverview(monthlyInventory: List<MonthlyInventory>) {
    var totalMonthly by remember { mutableStateOf(false) }

    monthlyInventory.forEach { monthly ->
        if (monthly.remainingQuantity < 0) {
            totalMonthly = true
            return@forEach
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { testTag = "android:id/inventoryPieChartContainer" }
    ) {
        // Alerts Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/alertsCard" }
        ) {
            // Alert
            Column(Modifier
                .background(PaleBlue)
                .padding(20.dp)
                .fillMaxWidth()
            ) {
                Text("Alerts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkBlue)
                Spacer(modifier = Modifier.height(4.dp))

                if (totalMonthly) {
                    Text("• Not Enough Supply this Month", fontSize = 14.sp, color = DuskyBlue)
                    monthlyInventory.forEach { monthly ->
                        if (monthly.remainingQuantity < 0) {
                            Text(
                                "• ${monthly.productName}: ${monthly.remainingQuantity}kg",
                                fontSize = 14.sp,
                                color = DuskyBlue,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                } else {
                    Text("• Supply is Enough this Month", fontSize = 14.sp, color = DuskyBlue)
                }
            }
            //
        }
        //Inventory
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(Modifier
                .background(PaleBlue)
                .padding(20.dp)
                .fillMaxWidth()
            ) {
                Text("Inventory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkBlue)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaleBlue),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                monthlyInventory.chunked(2).forEach  { chunk ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)

                    ) {
                        chunk.forEach { monthly ->
                            Card (
                                modifier = Modifier
                                    .width(135.dp)
                                    .height(140.dp)
                                    .padding(bottom = 10.dp)
                            ){
                                InventorySummary(monthly.productName, monthly.currentInv.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun InventorySummary(label: String, value: String) {
    Box {
        Column (
            modifier = Modifier
                .background(DuskyBlue)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberImagePainter(R.drawable.box),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Product Image",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .size(50.dp)
                )
                Text(
                    "${value}kg",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Text(
                label,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontFamily = mintsansFontFamily,
                modifier = Modifier
                    .padding(top = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UserManagementDashboard(navController: NavController) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val transactionViewModel: TransactionViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    var activeUsers by remember { mutableIntStateOf(0) }
    var filteredTransaction by remember { mutableStateOf<Map<String, List<TransactionData>>>(emptyMap()) }
    var userData by remember { mutableStateOf<UserData?>(null) }

    LaunchedEffect(transactionData) {
        transactionViewModel.fetchAllTransactions()
        userViewModel.fetchUsers()
        userViewModel.fetchActiveUsers(
            onResult = { activeUsers = it.size },
            onError = { activeUsers = 0 }
        )
        val transactions = mutableMapOf<String, List<TransactionData>>()

        transactionData.forEach { (userId, transaction) ->
            UserIdentifier.getUserData(userId) {
                userData = it
            }

            if (userData?.role?.contains("Coop") == true) {
                transactions[userId] = transaction
            }
        }

        filteredTransaction = transactions
    }

    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .semantics { testTag = "android:id/userManagementDashboard" }
    ) {
        // Total Users Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/totalUsersCard" }
        ) {
            Column(Modifier
                .background(PaleBlue)
                .fillMaxWidth()
                .padding(20.dp)
            ) {
                Text(
                    "Total Users",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = mintsansFontFamily,
                    color = DarkBlue,
                    modifier = Modifier.semantics { testTag = "android:id/totalUsersLabel" }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    usersData.size.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    color = DarkBlue,
                    modifier = Modifier.semantics { testTag = "android:id/totalUsersCount" }
                )
            }
        }

        // Active Users Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { testTag = "android:id/activeUsersCard" }
        ) {
            Column(Modifier
                .background(PaleBlue)
                .fillMaxWidth()
                .padding(16.dp)
            ) {
                Text(
                    "Active Users",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = mintsansFontFamily,
                    color = DarkBlue,
                    modifier = Modifier.semantics { testTag = "android:id/activeUsersLabel" }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    activeUsers.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    color = DuskyBlue,
                    modifier = Modifier.semantics { testTag = "android:id/activeUsersCount" }
                )
            }
        }

        // Recent Activity Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DarkBlue,
                fontFamily = mintsansFontFamily,
                modifier = Modifier.semantics { testTag = "android:id/recentActivityLabel" }
            )
            TextButton(onClick = { navController.navigate("admin_add_user_management_logs") }) {
                Text(
                    text = "See All",
                    color = DarkBlue,
                    fontFamily = mintsansFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { testTag = "android:id/recentActivitySeeAllButton" }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .semantics { testTag = "android:id/recentActivityList" }
        ) {
            filteredTransaction.entries
                .flatMap { entry ->
                    entry.value.map { transaction ->
                        Pair(entry.key, transaction)
                    }
                }
                .sortedByDescending { (_, transaction) ->
                    dateFormat.parse(transaction.date) ?: Date(0)
                }
                .take(3)
                .forEach { (userId, transaction) ->
                    var user by remember { mutableStateOf(UserData()) }
                    LaunchedEffect(userId) {
                        UserIdentifier.getUserData(userId) { result ->
                            user = result
                        }
                    }
                    Text(
                        "• ${transaction.date} - ${user.firstname} ${user.lastname} - ${transaction.description}",
                        fontSize = 14.sp,
                        fontFamily = mintsansFontFamily,
                        color = DarkBlue,
                        modifier = Modifier.semantics { testTag = "android:id/recentActivityTransaction_${transaction.date}" }
                    )
                }
        }
    }
}