package com.coco.celestia.components

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.AdminInventory
import com.coco.celestia.AdminUserManagement
import com.coco.celestia.ClientContact
import com.coco.celestia.ClientDashboard
import com.coco.celestia.ClientOrder
import com.coco.celestia.CoopDashboard
import com.coco.celestia.CoopInventory
import com.coco.celestia.CoopOrder
//import com.coco.celestia.FarmerAddProductScreen
import com.coco.celestia.FarmerDashboard
import com.coco.celestia.FarmerInventoryScreen
import com.coco.celestia.FarmerManageOrder
import com.coco.celestia.Routes
import com.coco.celestia.Screen
import com.coco.celestia.dialogs.ExitDialog
import com.coco.celestia.screens.Profile
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.util.routeHandler
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavDrawer(
    role: String,
    navController: NavController,
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    userViewModel: UserViewModel
) {
    val navHostController = rememberNavController()
    var exitDialog by remember { mutableStateOf(false) }
    val routes = routeHandler(role)

    BackHandler {
        exitDialog = true
    }

    if (exitDialog) {
        ExitDialog(
            onDismiss = { exitDialog = false },
            onExit = { (navController.context as Activity).finish() }
        )
    }

    Scaffold(
        topBar = { NavDrawerTopBar(role) },
        bottomBar = {
            NavDrawerBottomBar(
                role = role,
                routes = routes,
                navHostController = navHostController
            )
        }
    ) {
        NavDrawerNavHost(
            role = role,
            routes = routes,
            navHostController = navHostController,
            navController = navController,
            contactViewModel = contactViewModel,
            orderViewModel = orderViewModel,
            productViewModel = productViewModel,
            userViewModel = userViewModel,
            locationViewModel = locationViewModel
        )
    }
}

@Composable
fun NavDrawerNavHost(
    role: String,
    routes: Routes,
    navHostController: NavHostController,
    navController: NavController,
    contactViewModel: ContactViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel
) {
    NavHost(
        navController = navHostController,
        startDestination = routes.dashboard
    ) {
        composable(routes.dashboard) {
            when (role) {
                "Admin" -> AdminInventory(productViewModel = productViewModel)
                "Client" -> ClientDashboard()
                "Coop" -> CoopDashboard()
                "Farmer" -> FarmerDashboard()
            }
        }
        if (role == "Admin" || role == "Coop" || role == "Farmer") {
            composable(routes.inventory) {
                when (role) {
                    "Admin" -> AdminInventory(productViewModel = productViewModel)
                    "Coop" -> CoopInventory(navController = navController)
                    "Farmer" -> FarmerInventoryScreen()
                }
            }
        }
        if (role == "Admin") {
            composable(Screen.AdminUserManagement.route) {
                AdminUserManagement(userViewModel = userViewModel)
            }
        }
        if (role == "Client") {
            composable(Screen.ClientContact.route) {
                ClientContact(contactViewModel = contactViewModel)
            }
        }
        if (role == "Client" || role == "Coop" || role == "Farmer") {
            composable(routes.orders) {
                when (role) {
                    "Client" -> ClientOrder(
                        navController = navController,
                        orderViewModel = orderViewModel,
                        userViewModel = userViewModel
                    )
                    "Coop" -> CoopOrder()
                    "Farmer" -> FarmerManageOrder(
                        mainNavController = navController,
                        userViewModel = userViewModel,
                        orderViewModel = orderViewModel
                    )
                }
            }
        }
//        if (role == "Farmer") {
//            composable(Screen.FarmerAddProduct.route) {
//                FarmerAddProductScreen()
//            }
//        }
        composable(Screen.Profile.route) {
            Profile(
                navController = navController,
                userViewModel = userViewModel,
                locationViewModel = locationViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(role: String) {
    when (role) {
        "Client" ->
            TopAppBar(
                title = { Text(text = "Client User 1") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        "Farmer" ->
            TopAppBar(
                title = { Text(text = "Farmer User 1") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF41644A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
    }
}

@Composable
fun NavDrawerBottomBar(
    role: String,
    routes: Routes,
    navHostController: NavHostController
) {
    val currentDestination = navHostController.currentBackStackEntryAsState().value?.destination?.route
    val bottomBarColors: Pair<Color, Color> = when (role) {
        "Admin" -> Pair(Color.White, DarkBlue)
        "Client" -> Pair(Orange, DarkGreen)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color.White, Color.Green)
        else -> Pair(Color.White, Color.Black) // Default
    }

    NavigationBar(
        containerColor = bottomBarColors.first,
        contentColor = bottomBarColors.second
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentDestination == routes.dashboard,
            onClick = {
                navHostController.navigate(routes.dashboard) {
                    popUpTo(0)
                }
            }
        )
        if (role == "Client" || role == "Coop" || role == "Farmer") {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders") },
                label = { Text("Orders") },
                selected = currentDestination == routes.orders,
                onClick = {
                    navHostController.navigate(routes.orders) {
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
                    navHostController.navigate(routes.inventory) {
                        popUpTo(0)
                    }
                }
            )
        }

        if (role == "Admin") {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "User Management") },
                label = { Text("User Management", fontSize = 10.sp) },
                selected = currentDestination == Screen.AdminUserManagement.route,
                onClick = {
                    navHostController.navigate(Screen.AdminUserManagement.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        if (role == "Client") {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Contact Inquiry") },
                label = { Text("Contact") },
                selected = currentDestination == Screen.ClientContact.route,
                onClick = {
                    navHostController.navigate(Screen.ClientContact.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentDestination == Screen.Profile.route,
            onClick = {
                navHostController.navigate(Screen.Profile.route) {
                    popUpTo(0)
                }
            }
        )
    }
}