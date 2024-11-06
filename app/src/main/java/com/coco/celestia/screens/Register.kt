package com.coco.celestia.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.ui.theme.*

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
            title = { Text(text = "Register As", color = BrownCoffee2, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Choose your role", color = BrownCoffee2, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedRole = "Farmer"
                                    showRoleDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(BrownCoffee),
                                modifier = Modifier.semantics { testTag = "android:id/farmerButton" }
                            ) {
                                Text(text = "Farmer")
                            }

                            Button(
                                onClick = {
                                    selectedRole = "Client"
                                    showRoleDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(BrownCoffee),
                                modifier = Modifier.semantics { testTag = "android:id/clientButton" }
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
                        },
                        colors = ButtonDefaults.buttonColors(Gray),
                        modifier = Modifier.semantics { testTag = "android:id/cancelButton" }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(id = R.drawable.a), contentDescription = "Login Image",
                    modifier = Modifier.size(195.dp))

                Text(text = "CoCo", fontSize = 54.sp, fontWeight = FontWeight.Bold, color = BrownCoffee2)
                Text(text = "Coop Connects", fontSize = 15.sp, color = BrownCoffee2)
                Spacer(modifier = Modifier.height(35.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            email = it
                            isValidEmail = isValidEmail(it)
                        }
                    },
                    label = { Text(text = "Email", color = BrownCoffee2) },
                    isError = !isValidEmail,
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrownCoffee2,
                        unfocusedBorderColor = BrownCoffee2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/emailFieldReg" }
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
                    label = { Text(text = "First Name", color = BrownCoffee2) },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrownCoffee2,
                        unfocusedBorderColor = BrownCoffee2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/firstNameField" }
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            lastName = it
                        }
                    },
                    label = { Text(text = "Last Name", color = BrownCoffee2) },
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrownCoffee2,
                        unfocusedBorderColor = BrownCoffee2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/lastNameField" }
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            password = it
                        }
                    },
                    label = { Text(text = "Password", color = BrownCoffee2) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrownCoffee2,
                        unfocusedBorderColor = BrownCoffee2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/passwordField" }
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            confirmPassword = it
                        }
                    },
                    label = { Text(text = "Confirm Password", color = BrownCoffee2) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrownCoffee2,
                        unfocusedBorderColor = BrownCoffee2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/confirmPassField" }
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
                    colors = ButtonDefaults.buttonColors(BrownCoffee),
                    modifier = Modifier
                        .semantics { testTag = "android:id/registerButton" }
                        .width(285.dp)
                        .height(50.dp)
                ) {
                    Text(text = "Register", color = Color.White)
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}