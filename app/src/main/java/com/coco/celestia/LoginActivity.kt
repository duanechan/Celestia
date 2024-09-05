package com.coco.celestia

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.FirebaseApp

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {

                }
            }
        }
    }
}

@Composable
fun LoginScreen(mainNavController: NavController) {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var exitDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }

    BackHandler {
        exitDialog = true
    }

    if (exitDialog) {
        ExitDialog(
            onDismiss = { exitDialog = false },
            onExit = { (mainNavController.context as Activity).finish() }
        )
    }

    LaunchedEffect(userState) {
        when (userState) {
            is UserState.ERROR -> {
                Toast.makeText(navController.context, "Error: ${(userState as UserState.ERROR).message}", Toast.LENGTH_SHORT).show()
            }
            is UserState.LOGIN_SUCCESS -> {
                Toast.makeText(navController.context, "Login Successful", Toast.LENGTH_SHORT).show()
                val role = (userState as UserState.LOGIN_SUCCESS).role
                when (role) {
                    "Farmer" -> mainNavController.navigate(Screen.Farmer.route)
                    "Client" -> mainNavController.navigate(Screen.Client.route)
                    "Admin" -> mainNavController.navigate(Screen.Admin.route)
                    "Coop" -> mainNavController.navigate(Screen.Coop.route)
                    // TODO: Handle unknown role
                    else -> mainNavController.navigate(Screen.Home.route)
                }
            }
            is UserState.REGISTER_SUCCESS -> {
                Toast.makeText(navController.context, "You can now log in!", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
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
                if (email.isEmpty() || password.isEmpty()) {
                    errorDialogMessage = "Fields cannot be empty"
                    showDialog = true
                } else {
                    userViewModel.login(email, password)
                }
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)
        ) {
            Text(text = "Login")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = if (errorDialogMessage.isNotEmpty()) "Login Failed" else "Login Successful!") },
                text = { Text(text = if (errorDialogMessage.isNotEmpty()) errorDialogMessage else "Welcome back!") },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text(if (errorDialogMessage.isNotEmpty()) "Retry" else "Let's Go!")
                    }
                }
            )
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
