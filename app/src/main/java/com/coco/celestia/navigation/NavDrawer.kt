package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.Screen
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.util.routeHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(role: String, firstName: String, lastName: String) {
    val fullName = "$firstName $lastName"
    when (role) {
        "Client" -> {
            TopAppBar(
                title = { Text(text = fullName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
        "Farmer" -> {
            GradientTopBar(fullName = fullName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(fullName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF41644A),  // First color (#41644A)
                        Color(0xFF83CA95)   // Second color (#83CA95)
                    )
                )
            )
    ) {
        TopAppBar(
            title = { Text(text = fullName) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NavDrawerBottomBar(
    role: String,
    onAddProduct: () -> Unit,
    onSaveProduct: () -> Unit,
    navController: NavController
) {
    var showOrderOptions by remember { mutableStateOf(false) }
    val routes = routeHandler(role)
    val bottomBarColors: Pair<Color, Color> = bottomColorConfig(role)
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = bottomBarColors.first,
            contentColor = bottomBarColors.second
        ) {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                selected = currentDestination == routes.dashboard,
                onClick = {
                    navController.navigate(routes.dashboard) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            )

            if (role == "Client" || role == "Coop" || role == "Farmer") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Orders"
                        )
                    },
                    label = { Text("Orders") },
                    selected = currentDestination == routes.orders,
                    onClick = { showOrderOptions = !showOrderOptions }
                )
            }

            if (role == "Admin" || role == "Coop" || role == "Farmer") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Items") },
                    label = { Text("Items") },
                    selected = currentDestination == routes.inventory,
                    onClick = {
                        navController.navigate(routes.inventory) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    }
                )
            }

            if (role == "Admin") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Management"
                        )
                    },
                    label = { Text("User Management", fontSize = 10.sp) },
                    selected = currentDestination == Screen.AdminUserManagement.route,
                    onClick = {
                        navController.navigate(Screen.AdminUserManagement.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    }
                )
            }

            if (role == "Client") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Contact Inquiry"
                        )
                    },
                    label = { Text("Contact") },
                    selected = currentDestination == Screen.ClientContact.route,
                    onClick = {
                        navController.navigate(Screen.ClientContact.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    }
                )
            }

            //Drop down menu for farmer side
            DropdownMenu(
                expanded = showOrderOptions,
                onDismissRequest = { showOrderOptions = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5A8F5C))
            ) {
                DropdownMenuItem(
                    onClick = {
                        showOrderOptions = false
                        navController.navigate(Screen.FarmerManageOrder.route)
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(), // Make the Box fill the width of the item
                            contentAlignment = Alignment.Center // Center align the text
                        ) {
                            Text("Order Status", color = Color.White)
                        }
                    }
                )

                Divider(color = Color.White, thickness = 1.dp)

                DropdownMenuItem(
                    onClick = {
                        showOrderOptions = false
                        navController.navigate(Screen.FarmerManageOrderRequest.route)
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(), // Make the Box fill the width of the item
                            contentAlignment = Alignment.Center // Center align the text
                        ) {
                            Text("Order Request", color = Color.White)
                        }
                    }
                )
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("Profile") },
                selected = currentDestination == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            )
        }

        if (role == "Coop" && currentDestination == Screen.CoopProductInventory.route) {
            FloatingActionButton(
                onClick = onAddProduct,
                shape = CircleShape,
                containerColor = DarkGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (role == "Coop" && currentDestination == Screen.CoopAddProductInventory.route) {
            FloatingActionButton(
                onClick = onSaveProduct,
                shape = CircleShape,
                containerColor = DarkGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

fun bottomColorConfig(role: String): Pair<Color, Color> {
    return when (role) {
        "Admin" -> Pair(Color.White, DarkBlue)
        "Client" -> Pair(Orange, DarkGreen)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color.White, Color.Green)
        else -> Pair(Color.White, Color.Black) // Default
    }
}

