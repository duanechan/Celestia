package com.coco.celestia.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.Screen
import com.coco.celestia.dialogs.ExitDialog
import com.coco.celestia.dialogs.LogoutDialog
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.util.routeHandler
import com.coco.celestia.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(role: String, firstName: String, lastName: String) {
    val fullName = "$firstName $lastName"
    when (role) {
        "Client" ->
            TopAppBar(
                title = { Text(text = fullName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
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
                        popUpTo(0)
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
                    onClick = {
                        navController.navigate(routes.orders) {
                            popUpTo(0)
                        }
                    }
                )
            }

            if (role == "Admin" || role == "Coop" || role == "Farmer") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Items") },
                    label = { Text("Items") },
                    selected = currentDestination == routes.inventory,
                    onClick = {
                        navController.navigate(routes.inventory) {
                            popUpTo(0)
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
                            popUpTo(0)
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
                            popUpTo(0)
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
                        popUpTo(0)
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
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp))
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
