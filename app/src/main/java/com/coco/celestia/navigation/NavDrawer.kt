package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import com.coco.celestia.screens.Screen
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.VeryDarkPurple
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
                    containerColor = LightOrange,
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
                        Color(0xFFE0A83B),  // First color (#41644A)0xFFA36361
                        Color(0xFF7A5C20)   // Second color (#83CA95)0xFFC3B0AD
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
    navController: NavController
) {
    val routes = routeHandler(role)
    val bottomBarColors: Pair<Color, Color> = bottomColorConfig(role)
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = bottomBarColors.first,
            contentColor = bottomBarColors.second
        ) {
            // Dashboard - Default to all roles except Client
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard", tint = if (role == "Client") VeryDarkPurple else bottomBarColors.second) },
                label = { Text("Dashboard", color = if (role == "Client") Color.White else bottomBarColors.second) },
                selected = currentDestination == routes.dashboard,
                onClick = {
                    navController.navigate(routes.dashboard) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            )

            if(role == "Coop" || role == "Client" || role == "Farmer") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders", tint = if (role == "Client") VeryDarkPurple else bottomBarColors.second) },
                    label = { Text("Orders", color = if (role == "Client") Color.White else bottomBarColors.second) },
                    selected = currentDestination == routes.orders,
                    onClick = {
                        if (role == "Farmer") {
                            // Directly navigate to Order Status
                            navController.navigate(Screen.FarmerManageOrder.route) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        } else {
                            navController.navigate(routes.orders) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        }
                    }
                )
            }

            if (role == "Admin" || role == "Coop" || role == "Farmer") {
                val (backgroundColor, contentColor) = bottomColorConfig(role)

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Items",
                            tint = contentColor
                        )
                    },
                    label = { Text("Items", color = contentColor) },
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
                    icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Contact", tint = VeryDarkPurple) },
                    label = { Text("Contact", color = Color.White) },
                    selected = currentDestination == Screen.ClientContact.route,
                    onClick = {
                        navController.navigate(Screen.ClientContact.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    }
                )
            }

            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = if (role == "Client") VeryDarkPurple else bottomBarColors.second) },
                label = { Text("Profile", color = if (role == "Client") Color.White else bottomBarColors.second) },
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
                onClick = { navController.navigate(Screen.CoopAddProductInventory.route) },
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
                onClick = { navController.navigate(Screen.CoopAddProductInventoryDB.route) },
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
        "Client" -> Pair(LightOrange, Color.White)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color(0xFFE0A83B), Color(0xFF693F27))
        else -> Pair(Color.White, Color.Black) // Default
    }
}

