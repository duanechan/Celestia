package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.ui.theme.*

@Composable
fun AdminHome(
    navController: NavController
) {
    var currentView by remember { mutableStateOf("Dashboard") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        // Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationTab(
                title = "Dashboard",
                iconRes = R.drawable.dashboard,
                isActive = currentView == "Dashboard",
                onClick = {
                    currentView = "Dashboard"
                }
            )
            NavigationTab(
                title = "Add Facility",
                iconRes = R.drawable.facility,
                isActive = currentView == "Add Facility",
                onClick = {
                    currentView = "Add Facility"
                }
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(if (currentView == "Dashboard") Alignment.Start else Alignment.End),
            color = Green1,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (currentView) {
            "Dashboard" -> {
                Text(
                    text = "My Facilities",
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = "No facilities yet.",
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Special Requests",
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SpecialRequestCard(title = "Pending", count = "0", onClick = { })
                    SpecialRequestCard(title = "In Progress", count = "0", onClick = { })
                    SpecialRequestCard(title = "Cancelled", count = "0", onClick = { })
                    SpecialRequestCard(title = "Turned Down", count = "0", onClick = { })
                }
            }
            "Add Facility" -> {
                AddFacilityForm()
            }
        }
    }
}

@Composable
fun NavigationTab(
    title: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFacilityForm() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Green4),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.facility),
                        contentDescription = "Facility Image",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Facility Name",
                        color = Green1,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                var facilityFocused by remember { mutableStateOf(false) }
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = {
                        Text(
                            text = "Enter name of Facility",
                            fontStyle = FontStyle.Italic,
                            color = if (facilityFocused) Color.Transparent else Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .onFocusChanged { focusState ->
                            facilityFocused = focusState.isFocused
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = White1,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Accessible to:",
                    color = Green1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                var accessibleFocused by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = {
                            Text(
                                text = "Enter email address",
                                fontStyle = FontStyle.Italic,
                                color = if (accessibleFocused) Color.Transparent else Color.Gray
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .onFocusChanged { focusState ->
                                accessibleFocused = focusState.isFocused
                            },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = White1,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { /* Add Action */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Green1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Add Action */ },
            colors = ButtonDefaults.buttonColors(containerColor = Green1),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "ADD", color = Color.White)
        }
    }
}

@Composable
fun SpecialRequestCard(title: String, count: String, onClick: () -> Unit) {
    val imageRes = when (title) {
        "Pending" -> R.drawable.preparing
        "In Progress" -> R.drawable.progress
        "Cancelled" -> R.drawable.cancelled
        "Turned Down" -> R.drawable.cancel
        else -> R.drawable.warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Green4),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "$title Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Green1)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Green1,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = count,
                color = Green1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}