package com.coco.celestia.screens

import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coco.celestia.R
import com.coco.celestia.Screen
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(userViewModel: UserViewModel, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(currentUser) {
        scope.launch {
            if (currentUser != null) {
                currentUser.uid.let { userViewModel.fetchUser(it) }
            } else {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(userState, userData) {
        scope.launch {
            visible = false
            when (userState) {
                is UserState.SUCCESS -> {
                    val firstName = "${userData?.firstname}"
                    val role = userData?.role
                    val destination = when (role) {
                        "Farmer" -> Screen.Farmer.route
                        "Admin" -> Screen.Admin.route
                        "Client" -> Screen.Client.route
                        "Coop" -> Screen.Coop.route
                        else -> Screen.Login.route
                    }
//                    Toast.makeText(navController.context, "Welcome back, $firstName!", Toast.LENGTH_SHORT).show()
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                is UserState.ERROR -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.a),
                    contentDescription = "CoCo Logo",
                    modifier = Modifier.size(195.dp)
                )
            }
            item {
                CircularProgressIndicator()
            }
        }
    }
}
