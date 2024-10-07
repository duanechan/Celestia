package com.coco.celestia.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.checkNetworkConnection
import com.coco.celestia.util.routeHandler
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    userViewModel: UserViewModel,
    navController: NavHostController,
    onAccessEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val connection = checkNetworkConnection(LocalContext.current)
    var visible by remember { mutableStateOf(true) }
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        scope.launch {
            if (currentUser != null) {
                onAccessEvent(Triple(ToastStatus.INFO, "Checking credentials...", System.currentTimeMillis()))
                currentUser.uid.let { userViewModel.fetchUser(it) }
            } else {
                navController.navigate(Screen.Login.route)
            }
        }
    }

    LaunchedEffect(userState, userData) {
        scope.launch {
            when (userState) {
                is UserState.SUCCESS -> {
                    val role = userData?.role.toString()
                    val route = routeHandler(role)
                    onAccessEvent(Triple(ToastStatus.SUCCESSFUL, "Welcome back, ${userData?.firstname}!", System.currentTimeMillis()))
                    navController.navigate(route.dashboard) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                is UserState.ERROR -> {
                    navController.navigate(Screen.Login.route)
                }
                else -> {}
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (connection) {
                    Image(
                        painter = painterResource(id = R.drawable.a),
                        contentDescription = "CoCo Logo",
                        modifier = Modifier.size(195.dp)
                    )
                    visible = false
                } else {
                    Text(
                        text = "Go online and we'll put you back on track.",
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 35.sp
                    )
                }
            }
        }
    }
}
