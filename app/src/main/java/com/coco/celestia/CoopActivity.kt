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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.dialogs.ExitDialog
import com.coco.celestia.dialogs.LogoutDialog
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.LightGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.viewmodel.UserViewModel

class CoopActivity: ComponentActivity(){
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor) // Hex color))
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Coop.route
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CoopDashboard() {
    Image(painter = painterResource(id = R.drawable.dashboardmock), contentDescription = "Login Image",
        modifier = Modifier.size(1000.dp))

    Spacer(modifier = Modifier.height(50.dp))

    Text(text = "Coop Dashboard Test", fontSize = 50.sp, modifier =  Modifier.padding(50.dp,350.dp))
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoopNavDrawer(mainNavController: NavController, userViewModel: UserViewModel) {
    val navigationController = rememberNavController()
    val context = LocalContext.current
    var exitDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }

    BackHandler {
        exitDialog = true
    }

    if (exitDialog) {
        ExitDialog(
            onDismiss = { exitDialog = false },
            onExit = { (mainNavController.context as Activity).finish() }
        )
    }

    if (logoutDialog) {
        LogoutDialog(
            onDismiss = { logoutDialog = false },
            onLogout = {
                userViewModel.logout()
                mainNavController.navigate(Screen.Login.route) {
                    popUpTo(mainNavController.graph.startDestinationId)
                }
                Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                logoutDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = DarkGreen
            ) {
                val currentDestination = navigationController.currentBackStackEntryAsState().value?.destination?.route
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination == Screen.Coop.route,
                    onClick = {
                        navigationController.navigate(Screen.Coop.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Items") },
                    label = { Text("Items") },
                    selected = currentDestination == Screen.CoopInventory.route,
                    onClick = {
                        navigationController.navigate(Screen.CoopInventory.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = currentDestination == Screen.CoopOrder.route,
                    onClick = {
                        navigationController.navigate(Screen.CoopOrder.route) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { logoutDialog = true }
                )
            }
        }
    ) {
        NavHost(navController = navigationController, startDestination = Screen.Coop.route) {
            composable(Screen.Coop.route) { CoopDashboard() }
            composable(Screen.CoopInventory.route) { CoopInventory(navController = mainNavController) }
            composable(Screen.CoopOrder.route) { CoopOrder() }
        }
    }
}