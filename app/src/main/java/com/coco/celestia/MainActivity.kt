package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.components.toast.Toast
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.navigation.NavDrawerBottomBar
import com.coco.celestia.navigation.NavDrawerTopBar
import com.coco.celestia.navigation.NavGraph
import com.coco.celestia.screens.admin.ActionButtons
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.util.checkNetworkConnection
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val role = userData?.role
    val firstName = userData?.firstname
    val lastName = userData?.lastname
    val currentDestination = navController.currentBackStackEntry?.destination?.route
    val selectedUsers = userViewModel.selectedUsers
    var toastStatus by remember { mutableStateOf(ToastStatus.INFO) }
    var toastShown by remember { mutableStateOf(true) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastEvent by remember { mutableStateOf(Pair(ToastStatus.INFO, "")) }
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        onDispose {}
    }

    LaunchedEffect(Unit) {
        while(true) {
            val connection = checkNetworkConnection(context)
            if (!connection) {
                if(!toastShown) {
                    toastEvent = Pair(ToastStatus.FAILED, "You're offline. Please check your internet connection.")
                }
                toastShown = true
            } else {
                if (toastShown) {
                    toastEvent = Pair(ToastStatus.SUCCESSFUL, "Online!")
                }
                toastShown = false
            }
            delay(2000)
        }
    }

    LaunchedEffect(toastEvent) {
        if (toastEvent.second.isNotEmpty()) {
            toastStatus = toastEvent.first
            toastMessage = toastEvent.second
            showToast = true
            delay(2000)
            showToast = false
        }
    }

    Scaffold(
        topBar = {
            Toast(message = toastMessage, status = toastStatus, visibility = showToast)

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
            if (selectedUsers.isNotEmpty()) {
                ActionButtons()

                BackHandler {
                    userViewModel.clearSelectedUsers()
                }
            } else {
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
        }
    ) { // APP CONTENT
        NavGraph(
            navController = navController
        ) {
            toastEvent = it
        }
    }
}