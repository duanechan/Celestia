package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.routeHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(
    navController: NavController,
    title: String,
    role: String,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    when (role) {
        "Client" -> {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentDestination != "${role.lowercase()}_dashboard") {
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .semantics { testTag = "android:id/backButton" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Back button",
                                )
                            }
                        }
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = { navController.navigate(Screen.AddOrder.route) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .semantics { testTag = "android:id/addOrderButton" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Add order"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightOrange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
            )
        }
        "Admin" -> { TopBar(title = title, navController = navController, gradient = BlueGradientBrush) }
        "Farmer" -> { GradientTopBar(title = title, navController = navController) }
        "Coop" -> { TopBar(title = title, navController = navController, gradient = GreenGradientBrush) }
        "CoopCoffee" -> { TopBar(title = title, navController = navController, gradient = GreenGradientBrush) }
        "CoopMeat" -> { TopBar(title = title, navController = navController, gradient = GreenGradientBrush) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navController: NavController, gradient: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)  // Apply the gradient background
    ) {
        TopAppBar(
            title = {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            actions = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    tint = Color.White,
                    contentDescription = "Calendar Icon",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(30.dp)
                        .clickable {
                            navController.navigate(Screen.Calendar.route)
                        }
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier
                .background(Color.Transparent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(title: String, navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // List of routes where the back button should be hidden
    val noBackButtonRoutes = listOf("farmer_dashboard", "farmer_manage_order", "farmer_items", "profile")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Sand, Yellow4)
                )
            )
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Cocoa,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            navigationIcon = {
                if (currentDestination !in noBackButtonRoutes) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back button",
                            tint = Cocoa
                        )
                    }
                }
            },
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
    navController: NavController,
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
                label = { Text("Dashboard", color = if (role == "Client") Color.White else bottomBarColors.second, fontFamily = mintsansFontFamily) },
                selected = currentDestination == routes.dashboard,
                onClick = {
                    navController.navigate(routes.dashboard) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                modifier = Modifier.semantics { testTag = "android:id/dashboardPage" }
            )

            if(role == "Coop" || role == "CoopCoffee" || role == "CoopMeat"|| role == "Client" || role == "Farmer") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders", tint = if (role == "Client") VeryDarkPurple else bottomBarColors.second) },
                    label = { Text("Orders", color = if (role == "Client") Color.White else bottomBarColors.second, fontFamily = mintsansFontFamily) },
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
                    },
                    modifier = Modifier.semantics { testTag = "android:id/ordersPage" }
                )
            }

            if (role == "Admin" || role == "Coop" || role == "CoopCoffee" || role == "CoopMeat" || role == "Farmer") {
                val (backgroundColor, contentColor) = bottomColorConfig(role)

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Items",
                            tint = contentColor
                        )
                    },
                    label = { Text("Items", color = contentColor, fontFamily = mintsansFontFamily) },
                    selected = currentDestination == routes.inventory || currentDestination == Screen.AdminAddProduct.route,
                    onClick = {
                        navController.navigate(routes.inventory) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    modifier = Modifier.semantics { testTag = "android:id/itemsPage" }
                )
            }

            if (role == "Admin") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Management",
                        )
                    },
                    label = { Text("User Management",
                        fontSize = (9.9).sp,
                        fontFamily = mintsansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis) },
                    selected = currentDestination == Screen.AdminUserManagement.route ||
                            currentDestination == Screen.AdminAddUserManagement.route,
                    onClick = {
                        navController.navigate(Screen.AdminUserManagement.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    modifier = Modifier.semantics { testTag = "android:id/userManagementPage" }
                )
            }

            if (role == "Client") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Contact", tint = VeryDarkPurple) },
                    label = { Text("Contact", color = Color.White, fontFamily = mintsansFontFamily) },
                    selected = currentDestination == Screen.ClientContact.route,
                    onClick = {
                        navController.navigate(Screen.ClientContact.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    modifier = Modifier.semantics { testTag = "android:id/contactPage" }
                )
            }

            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = if (role == "Client") VeryDarkPurple else bottomBarColors.second) },
                label = { Text("Profile", color = if (role == "Client") Color.White else bottomBarColors.second, fontFamily = mintsansFontFamily) },
                selected = currentDestination == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                modifier = Modifier.semantics { testTag = "android:id/profilePage" }
            )
        }

        if ((role == "Coop" || role == "CoopCoffee" || role == "CoopMeat") && currentDestination == Screen.CoopProductInventory.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProductInventory.route) },
                shape = CircleShape,
                containerColor = DarkGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
                    .semantics { testTag = "android:id/coopAddProductButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if ((role == "Coop" || role == "CoopCoffee" || role == "CoopMeat") && currentDestination == Screen.AddProductInventory.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CoopAddProductInventoryDB.route) },
                shape = CircleShape,
                containerColor = DarkGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
                    .semantics { testTag = "android:id/coopAddProductButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (role == "Farmer" && currentDestination == Screen.FarmerItems.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProductInventory.route) },
                shape = CircleShape,
                containerColor = SageGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
                    .semantics { testTag = "android:id/farmerAddProductButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }


        if (role == "Farmer" && currentDestination == Screen.AddProductInventory.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.FarmerAddProductInventoryDB.route) },
                shape = CircleShape,
                containerColor = SageGreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
                    .semantics { testTag = "android:id/farmerAddProductButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (role == "Admin" && currentDestination == Screen.AdminUserManagement.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddUserManagement.route) },
                shape = CircleShape,
                containerColor = LightBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
//                        .background(BlueGradientBrush)
                )
            }
        }
        if (role == "Admin" && currentDestination == Screen.AdminAddUserManagement.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddUserManagementDB.route) },
                shape = CircleShape,
                containerColor = DarkBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
                    .semantics { testTag = "android:id/adminAddUserButton" }

            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (role == "Admin" && currentDestination == Screen.AdminInventory.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddProduct.route) },
                shape = CircleShape,
                containerColor = LightBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
//                        .background(BlueGradientBrush)
                )
            }
        }
        if (role == "Admin" && currentDestination == Screen.AdminAddProduct.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminConfirmAddProduct.route) },
                shape = CircleShape,
                containerColor = DarkBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-30).dp)

            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Confirm Add Product",
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
        "CoopCoffee" -> Pair(Color.White, DarkGreen)
        "CoopMeat" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Yellow4, Cocoa)
        else -> Pair(Color.White, Color.Black) // Default
    }
}

