package com.coco.celestia

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    var email by remember { mutableStateOf("") }
    var errorDialogMessage by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { if (it.length <= 25) email = it },
            label = { Text(text = "Email") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (email.isEmpty()) {
                    Toast.makeText(navController.context, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    userViewModel.sendPasswordResetEmail(email)
                }
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)
        ) {
            Text(text = "Send")
        }

        LaunchedEffect (userState){
            when (userState) {
                is UserState.EMAIL_SENT_SUCCESS -> {
                    Toast.makeText(navController.context, "Reset email sent successfully.", Toast.LENGTH_SHORT).show()
                }

                is UserState.ERROR -> {
                    Toast.makeText(navController.context, "Error: ${(userState as UserState.ERROR).message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}