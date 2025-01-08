package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.util.sendEmail
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
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
    facilityViewModel: FacilityViewModel,
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel,
    onEmailChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onRoleChanged: (String) -> Unit,
    onRegisterEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val maxChar = 100
    var isValidEmail by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showCheckDialog by remember { mutableStateOf(false) }

    // Observe facilities from ViewModel
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(initial = FacilityState.LOADING)

    // Effect to fetch facilities when the composable is first created
    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Email field
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { testTag = "android:id/invalidEmailMessage" }
            )
        }

        // First Name field
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

        // Last Name field
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

        // Facility/Role Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.semantics { testTag = "android:id/roleDropdownBox" }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = if (role.isEmpty()) "" else role.removePrefix("Coop"),
                onValueChange = {},
                placeholder = { Text("Select Facility") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .semantics { testTag = "android:id/roleDropdownField" }
            )

            when (facilityState) {
                is FacilityState.LOADING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is FacilityState.EMPTY -> {
                    Text(
                        text = "No facilities available",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is FacilityState.SUCCESS -> {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.semantics { testTag = "android:id/roleDropdownMenu" }
                    ) {
                        facilitiesData.forEach { facility ->
                            DropdownMenuItem(
                                text = { Text(facility.name) },
                                onClick = {
                                    onRoleChanged("Coop${facility.name}")
                                    expanded = false
                                },
                                modifier = Modifier.semantics { testTag = "android:id/roleItem_${facility.name}" }
                            )
                        }
                    }
                }
                is FacilityState.ERROR -> {
                    Text(
                        text = (facilityState as FacilityState.ERROR).message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Add User Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Button(
                onClick = { showCheckDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { testTag = "android:id/addUserButton" },
                enabled = email.isNotEmpty() &&
                        firstname.isNotEmpty() &&
                        lastname.isNotEmpty() &&
                        role.isNotEmpty() &&
                        isValidEmail
            ) {
                Text(
                    text = "Add User",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // Show CheckAddUser dialog when needed
    if (showCheckDialog) {
        CheckAddUser(
            userViewModel = userViewModel,
            transactionViewModel = transactionViewModel,
            navController = navController,
            email = email,
            firstname = firstname,
            lastname = lastname,
            role = role,
            onRegisterEvent = { event ->
                onRegisterEvent(event)
                if (event.first != ToastStatus.INFO) {
                    showCheckDialog = false
                }
            }
        )
    }
}

@Composable
fun CheckAddUser(
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel,
    navController: NavController,
    email: String,
    firstname: String,
    lastname: String,
    role: String,
    onRegisterEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userState by userViewModel.userState.observeAsState("")
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

    var adminPassword by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(true) }
    var isButtonEnabled by remember { mutableStateOf(true) }
    val adminEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    if (email.isNotEmpty() && firstname.isNotEmpty() && lastname.isNotEmpty() && role.isNotEmpty()) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    navController.navigate(Screen.AdminUserManagement.route)
                },
                title = {
                    Text(
                        "Enter Admin Password",
                        modifier = Modifier.semantics { testTag = "android:id/dialogTitle" }
                    )
                },
                text = {
                    Column {
                        Text(
                            "Creating account for:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "$firstname $lastname",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            email,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Role: ${role.removePrefix("Coop")}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Admin Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "android:id/passwordInputField" }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isButtonEnabled) {
                                isButtonEnabled = false // Prevent double-clicks
                                showDialog = false

                                // First sign out current user
                                FirebaseAuth.getInstance().signOut()

                                // Sign in with admin credentials
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail, adminPassword)
                                    .addOnSuccessListener {
                                        // Now proceed with account creation
                                        userViewModel.addAccount(
                                            email = email,
                                            firstname = firstname,
                                            lastname = lastname,
                                            password = placeholderPass,
                                            role = role,
                                            currentPass = adminPassword
                                        )

                                        transactionViewModel.recordTransaction(
                                            uid = it.user?.uid ?: "",
                                            transaction = TransactionData(
                                                transactionId = "Transaction-${UUID.randomUUID()}",
                                                type = "User_Added",
                                                date = formattedDateTime,
                                                description = "Added $firstname $lastname's account ($email) with role: ${role.removePrefix("Coop")}."
                                            )
                                        )
                                    }
                                    .addOnFailureListener { exception ->
                                        isButtonEnabled = true
                                        onRegisterEvent(Triple(ToastStatus.FAILED, "Authentication failed: ${exception.message}", System.currentTimeMillis()))
                                    }
                            }
                        },
                        modifier = Modifier.semantics { testTag = "android:id/confirmButton" },
                        enabled = adminPassword.isNotEmpty() && isButtonEnabled
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            navController.navigate(Screen.AdminUserManagement.route)
                        },
                        modifier = Modifier.semantics { testTag = "android:id/cancelButton" }
                    ) {
                        Text("Cancel")
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
            is UserState.LOADING -> {
                onRegisterEvent(Triple(ToastStatus.INFO, "Loading...", System.currentTimeMillis()))
            }
            is UserState.REGISTER_SUCCESS -> {
                sendEmail(email, subject, body)
                onRegisterEvent(Triple(ToastStatus.SUCCESSFUL, "Registration Successful", System.currentTimeMillis()))
                navController.navigate(Screen.AdminUserManagement.route)
            }
            is UserState.ERROR -> {
                isButtonEnabled = true // Re-enable the button on error
                onRegisterEvent(Triple(ToastStatus.FAILED, "Error: ${(userState as UserState.ERROR).message}", System.currentTimeMillis()))
            }
            else -> {}
        }
    }
}