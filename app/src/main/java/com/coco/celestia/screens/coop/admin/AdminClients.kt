package com.coco.celestia.screens.coop.admin

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AdminClients(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var currentView by remember { mutableStateOf("All Clients") }

    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    val clients = usersData.filter { it.role.contains("Client", ignoreCase = true) }

    val today = remember { Calendar.getInstance() }
    val currentYear = today.get(Calendar.YEAR)
    val currentMonth = today.get(Calendar.MONTH)

    val newClients = clients.filter { client ->
        if (client.registrationDate.isBlank()) {
            false
        } else {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val registrationDate = dateFormat.parse(client.registrationDate)
                val calendar = Calendar.getInstance().apply {
                    time = registrationDate
                }

                calendar.get(Calendar.YEAR) == currentYear &&
                        calendar.get(Calendar.MONTH) == currentMonth
            } catch (e: Exception) {
                false
            }
        }
    }

    LaunchedEffect(userState) {
        userViewModel.fetchUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationTabs(
                title = "New Registered",
                iconRes = R.drawable.new_register,
                isActive = currentView == "New Registered",
                onClick = { currentView = "New Registered" }
            )
            NavigationTabs(
                title = "All Clients",
                iconRes = R.drawable.members,
                isActive = currentView == "All Clients",
                onClick = { currentView = "All Clients" }
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(if (currentView == "New Registered") Alignment.Start else Alignment.End),
            color = Green1,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (currentView) {
            "New Registered" -> {
                if (newClients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No new registered clients this month",
                            color = Color.Gray,
                            fontFamily = mintsansFontFamily
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(newClients) { client ->
                            ClientItem(client, navController)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            "All Clients" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(clients) { client ->
                        ClientItem(client, navController)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (clients.isEmpty()) {
                        item {
                            when (userState) {
                                UserState.LOADING -> Text(text = "Loading clients...", color = Color.Gray)
                                UserState.SUCCESS -> Text(text = "No clients found", color = Color.Gray)
                                else -> Text(text = "Failed to load clients", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationTabs(
    title: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                colorFilter = if (isActive) ColorFilter.tint(Green1) else null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isActive) Green1 else Color.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ClientItem(client: UserData, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate(Screen.AdminClientDetails.createRoute(client.email))
            },
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Client Profile",
                modifier = Modifier.size(50.dp),
                tint = Green1
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${client.firstname} ${client.lastname}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = client.email,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (client.registrationDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Registered: ${client.registrationDate}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            IconButton(onClick = {
                navController.navigate(Screen.AdminClientDetails.createRoute(client.email))
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Details"
                )
            }
        }
    }
}

@Composable
fun ClientDetails(
    email: String,
    userViewModel: UserViewModel
) {
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val client = usersData.find { it.email == email }

    if (client != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClientInfoCard(label = "Name", value = "${client.firstname} ${client.lastname}", icon = Icons.Default.Person)
            ClientInfoCard(label = "Email", value = client.email, icon = Icons.Default.Email)
            ClientInfoCard(label = "Phone Number", value = client.phoneNumber, icon = Icons.Default.Phone)
            ClientInfoCard(label = "Address", value = "${client.streetNumber}, ${client.barangay}", icon = Icons.Default.Home)
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Client not found",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ClientInfoCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(36.dp),
                    tint = Green1
                )
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                color = Green1
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}