package com.coco.celestia.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    onRegisterEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val maxChar = 25
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var privacyPolicyRead by remember { mutableStateOf(false) }
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = privacyPolicyRead,
                        onCheckedChange = { privacyPolicyRead = !privacyPolicyRead },
                        colors = CheckboxDefaults.colors(checkedColor = BrownCoffee),
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("I have read and agree to the ")
                            withStyle(
                                style = SpanStyle(
                                    color = BrownCoffee,
                                    textDecoration = TextDecoration.Underline)
                            ) {
                                append("Privacy Policy")
                            }
                            append(".")
                        },
                        modifier = Modifier.clickable { showPrivacyPolicy = true },
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = {
                        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                            onRegisterEvent(Triple(ToastStatus.WARNING, "All text must be filled", System.currentTimeMillis()))
                        } else if(password != confirmPassword) {
                            onRegisterEvent(Triple(ToastStatus.WARNING, "Password does not match.", System.currentTimeMillis()))
                        } else if (!privacyPolicyRead) {
                            onRegisterEvent(Triple(ToastStatus.WARNING, "Please read and check the privacy policy first.", System.currentTimeMillis()))
                        } else {
                            Log.d("Registration", selectedRole)
                            userViewModel.register(email, firstName, lastName, password, selectedRole)
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
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text(text = "Privacy Policy", color = BrownCoffee, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily) },
            text = {
                PrivacyPolicy()

            },
            confirmButton = {},
            dismissButton = {
                ClickableText(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = BrownCoffee)) {
                            append("I understand")
                        }
                    },
                    onClick = { showPrivacyPolicy = false }
                )
            }
        )
    }
}

@Composable
fun PrivacyPolicy() {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Text(
                text = "Welcome to CoCo: CoopConnects. We are committed to protecting your personal data. " +
                        "This Privacy Policy explains how we collect, use, share, and protect the information " +
                        "you provide in compliance with the Data Privacy Act of 2012 in the Philippines.",
                textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
            )
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "Information We Collect", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text =  "When you use CoCo: CoopConnects, we may collect the following types of personal data:\n" +
                            "+ Personal Information: This includes your email address, first name, last name, address, and phone number, which help us manage your account and provide relevant services.\n" +
                            "+ Account Security Information: Your password, stored securely using encryption.",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "How We Use Your Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "We use your data to:\n" +
                            "+ Set Up and Manage Your Account: Ensuring smooth access to the app.\n" +
                            "+ Communicate with You: Sending updates, notifications, and responding to inquiries.\n" +
                            "+ Ensure Security and Compliance: Protecting accounts and fulfilling legal requirements.",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "Data Sharing and Disclosure", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "Your data will not be shared with third parties, except in the following situations:\n" +
                            "+ With Your Consent: If you provide explicit permission, we may share data with certain third parties.\n" +
                            "+ Legal Obligations: We may disclose data to comply with legal requirements, such as court orders or regulatory requests.\n" +
                            "+ Service Providers: We may share data with trusted partners who assist us in providing and improving our services. These include:\n" +
                            "Firebase (for data storage and authentication)\n" +
                            "These third parties operate under strict confidentiality and security agreements to ensure the protection of your data.\n",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "Data Storage, Retention, and Security", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "We store your data securely with Firebase, a trusted, industry-standard" +
                            " platform provided by Google. Firebase employs encryption " +
                            "and strict security measures to protect your information, " +
                            "and we implement Firebase security rules to manage access " +
                            "and ensure data privacy.\n",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "Your Rights", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "Under the Data Privacy Act, you have the right to:\n" +
                            "+ Be Informed: Know how your data is used.\n" +
                            "+ Access: Request a copy of your personal data.\n" +
                            "+ Rectification: Update or correct your data.\n" +
                            "+ Erasure: Request deletion of your data when it’s no longer needed.\n" +
                            "+ Object: Withdraw consent for certain types of processing.\n" +
                            "To exercise these rights, contact us at [to add].",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column {
                Text(text = "Changes to This Privacy Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "We may update this policy as necessary. We will notify you of " +
                            "significant changes within the app or via email.\n",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
            }
        }
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contact Us", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
                Text(
                    text = "If you have questions about this Privacy Policy or your personal" +
                            " data, please reach out to us using the information below:\n" +
                            "\n",
                    textAlign = TextAlign.Justify, fontFamily = mintsansFontFamily
                )
                Row {
                    Text(
                        text = "Cooperative’s Email:\n[e.g: support@coopconnects.com]\n", fontFamily = mintsansFontFamily
                    )
                }
                Row {
                    Text(
                        text = "Cooperative’s Phone:\n[Insert Phone Number]\n", fontFamily = mintsansFontFamily
                    )
                }
                Row {
                    Text(
                        text = "Developer’s Email:\ncocobycelestia@gmail.com", fontFamily = mintsansFontFamily
                    )
                }
            }
        }

    }
}