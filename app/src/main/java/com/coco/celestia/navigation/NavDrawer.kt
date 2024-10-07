package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.screens.coop.TopBar
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.LightOrange
import com.coco.celestia.ui.theme.VeryDarkPurple
import com.coco.celestia.ui.theme.mintsansFontFamily
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
                                modifier = Modifier.align(Alignment.CenterStart)
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
                            onClick = { navController.navigate(Screen.Cart.route) },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Your cart"
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
        "Farmer" -> { GradientTopBar(title = title) }
//        "Coop" -> { TopBar(title = title) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(title: String) {
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
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
//                    IconButton(
//                        onClick = {},
//                        modifier = Modifier.align(Alignment.CenterStart)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.KeyboardArrowLeft,
//                            contentDescription = "Back button",
//                        )
//                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                }
            )

            if(role == "Coop" || role == "Client" || role == "Farmer") {
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
                    label = { Text("Items", color = contentColor, fontFamily = mintsansFontFamily) },
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
                            contentDescription = "User Management",
                        )
                    },
                    label = { Text("User Management",
                        fontSize = (9.9).sp,
                        fontFamily = mintsansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis) },
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
                    label = { Text("Contact", color = Color.White, fontFamily = mintsansFontFamily) },
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
                label = { Text("Profile", color = if (role == "Client") Color.White else bottomBarColors.second, fontFamily = mintsansFontFamily) },
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

        if (role == "Admin" && currentDestination == Screen.AdminUserManagement.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddUserManagement.route) },
                shape = CircleShape,
                containerColor = DarkBlue,
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
        if (role == "Admin" && currentDestination == Screen.AdminAddUserManagement.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddUserManagementDB.route) },
                shape = CircleShape,
                containerColor = DarkBlue,
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

