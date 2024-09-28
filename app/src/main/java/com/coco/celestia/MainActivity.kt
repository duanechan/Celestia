package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.navigation.NavDrawerBottomBar
import com.coco.celestia.navigation.NavDrawerTopBar
import com.coco.celestia.navigation.NavGraph
import com.coco.celestia.screens.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.viewmodel.ProductData
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
                    App()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState()
    val role = userData?.role
    val firstName = userData?.firstname
    val lastName = userData?.lastname
    val currentDestination = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (role != null ||
                currentDestination != null &&
                currentDestination != Screen.Login.route &&
                currentDestination != Screen.Register.route &&
                currentDestination != Screen.Splash.route)
            {
                NavDrawerTopBar(
                    role = role.toString(),
                    firstName = firstName.toString(),
                    lastName = lastName.toString()
                )
            }
        },
        bottomBar = {
            if (role != null ||
                currentDestination != null &&
                currentDestination != Screen.Login.route &&
                currentDestination != Screen.Register.route &&
                currentDestination != Screen.Splash.route &&
                currentDestination != Screen.ForgotPassword.route)
            {
                NavDrawerBottomBar(
                    role = role.toString(),
                    navController = navController
                )
            }
        }
    ) { // APP CONTENT
        NavGraph(
            navController = navController
        )
    }
}