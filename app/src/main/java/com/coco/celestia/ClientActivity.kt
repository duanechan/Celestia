package com.coco.celestia

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.ui.theme.Pink40
import com.coco.celestia.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class ClientActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor)
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Client.route
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ClientDashboard() {
    Image(painter = painterResource(id = R.drawable.clientdashboardmock), contentDescription = "Login Image",
        modifier = Modifier.size(1000.dp))

    Spacer(modifier = Modifier.height(50.dp))

    Text(text = "Client Dashboard Test", fontSize = 50.sp, modifier = Modifier.padding(50.dp, 350.dp))
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientNavDrawer(mainNavController: NavController) {
    val navigationController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    var exitDialog by remember { mutableStateOf(false) }

    BackHandler {
        exitDialog = true
    }

    if (exitDialog) {
        ExitDialog(
            onDismiss = { exitDialog = false },
            onExit = { (mainNavController.context as Activity).finish() }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Client User 1") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Orange,
                contentColor = DarkGreen
            ) {
                val currentDestination = navigationController.currentBackStackEntryAsState().value?.destination?.route
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination == Screen.Client.route,
                    onClick = {
                        navigationController.navigate(Screen.Client.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = currentDestination == Screen.ClientOrder.route,
                    onClick = {
                        navigationController.navigate(Screen.ClientOrder.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Contact Inquiry") },
                    label = { Text("Contact Inquiry") },
                    selected = currentDestination == Screen.ClientContact.route,
                    onClick = {
                        navigationController.navigate(Screen.ClientContact.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        mainNavController.navigate(Screen.Login.route) {
                            popUpTo(mainNavController.graph.startDestinationId)
                        }
                        Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                        userViewModel.logout()
                    }
                )
            }
        }
    ) {
        NavHost(navController = navigationController, startDestination = Screen.Client.route) {
            composable(Screen.Client.route) { ClientDashboard() }
            composable(Screen.Client.route) { ClientOrder() }
            composable(Screen.ClientContact.route) { ClientContact() }
        }
    }
}
