package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.DOrangeCircle
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.routeHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(
    navController: NavController,
    title: String,
    onSidebarToggle: () -> Unit,
) {

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                )
                if (navController.currentDestination.toString().contains(Screen.AdminUserManagement.route)) {
                    IconButton(onClick = { navController.navigate(Screen.AdminAddUserManagement.route) }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add User",
                            tint = Green1
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { onSidebarToggle() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Button",
                    tint = Green1
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Green4,
            titleContentColor = Green1,
            navigationIconContentColor = Green1
        )
    )
}

@Composable
fun CircleAvatar(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    size: Dp = 48.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    navController: NavController,
    containerColor: Color,
    currentDestination: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Green1
                    )
                }
            },
            navigationIcon = {
                if (currentDestination == Screen.AdminAddProduct.route) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
                            tint = Green1
                        )
                    }
                }
                if (currentDestination == Screen.ClientAddSpecialReq.route) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Back Button",
                            tint = Green1,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                if (currentDestination == Screen.AdminUserManagementAuditLogs.route) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
                            tint = Green1
                        )
                    }
                }
                if (currentDestination == Screen.AdminAddUserManagement.route) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
                            tint = Green1
                        )
                    }
                }
                if (currentDestination == Screen.AddProductInventory.route) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
                            tint = Green1
                        )
                    }
                }
            },
            actions = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    tint = Green1,
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
                titleContentColor = Green1
            ),
            modifier = Modifier
                .background(Color.Transparent)
        )
    }
}

@Composable
fun NavDrawerBottomBar(
    role: String,
    navController: NavController,
) {
    val routes = routeHandler(role)
    val navColor = Green4
    val contentsColor = Green1
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = when (role) {
                "Client" -> Green1
                else -> navColor
            },
            contentColor = contentsColor
        ) {
            // Home
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = when {
                            role == "Client" -> Color.White
                            role == "Farmer" && currentDestination == routes.dashboard -> contentsColor
                            role == "CoopCoffee" && currentDestination == routes.dashboard -> contentsColor
                            else -> contentsColor
                        }
                    )
                },
                label = {
                    Text(
                        "Home",
                        color = when {
                            role == "Client" -> Color.White
                            role == "Farmer" && currentDestination == routes.dashboard -> contentsColor
                            role == "CoopCoffee" && currentDestination == routes.dashboard -> contentsColor
                            else -> contentsColor
                        },
                        fontFamily = mintsansFontFamily
                    )
                },
                selected = currentDestination == routes.dashboard,
                onClick = {
                    navController.navigate(routes.dashboard) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = contentsColor,
                    indicatorColor = navColor
                ),
                modifier = Modifier.semantics { testTag = "android:id/dashboardPage" }
            )

            // Orders section
            if (role == "Coop" || role == "CoopCoffee" || role == "CoopMeat" || role == "Client" || role == "Farmer" || role == "Admin") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Orders",
                            tint = when {
                                role == "Client" -> Color.White
                                role == "Farmer" && currentDestination == routes.orders -> contentsColor
                                role == "CoopCoffee" && currentDestination == routes.orders -> contentsColor
                                role == "Admin" && currentDestination == routes.orders -> contentsColor
                                else -> contentsColor
                            }
                        )
                    },
                    label = {
                        Text(
                            if (role == "Client") "Track Orders" else "Orders",
                            color = when {
                                role == "Client" -> Color.White
                                role == "Farmer" && currentDestination == routes.orders -> contentsColor
                                role == "CoopCoffee" && currentDestination == routes.orders -> contentsColor
                                role == "Admin" && currentDestination == routes.orders -> contentsColor
                                else -> contentsColor
                            },
                            fontFamily = mintsansFontFamily
                        )
                    },
                    selected = currentDestination == routes.orders || currentDestination == Screen.CoopOrder.route,
                    onClick = {
                        when (role) {
                            "Farmer" -> {
                                navController.navigate(Screen.FarmerManageOrder.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                }
                            }
                            "Admin" -> {
                                navController.navigate(Screen.AdminOrders.route) { // Navigate to AdminOrders screen
                                    popUpTo(navController.graph.startDestinationId)
                                }
                            }
                            else -> {
                                navController.navigate(routes.orders) {
                                    popUpTo(navController.graph.startDestinationId)
                                }
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = contentsColor,
                        indicatorColor = navColor
                    ),
                    modifier = Modifier
                        .semantics { testTag = "android:id/ordersPage" }
                )
            }

            // Items section
            if (role == "Admin" || role == "Coop" || role == "CoopCoffee" || role == "CoopMeat") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Items",
                            tint = contentsColor
                        )
                    },
                    label = {
                        Text(
                            if (role == "CoopCoffee" || role == "CoopMeat") "Inventory" else "Items",
                            color = contentsColor,
                            fontFamily = mintsansFontFamily
                        )
                    },
                    selected = currentDestination == routes.inventory ||
                            currentDestination == Screen.AdminAddProduct.route ||
                            currentDestination == Screen.CoopProductInventory.route ||
                            currentDestination == Screen.AddProductInventory.route,
                    onClick = {
                        navController.navigate(routes.inventory) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = contentsColor,
                        indicatorColor = navColor
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/itemsPage" }
                )
            }

            // User Management section for Admin
            if (role == "Admin") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Management",
                            tint = contentsColor
                        )
                    },
                    label = {
                        Text("User Management",
                            fontSize = (9.9).sp,
                            fontFamily = mintsansFontFamily,
                            maxLines = 1,
                            color = contentsColor,
                            overflow = TextOverflow.Ellipsis)
                    },
                    selected = currentDestination == Screen.AdminUserManagement.route ||
                            currentDestination == Screen.AdminAddUserManagement.route ||
                            currentDestination == Screen.AdminUserManagementAuditLogs.route,
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
                    icon = { Icon(painterResource(R.drawable.special_request), contentDescription = "Special Request", tint = Color.White, modifier = Modifier.size(24.dp)) },
                    label = { Text("Request", color = Color.White, fontFamily = mintsansFontFamily) },
                    selected = currentDestination == Screen.ClientSpecialReq.route,
                    onClick = {
                        navController.navigate(Screen.ClientSpecialReq.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = contentsColor,
                        indicatorColor = White1
                    )
                )
            }
            // Notification
            if (role == "Farmer") {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notification", tint = contentsColor) },
                    label = { Text("Notification", color = contentsColor, fontFamily = mintsansFontFamily) },
                    selected = currentDestination == Screen.Notifications.route,
                    onClick = {
                        navController.navigate(Screen.Notifications.route) {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = contentsColor,
                        indicatorColor = DOrangeCircle
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/notificationsPage" }
                )
            }

            // Profile section
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                },
                label = {
                    Text(
                        "Profile",
                        color = Color.White,
                        fontFamily = mintsansFontFamily
                    )
                },
                selected = currentDestination == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = contentsColor,
                    indicatorColor = navColor
                ),
                modifier = Modifier.semantics { testTag = "android:id/profilePage" }
            )
        }
    }
}
