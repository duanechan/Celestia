package com.coco.celestia

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import com.coco.celestia.dialogs.ExitDialog
import com.coco.celestia.util.redirectUser
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun LoginScreen(mainNavController: NavController, userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val firstName = userData?.firstname
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
            Toast.makeText(navController.context, "Error: ${(userState as UserState.ERROR).message}", Toast.LENGTH_SHORT).show()
        }
        is UserState.LOGIN_SUCCESS -> {
            Toast.makeText(navController.context, "Welcome, $firstName!", Toast.LENGTH_SHORT).show()
            val role = (userState as UserState.LOGIN_SUCCESS).role
            redirectUser(role, mainNavController)
        }
        is UserState.REGISTER_SUCCESS -> {
            Toast.makeText(navController.context, "You can now log in!", Toast.LENGTH_SHORT).show()
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
