package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.components.toast.Toast
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.components.toast.toastDelay
import com.coco.celestia.navigation.NavDrawerBottomBar
import com.coco.celestia.navigation.NavDrawerTopBar
import com.coco.celestia.navigation.NavGraph
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.util.checkNetworkConnection
import com.coco.celestia.viewmodel.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

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
    val userData by userViewModel.userData.observeAsState()
    var lastConnectionState = checkNetworkConnection(context)
    val currentDestination = navController.currentBackStackEntry?.destination?.route
    var topBarTitle by remember { mutableStateOf("") }
    var toastStatus by remember { mutableStateOf(ToastStatus.INFO) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastEvent by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
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
    Scaffold(
        topBar = {
            if (currentDestination != null &&
                currentDestination != Screen.Login.route &&
                currentDestination != Screen.Register.route &&
                currentDestination != Screen.Splash.route)
            {
                NavDrawerTopBar(
                    navController = navController,
                    title = topBarTitle,
                    role = userData?.role.toString()
                )
            }
            Toast(message = toastMessage, status = toastStatus, visibility = showToast)
        },
        bottomBar = {
            if (currentDestination != null &&
                currentDestination != Screen.Login.route &&
                currentDestination != Screen.Register.route &&
                currentDestination != Screen.Splash.route &&
                currentDestination != Screen.ForgotPassword.route)
            {
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
            onEvent = { toastEvent = Triple(it.first, it.second, it.third) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}