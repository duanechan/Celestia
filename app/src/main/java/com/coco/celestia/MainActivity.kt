package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
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
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
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
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val menuItems = if (userData.role == "Admin") {
        listOf(
            "Home" to Screen.Admin.route,
            "Special Requests" to Screen.AdminOrders.route,
            "Members" to Screen.AdminUserManagement.route,
            "Clients & Customers" to Screen.AdminClients.route,
            "Settings" to Screen.AdminSettings.route
        )
    } else {
        listOf(
            "Home" to Screen.Admin.route,
            "Profile" to Screen.Profile.route,
            "Settings" to Screen.Profile.route
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
            if(toastEvent.second == "Logging in...") {
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

    println("User Role: ${userData.role}")
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
                        if (label == "Special Requests") {
                            Column {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
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
                                                        // Change navigation : Add status in route
                                                        navController.navigate(Screen.AdminOrders.route)
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
                        } else {
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
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                if (userData != null && shouldShowNavigation) {
                    if (userData!!.role == "Admin") {
                        NavDrawerTopBar(
                            navController = navController,
                            title = topBarTitle,
                            role = userData!!.role,
                            orderViewModel = OrderViewModel(),
                            transactionViewModel = TransactionViewModel(),
                            onUpdateOrder = {},
                            userViewModel = userViewModel,
                            locationViewModel = LocationViewModel(),
                            onLogoutEvent = {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                                if (uid.isNotEmpty()) {
                                    userViewModel.logout(uid = uid)
                                }
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onProfileUpdateEvent = {},
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
                    } else {
                        TopBar(
                            title = topBarTitle,
                            navController = navController,
                            containerColor = Green4,
                            currentDestination = navController.currentBackStackEntry?.destination?.route
                        )
                    }
                }
                Toast(message = toastMessage, status = toastStatus, visibility = showToast)
            },
            bottomBar = {
                if (shouldShowNavigation && userData?.role !in listOf("Admin")) {
                    NavDrawerBottomBar(
                        role = userData?.role.toString(),
                        navController = navController
                    )
                }
            }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                userRole = userData?.role.toString(),
                onNavigate = { topBarTitle = it },
                onEvent = {  toastEvent = Triple(it.first, it.second, it.third)
                          println(toastEvent)},
                modifier = if (shouldShowNavigation) {
                    Modifier.padding(paddingValues)
                } else {
                    Modifier
                }
            )
        }
    }
}