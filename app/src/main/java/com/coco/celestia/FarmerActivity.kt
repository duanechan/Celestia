package com.coco.celestia


import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.ui.theme.Pink40
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class FarmerActivity: ComponentActivity(){
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
                        startDestination = Screen.Farmer.route
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FarmerDashboard() {
    Image(painter = painterResource(id = R.drawable.clientdashboardmock), contentDescription = "Login Image",
        modifier = Modifier.size(1000.dp))

    Spacer(modifier = Modifier.height(50.dp))

    Text(text = "Farmer Dashboard Test", fontSize = 50.sp, modifier =  Modifier.padding(50.dp,350.dp))
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerNavDrawer(mainNavController: NavController){
    val navigationController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val userViewModel: UserViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current.applicationContext
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(modifier = Modifier
                    .background(Pink40)
                    .fillMaxWidth()
                    .height(150.dp)
                ){
                    Text(text = "Farmer User1")
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "Dashboard", color = Orange) },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard", tint = DarkGreen)},
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screen.Farmer.route){
                            popUpTo(0)
                        }
                    })

                NavigationDrawerItem(
                    label = { Text(text = "Orders", color = Orange) },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Items", tint = DarkGreen)},
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screen.FarmerInventory.route){
                            popUpTo(0)
                        }
                    })

                NavigationDrawerItem(
                    label = { Text(text = "Contact Inquiry", color = Orange) },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Orders", tint = DarkGreen)},
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screen.FarmerManageOrder.route){
                            popUpTo(0)
                        }
                    })


                NavigationDrawerItem(
                    label = { Text(text = "Logout", color = Orange) },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = DarkGreen)},
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        userViewModel.logout()
                        mainNavController.navigate(Screen.Login.route) {
                            popUpTo(mainNavController.graph.startDestinationId)
                        }
                        Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                    })
            }
        }) {
        Scaffold(
            topBar = {
                val coroutineScope = rememberCoroutineScope()
                TopAppBar(title = { Text(text = "Client User 1")},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Orange,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ), navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Menu, contentDescription = "MenuButton"
                            )
                        }
                    })
            }
        ) {
            NavHost(
                navController = navigationController,
                startDestination = Screen.Farmer.route
            ){
                composable(Screen.Farmer.route){ FarmerDashboard() }
                composable(Screen.FarmerInventory.route){ FarmerInventory() }
                composable(Screen.FarmerManageOrder.route){ FarmerManageOrder() }
            }
        }
    }
}
