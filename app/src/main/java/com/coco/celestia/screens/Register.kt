package com.coco.celestia.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    onRegisterEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val maxChar = 25
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showRoleDialog by remember { mutableStateOf(true) }
    var selectedRole by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(true) }

    LaunchedEffect(userState) {
        when (userState) {
            is UserState.ERROR -> {
                onRegisterEvent(Triple(ToastStatus.FAILED, (userState as UserState.ERROR).message, System.currentTimeMillis()))
            }
            is UserState.REGISTER_SUCCESS -> {
                onRegisterEvent(Triple(ToastStatus.SUCCESSFUL, "Registration Successful", System.currentTimeMillis()))
                userViewModel.resetUserState()
                navController.navigate(Screen.Login.route)
            }
            else -> {}
        }
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text(text = "Register As") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Choose your role")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedRole = "Farmer"
                                    showRoleDialog = false
                                }
                            ) {
                                Text(text = "Farmer")
                            }

                            Button(
                                onClick = {
                                    selectedRole = "Client"
                                    showRoleDialog = false
                                }
                            ) {
                                Text(text = "Client")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            showRoleDialog = false
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.a), contentDescription = "Login Image",
                modifier = Modifier.size(195.dp))

            Text(text = "CoCo", fontSize = 54.sp, fontWeight = FontWeight.Bold)
            Text(text = "Coop Connects", fontSize = 15.sp)
            Spacer(modifier = Modifier.height(35.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length <= maxChar) {
                        email = it
                        isValidEmail = isValidEmail(it)
                    }
                },
                label = { Text(text = "Email") },
                isError = !isValidEmail,
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (!isValidEmail) {
                Text(
                    text = "Invalid email format",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    if (it.length <= maxChar) {
                        firstName = it
                    }
                },
                label = { Text(text = "First Name") },
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    if (it.length <= maxChar) {
                        lastName = it
                    }
                },
                label = { Text(text = "Last Name") },
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length <= maxChar) {
                        password = it
                    }
                },
                label = { Text(text = "Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    if (it.length <= maxChar) {
                        confirmPassword = it
                    }
                },
                label = { Text(text = "Confirm Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && password.isNotEmpty()) {
                        userViewModel.register(email, firstName, lastName, password, selectedRole)
                    } else {
                        onRegisterEvent(Triple(ToastStatus.WARNING, "All text must be filled", System.currentTimeMillis()))
                    }
                },
                modifier = Modifier
                    .width(285.dp)
                    .height(50.dp)) {
                Text(text = "Register")
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}