package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.components.toast.Toast
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.components.toast.toastDelay
import com.coco.celestia.navigation.CircleAvatar
import com.coco.celestia.navigation.NavDrawerBottomBar
import com.coco.celestia.navigation.NavDrawerTopBar
import com.coco.celestia.navigation.NavGraph
import com.coco.celestia.navigation.TopBar
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.util.checkNetworkConnection
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.UserData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                        .semantics { testTagsAsResourceId = true } //testing
                ) {
                    App()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference
            .child("users/$uid/online")
            .setValue(true)
    }

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference
            .child("users/$uid/online")
            .setValue(true)
    }

    override fun onStop() {
        super.onStop()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference
            .child("users/$uid/online")
            .setValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference
            .child("users/$uid/online")
            .setValue(false)
    }
}

@Composable
fun AppStateAndContent(
    content: @Composable (
        navController: NavHostController,
        userData: UserData,
        facilityData: FacilityData,
        topBarTitle: String,
        toastStatus: ToastStatus,
        showToast: Boolean,
        toastMessage: String,
        drawerState: DrawerState,
        scope: CoroutineScope,
        expandedMenus: Set<String>,
        menuItems: List<Pair<String, String>>,
        statuses: List<String>,
        shouldShowNavigation: Boolean,
        currentDestination: String?,
        onTopBarTitleChange: (String) -> Unit,
        onToastEvent: (Triple<ToastStatus, String, Long>) -> Unit,
        onExpandMenu: (String) -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val facilityViewModel: FacilityViewModel = viewModel()
    val facilityData by facilityViewModel.facilityData.observeAsState(FacilityData())
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    var lastConnectionState = checkNetworkConnection(context)
    val currentDestination = navController.currentBackStackEntry?.destination?.route
    var topBarTitle by remember { mutableStateOf("") }
    var toastStatus by remember { mutableStateOf(ToastStatus.INFO) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastEvent by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isHelpOverlayVisible = remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var expandedMenus by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(userData.email) {
        if (userData.email.isNotEmpty()) {
            facilityViewModel.fetchFacilities()
        }
    }

    val commonCoopMenuItems = listOf(
        "Home" to Screen.Coop.route,
        "Products" to Screen.CoopInventory.route,
        "Sales" to Screen.CoopSales.route,
        "Purchases" to Screen.CoopPurchases.route,
        "Reports" to Screen.CoopReports.route,
        "Settings" to Screen.AdminSettings.route
    )

    val menuItems = if (userData.role.startsWith("Coop")) {
        val userFacility = facilitiesData.find { facility ->
            facility.emails.contains(userData.email)
        }

        if (userFacility != null) {
            listOf("${userFacility.name.uppercase()} FACILITY" to "") + commonCoopMenuItems
        } else {
            listOf("NO FACILITY YET" to "") + commonCoopMenuItems
        }
    } else {
        listOf(
            "Home" to Screen.Admin.route,
            "Special Requests" to Screen.AdminSpecialRequests.route,
            "Members" to Screen.AdminUserManagement.route,
            "Clients & Customers" to Screen.AdminClients.route,
            "Settings" to Screen.AdminSettings.route
        )
    }

    val statuses = listOf("To Review", "In Progress", "Completed", "Cancelled", "Turned Down")
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        onDispose {}
    }

    LaunchedEffect(Unit) {
        var firstLaunch = true
        while (true) {
            val currentConnectionState = checkNetworkConnection(context)
            if (firstLaunch || currentConnectionState != lastConnectionState) {
                toastEvent = if (!currentConnectionState) {
                    Triple(ToastStatus.FAILED, "No internet connection.", System.currentTimeMillis())
                } else {
                    Triple(ToastStatus.SUCCESSFUL, "Online!", System.currentTimeMillis())
                }
                lastConnectionState = currentConnectionState
                firstLaunch = false
            }
            delay(toastDelay)
        }
    }

    LaunchedEffect(toastEvent) {
        if (toastEvent.second.isNotEmpty()) {
            toastStatus = toastEvent.first
            toastMessage = toastEvent.second
            if (toastEvent.second == "Logging in...") {
                showToast = true
            } else {
                showToast = true
                delay(toastDelay)
                showToast = false
            }
        }
    }

    val shouldShowNavigation = currentDestination != null &&
            currentDestination != Screen.Login.route &&
            currentDestination != Screen.Register.route &&
            currentDestination != Screen.Splash.route &&
            currentDestination != Screen.ForgotPassword.route

    content(
        navController,
        userData,
        facilityData,
        topBarTitle,
        toastStatus,
        showToast,
        toastMessage,
        drawerState,
        scope,
        expandedMenus,
        menuItems,
        statuses,
        shouldShowNavigation,
        currentDestination,
        { topBarTitle = it },
        { toastEvent = it },
        { menu ->
            expandedMenus = if (expandedMenus.contains(menu)) {
                expandedMenus - menu
            } else {
                expandedMenus + menu
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AppStateAndContent { navController, userData, facilityData, topBarTitle, toastStatus, showToast,
                         toastMessage, drawerState, scope, expandedMenus, menuItems, statuses,
                         shouldShowNavigation, currentDestination, onTopBarTitleChange, onToastEvent,
                         onExpandMenu ->
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
                            .background(Color(0xFFF5F5F5))
                            .padding(16.dp)
                    ) {
                        // Header
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
                        Spacer(modifier = Modifier.height(16.dp))

                        // Menu Items
                        menuItems.forEach { (label, route) ->
                            when {
                                label == "NO FACILITY YET" || label.endsWith("FACILITY") -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 18.sp,
                                            color = if (label == "NO FACILITY YET") Color.Red else Green1,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                                label in listOf("Products", "Sales", "Purchases") -> {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clickable { onExpandMenu(label) }
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (label) {
                                                    "Products" -> Icons.Default.List
                                                    "Sales" -> Icons.Default.ShoppingCart
                                                    "Purchases" -> Icons.Default.ShoppingCart
                                                    else -> Icons.Default.List
                                                },
                                                contentDescription = null,
                                                tint = if (currentDestination == route) Green1 else Color.Gray
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = label,
                                                color = if (currentDestination == route) Green1 else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                painter = painterResource(
                                                    id = if (expandedMenus.contains(label))
                                                        R.drawable.expand_less
                                                    else
                                                        R.drawable.expand_more
                                                ),
                                                contentDescription = null,
                                                tint = if (currentDestination == route) Green1 else Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        if (expandedMenus.contains(label)) {
                                            val subItems = when (label) {
                                                "Products" -> listOf(
                                                    "In-Store Products" to Screen.CoopInStoreProducts.route,
                                                    "Online Products" to Screen.CoopOnlineProducts.route
                                                )
                                                "Sales" -> listOf(
                                                    "In-Store Sales" to Screen.CoopInStoreSales.route,
                                                    "Online Sales" to Screen.CoopOnlineSales.route
                                                )
                                                "Purchases" -> listOf(
                                                    "Vendors" to Screen.CoopVendors.route,
                                                    "Purchase Orders" to Screen.CoopPurchases.route
                                                )
                                                else -> emptyList()
                                            }

                                            subItems.forEach { (subLabel, subRoute) ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 44.dp, top = 8.dp)
                                                        .clickable {
                                                            scope.launch {
                                                                drawerState.close()
                                                                navController.navigate(subRoute)
                                                            }
                                                        }
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = subLabel,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                label == "Special Requests" -> {
                                    Column {
                                        Row (
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                                .clickable { isDropdownExpanded = !isDropdownExpanded }
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Green1)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = label,
                                                color = if (currentDestination == route) Green1 else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                painter = painterResource(id = if (isDropdownExpanded) R.drawable.expand_less else R.drawable.expand_more),
                                                contentDescription = null,
                                                tint = Green1,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }

                                        if (isDropdownExpanded) {
                                            statuses.forEach { status ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                        .padding(start = 48.dp)
                                                        .clickable {
                                                            isDropdownExpanded = false
                                                            scope.launch {
                                                                drawerState.close()
                                                                navController.navigate(Screen.AdminSpecialRequests.createRoute(status))
                                                            }
                                                        }
                                                ) {
                                                    when (status) {
                                                        "To Review" -> Icon(painterResource(R.drawable.review), null, tint = Green1, modifier = Modifier.size(24.dp))
                                                        "In Progress" -> Icon(painterResource(R.drawable.progress), null, tint = Green1, modifier = Modifier.size(24.dp))
                                                        "Completed" -> Icon(painterResource(R.drawable.completed), null, tint = Green1, modifier = Modifier.size(24.dp))
                                                        "Cancelled" -> Icon(painterResource(R.drawable.cancelled), null, tint = Green1, modifier = Modifier.size(24.dp))
                                                        "Turned Down" -> Icon(painterResource(R.drawable.turned_down), null, tint = Green1, modifier = Modifier.size(24.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = status,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        }
                                }
                                else -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
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
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (label) {
                                                "Home" -> Icons.Default.Home
                                                "Settings" -> Icons.Default.Settings
                                                "Reports" -> Icons.Default.Info
                                                else -> Icons.Default.Home
                                            },
                                            contentDescription = null,
                                            tint = if (currentDestination == route) Green1 else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = label,
                                            color = if (currentDestination == route) Green1 else Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            drawerState = drawerState
        ) {
            Scaffold(
                topBar = {
                    if (userData != null && shouldShowNavigation) {
                        when {
                            userData.role.startsWith("Coop") || userData.role == "Admin" -> {
                                NavDrawerTopBar(
                                    navController = navController,
                                    title = topBarTitle,
                                    currentDestination = currentDestination,
                                    onSidebarToggle = {
                                        scope.launch {
                                            if (drawerState.isClosed) {
                                                drawerState.open()
                                            } else {
                                                drawerState.close()
                                            }
                                        }
                                    }
                                )
                            }
                            else -> {
                                TopBar(
                                    title = topBarTitle,
                                    navController = navController,
                                    containerColor = Green4,
                                    currentDestination = currentDestination
                                )
                            }
                        }
                    }
                    Toast(message = toastMessage, status = toastStatus, visibility = showToast)
                },
                bottomBar = {
                    if (shouldShowNavigation && !(userData.role.startsWith("Coop") || userData.role == "Admin")) {
                        NavDrawerBottomBar(
                            role = userData.role,
                            navController = navController
                        )
                    }
                }
            ) { paddingValues ->
                NavGraph(
                    navController = navController,
                    userRole = userData.role.toString(),
                    onNavigate = onTopBarTitleChange,
                    onEvent = onToastEvent,
                    modifier = if (shouldShowNavigation) {
                        Modifier.padding(paddingValues)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}