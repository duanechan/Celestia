package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.R
import com.coco.celestia.screens.client.ClientHelpOverlay
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
    val isHelpOverlayVisible = remember { mutableStateOf(false) }

    when (role) {
        "Client" -> {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentDestination == "${role.lowercase()}_dashboard") {
                            IconButton(
                                onClick = { isHelpOverlayVisible.value = true },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.help),
                                    contentDescription = "Need Help",
                                    tint = Green1
                                )
                            }
                        }

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
                            color = Green1,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (currentDestination != Screen.AddOrder.route &&
                            currentDestination != Screen.OrderDetails.route) {
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green4,
                    titleContentColor = Green1,
                    navigationIconContentColor = Green1
                ),
            )
            ClientHelpOverlay(isVisible = isHelpOverlayVisible)
        }
        "Admin" -> {
            TopBar(
                title = title,
                navController = navController,
                containerColor = Green4,
                currentDestination = currentDestination
            )
        }
        "Farmer" -> {
            TopBar(
                title = title,
                navController = navController,
                containerColor = Green4,
                currentDestination = currentDestination
            )
        }
        "Coop" -> {
            TopBar(
                title = title,
                navController = navController,
                containerColor = Green4,
                currentDestination = currentDestination
            )
        }
        "CoopCoffee" -> {
            TopBar(
                title = title,
                navController = navController,
                containerColor = Green4,
                currentDestination = currentDestination
            )
        }
        "CoopMeat" -> {
            TopBar(
                title = title,
                navController = navController,
                containerColor = Green4,
                currentDestination = currentDestination
            )
        }
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
                        color = Green1,
                        modifier = Modifier.weight(1f)
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
                if (currentDestination == Screen.Calendar.route) {
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
            containerColor = navColor,
            contentColor = contentsColor
        ) {
            // Home
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = when {
                            role == "Client" -> contentsColor
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
                            role == "Client" -> contentsColor
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
            if (role == "Coop" || role == "CoopCoffee" || role == "CoopMeat" || role == "Client" || role == "Farmer") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Orders",
                            tint = when {
                                role == "Client" -> contentsColor
                                role == "Farmer" && currentDestination == routes.orders -> contentsColor
                                role == "CoopCoffee" && currentDestination == routes.orders -> contentsColor
                                else -> contentsColor
                            }
                        )
                    },
                    label = {
                        Text(
                            if (role == "Client") "Track Orders" else "Orders",
                            color = when {
                                role == "Client" -> contentsColor
                                role == "Farmer" && currentDestination == routes.orders -> contentsColor
                                role == "CoopCoffee" && currentDestination == routes.orders-> contentsColor
                                else -> contentsColor
                            },
                            fontFamily = mintsansFontFamily
                        )
                    },
                    selected = currentDestination == routes.orders || currentDestination == Screen.ClientOrderDetails.route,
                    onClick = {
                        if (role == "Farmer") {
                            navController.navigate(Screen.FarmerManageOrder.route) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        } else {
                            navController.navigate(routes.orders) {
                                popUpTo(navController.graph.startDestinationId)
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

            // Notification
            if (role == "Client" || role == "Farmer") {
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
                        tint = contentsColor
                    )
                },
                label = {
                    Text(
                        "Profile",
                        color = contentsColor,
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
