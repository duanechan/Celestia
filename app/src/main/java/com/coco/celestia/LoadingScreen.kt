package com.coco.celestia

import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoadingScreen(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    val currentUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            currentUser.uid.let { userViewModel.fetchUser(it) }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Loading.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(userState, userData) {
        when (userState) {
            is UserState.SUCCESS -> {
                val role = userData?.role
                val destination = when (role) {
                    "Farmer" -> Screen.Farmer.route
                    "Admin" -> Screen.Admin.route
                    "Client" -> Screen.Client.route
                    "Coop" -> Screen.Coop.route
                    else -> Screen.Login.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Loading.route) { inclusive = true }
                }
            }
            is UserState.ERROR -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Loading.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GlideImage(imageRes = R.drawable.catroll)
        }
        item {
            Text("Loading...", fontSize = 20.sp, fontFamily = FontFamily.Monospace)
        }
    }
}


@Composable
fun GlideImage(modifier: Modifier = Modifier, @DrawableRes imageRes: Int) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            ImageView(context).apply {
                Glide.with(context)
                    .asGif()
                    .load(imageRes)
                    .apply(RequestOptions().fitCenter())
                    .into(this)
            }
        },
        modifier = modifier
    )
}


@Preview
@Composable
fun GifPreview() {
    GlideImage(imageRes = R.drawable.catroll)
}
