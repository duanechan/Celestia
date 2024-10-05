package com.coco.celestia.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.ExitDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.routeHandler
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    mainNavController: NavController,
    userViewModel: UserViewModel,
    onLoginEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val navController = rememberNavController()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
    when (userState) {
        is UserState.ERROR -> {
            onLoginEvent(Triple(ToastStatus.FAILED, "Error: ${(userState as UserState.ERROR).message}", System.currentTimeMillis()))
        }
        is UserState.LOGIN_SUCCESS -> {
            onLoginEvent(Triple(ToastStatus.SUCCESSFUL, "Login successful!", System.currentTimeMillis()))
            val role = (userState as UserState.LOGIN_SUCCESS).role
            val route = routeHandler(role)
            mainNavController.navigate(route.dashboard) {
                popUpTo(Screen.Splash.route)
            }
        }
        is UserState.REGISTER_SUCCESS -> {
            onLoginEvent(Triple(ToastStatus.SUCCESSFUL, "Registration successful! You can now log in.", System.currentTimeMillis()))
        }
        else -> {}
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.a),
            contentDescription = "Login Image",
            modifier = Modifier.size(195.dp)
        )

        Text(text = "CoCo", fontSize = 54.sp, fontWeight = FontWeight.Bold)
        Text(text = "Coop Connects", fontSize = 15.sp)

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { if (it.length <= 25) email = it },
            label = { Text(text = "Email") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { if (it.length <= 16) password = it },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                userViewModel.login(email, password)
                Log.d("LoginScreen", "$userState")
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Forgot Password?",
            fontSize = 13.sp,
            modifier = Modifier.clickable {
                mainNavController.navigate(Screen.ForgotPassword.route)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Don't have an account? Register Now!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                mainNavController.navigate(Screen.Register.route)
            }
        )
    }
}
