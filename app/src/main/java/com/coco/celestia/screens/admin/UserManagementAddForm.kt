package com.coco.celestia.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.util.sendEmail
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserForm(
    navController: NavController,
    email: String,
    firstname: String,
    lastname: String,
    role: String,
    onEmailChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onRoleChanged: (String) -> Unit
) {
    val maxChar = 25
    var isValidEmail by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Coffee", "Meat")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = { navController.navigate(Screen.AdminUserManagement.route) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Add User",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                if (it.length <= maxChar) {
                    onEmailChanged(it)
                    isValidEmail = isValidEmail(it)
                }
            },
            label = { Text(text = "Email") },
            isError = !isValidEmail,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/emailInputField" }
        )
        Spacer(modifier = Modifier.height(2.dp))

        if (!isValidEmail) {
            Text(
                text = "Invalid email format",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // First Name
        OutlinedTextField(
            value = firstname,
            onValueChange = {
                if (it.length <= maxChar) {
                    onFirstNameChanged(it)
                }
            },
            label = { Text(text = "First Name") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/firstNameInputField" }
        )
        Spacer(modifier = Modifier.height(2.dp))

        // Last Name
        OutlinedTextField(
            value = lastname,
            onValueChange = {
                if (it.length <= maxChar) {
                    onLastNameChanged(it)
                }
            },
            label = { Text(text = "Last Name") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/lastNameInputField" }
        )
        Spacer(modifier = Modifier.height(2.dp))

        // Role
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                readOnly = true,
                value = role,
                onValueChange = {},
                placeholder = { Text("Select Role") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .semantics { testTag = "android:id/roleDropdownField" }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { roleItem ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(roleItem) },
                        onClick = {
                            onRoleChanged(roleItem)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CheckAddUser(
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel,
    navController: NavController,
    email: (String),
    firstname: (String),
    lastname: (String),
    role: (String),
    onRegisterEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userState by userViewModel.userState.observeAsState("")
    var userRole by remember { mutableStateOf("") }
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val subject = "Welcome to Coco: Coop Connects"
    val placeholderPass = "Coco123"
    val body = """
                Dear ${("$firstname $lastname")},

                Welcome to Coco: Coop Connects!

                We are thrilled to have you join our community. Below are your account details:

                **Email:** $email  
                **Password:** $placeholderPass

                Please make sure to change your password after your first login for security purposes. Keep this information secure and do not share your password with anyone.

                Thank you for being a part of our community! We look forward to serving you.

                Best regards,  
                The Coco Team

                ---

                If you did not create an account, please ignore this email.

            """.trimIndent()

    if (email.isNotEmpty() && firstname.isNotEmpty() && lastname.isNotEmpty()) {
        val passwordInput = remember { mutableStateOf("") }
        val (showDialog, setShowDialog) = mutableStateOf(true)

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { setShowDialog(false) },
                title = { Text("Enter Password") },
                text = {
                    TextField(
                        value = passwordInput.value,
                        onValueChange = { passwordInput.value = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.semantics { testTag = "android:id/passwordInputField" }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        setShowDialog(false)
                        userRole = if (role == "Coffee") {
                            "CoopCoffee"
                        } else {
                            "CoopMeat"
                        }
                        userViewModel.addAccount(email, firstname, lastname, placeholderPass, userRole, passwordInput.value)
                        transactionViewModel.recordTransaction(
                            uid = FirebaseAuth.getInstance().uid.toString(),
                            transaction = TransactionData(
                                transactionId = "Transaction-${UUID.randomUUID()}",
                                type = "UserAdded",
                                date = formattedDateTime,
                                description = "Added $firstname $lastname's account ($email)."
                            )
                        )
                    }) {
                        Text("Confirm", modifier = Modifier.semantics { testTag = "android:id/confirmButton" })
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        setShowDialog(false)
                        navController.navigate(Screen.AdminUserManagement.route)
                    }) {
                        Text("Cancel", modifier = Modifier.semantics { testTag = "android:id/cancelButton" })
                    }
                }
            )
        }
    } else {
        onRegisterEvent(
            Triple(
                ToastStatus.WARNING,
                "All fields must be filled",
                System.currentTimeMillis()
            )
        )
        navController.navigate(Screen.AdminUserManagement.route)
    }

    LaunchedEffect(userState) {
        when (userState) {
            is UserState.REGISTER_SUCCESS -> {
                sendEmail(email, subject, body)
                onRegisterEvent(Triple(ToastStatus.SUCCESSFUL, "Registration Successful", System.currentTimeMillis()))
                userViewModel.resetUserState()
                navController.navigate(Screen.AdminUserManagement.route)
            }
            is UserState.ERROR -> {
                onRegisterEvent(Triple(ToastStatus.WARNING, "Error: ${(userState as UserState.ERROR).message}", System.currentTimeMillis()))
                navController.navigate(Screen.AdminUserManagement.route)
            }
            else -> {}
        }
    }
}