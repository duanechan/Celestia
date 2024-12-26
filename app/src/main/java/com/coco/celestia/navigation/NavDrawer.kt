package com.coco.celestia.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.coop.admin.AdminHome
import com.coco.celestia.screens.coop.admin.AdminOrders
import com.coco.celestia.screens.coop.admin.AdminUserManagement
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.routeHandler
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerTopBar(
    navController: NavController,
    title: String,
    role: String,
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel,
    onUpdateOrder: (Triple<ToastStatus, String, Long>) -> Unit,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel,
    onLogoutEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onProfileUpdateEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val isHelpOverlayVisible = remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userData by userViewModel.userData.observeAsState(initial = UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    val menuItems = if (role == "Admin") {
        listOf(
            "Home" to Screen.Admin.route,
            "Special Requests" to Screen.AdminOrders.route,
            "Members" to Screen.AdminUserManagement.route,
            "Clients & Customers" to Screen.AdminInventory.route,
            "Settings" to Screen.Profile.route
        )
    } else {
        listOf(
            "Home" to Screen.Admin.route,
            "Profile" to Screen.Profile.route,
            "Settings" to Screen.Profile.route
        )
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color(0xFFE0E0E0))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clickable {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(Screen.Profile.route)
                                }
                            }
                    ) {
                        CircleAvatar(
                            text = userData.firstname.take(1),
                            backgroundColor = Green4,
                            textColor = Green1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(userData.firstname + " " + userData.lastname, fontWeight = FontWeight.Bold)
                            Text(userData.email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Divider(color = Color.Gray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    menuItems.forEach { (label, route) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                        ) {
                            when (label) {
                                "Home" -> Icon(Icons.Default.Home, contentDescription = null, tint = Green1)
                                "Special Requests" -> Icon(Icons.Default.Info, contentDescription = null, tint = Green1)
                                "Members" -> Icon(Icons.Default.Face, contentDescription = null, tint = Green1)
                                "Clients & Customers" -> Icon(Icons.Default.Person, contentDescription = null, tint = Green1)
                                "Settings" -> Icon(Icons.Default.Settings, contentDescription = null, tint = Green1)
                                "Profile" -> Icon(Icons.Default.Person, contentDescription = null, tint = Green1)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                color = if (currentDestination == route) Green1 else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = Green1,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
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
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    Screen.AdminOrders.route -> AdminOrders(
                        userRole = role,
                        orderViewModel = orderViewModel,
                        transactionViewModel = transactionViewModel,
                        onUpdateOrder = onUpdateOrder
                    )
                    Screen.AdminUserManagement.route -> AdminUserManagement(
                        navController = navController,
                        userViewModel = userViewModel,
                        transactionViewModel = transactionViewModel
                    )
                    Screen.Profile.route -> Profile(
                        navController = navController,
                        userViewModel = userViewModel,
                        locationViewModel = locationViewModel,
                        onLogoutEvent = onLogoutEvent,
                        onProfileUpdateEvent = onProfileUpdateEvent
                    )
                    else -> {
                        AdminHome(navController)
                    }
                }
            }
        }
    }
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
            if (role == "Coop" || role == "CoopCoffee" || role == "CoopMeat" || role == "Client" || role == "Farmer" || role == "Admin") {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Orders",
                            tint = when {
                                role == "Client" -> contentsColor
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
                                role == "Client" -> contentsColor
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
