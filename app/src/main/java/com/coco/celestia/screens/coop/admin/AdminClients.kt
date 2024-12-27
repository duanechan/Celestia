package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData

@Composable
fun AdminClients(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var currentView by remember { mutableStateOf("All Clients") }

    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val clients = usersData.filter { it.role.contains("Client", ignoreCase = true) }

    LaunchedEffect(userState) {
        userViewModel.fetchUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No new registered clients",
                        color = Color.Gray,
                        fontFamily = mintsansFontFamily
                    )
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
                        ClientItem(client)
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
fun ClientItem(client: UserData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = White1
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Add Client",
                modifier = Modifier.size(50.dp),
                tint = Green1
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${client.firstname} ${client.lastname}",
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { /* to add later */ }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Details"
                )
            }
        }
    }
}