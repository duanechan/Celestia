@file:OptIn(ExperimentalMaterial3Api::class)

package com.coco.celestia.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.LogoutDialog
import com.coco.celestia.components.dialogs.SaveInfoDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.BGGradientBrush
import com.coco.celestia.ui.theme.Apricot
import com.coco.celestia.ui.theme.BlueGradientBrush
import com.coco.celestia.ui.theme.CProfile
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.Cocoa
import com.coco.celestia.ui.theme.DOrangeCircle
import com.coco.celestia.ui.theme.DuskyBlue
import com.coco.celestia.ui.theme.FarmerGradientBrush
import com.coco.celestia.ui.theme.GrayGradientBrush
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green2
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.LightBlue
import com.coco.celestia.ui.theme.LightBlueGreen
import com.coco.celestia.ui.theme.OrangeGradientBrush
import com.coco.celestia.ui.theme.PaleBlue
import com.coco.celestia.ui.theme.PreparingStatus
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.PhoneValidator.isValidPhoneNumber
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.util.isValidPassword
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.PasswordState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Preview
@Composable
fun ProfilePagePreview() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState()
    userData?.let {
        val firstName = it.firstname
        val lastName = it.lastname
        val email = it.email
        val phoneNumber = it.phoneNumber
        val streetNumber = it.streetNumber
        val barangay = it.barangay
        val role = it.role

        CelestiaTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF2E3DB))
            ) {
                ProfileScreen(
                    navController,
                    userViewModel,
                    locationViewModel,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    streetNumber,
                    barangay,
                    role,
                    onLogoutEvent = {},
                    onProfileUpdateEvent = {}
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel,
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    streetNumber: String,
    barangay: String,
    role: String,
    onLogoutEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onProfileUpdateEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val context = LocalContext.current
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState()
    val passwordState by userViewModel.passwordState.observeAsState()
    val locationData by locationViewModel.locationData.observeAsState()
    var profilePicture by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var updatedFirstName by remember { mutableStateOf(firstName) }
    var updatedLastName by remember { mutableStateOf(lastName) }
    var updatedEmail by remember { mutableStateOf(email) }
    var updatedPhoneNumber by remember { mutableStateOf(phoneNumber) }
    var updatedStreetNumber by remember { mutableStateOf(streetNumber) }
    var updatedBarangay by remember { mutableStateOf(barangay) }
    var updatedProfilePicture by remember { mutableStateOf<Uri?>(null) }
    var saveInfoDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }
    val passwordInput = remember { mutableStateOf("") }
    val newPasswordInput = remember { mutableStateOf("") }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        updatedProfilePicture = it
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                onProfileUpdateEvent(
                    Triple(
                        ToastStatus.WARNING,
                        "Grant app access to update your profile picture.",
                        System.currentTimeMillis()
                    )
                )
            }
        }
    )

    fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when (checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                galleryLauncher.launch("image/*")
            }

            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    LaunchedEffect(userState) {
        userViewModel.fetchUser(uid)
        locationViewModel.fetchLocations("")
        ImageService.fetchProfilePicture(uid) {
            profilePicture = it
        }
    }

    if (logoutDialog) {
        LogoutDialog(
            onDismiss = { logoutDialog = false },
            onLogout = {
                userViewModel.logout(uid)
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
                onLogoutEvent(Triple(ToastStatus.SUCCESSFUL, "Logged out.", System.currentTimeMillis()))
                logoutDialog = false
            }, role
        )
    }

    if (saveInfoDialog) {
        SaveInfoDialog(
            onSave = {
                userData?.let {
                    userViewModel.updateUser(
                        uid,
                        it.copy(
                            email = updatedEmail,
                            firstname = updatedFirstName,
                            lastname = updatedLastName,
                            phoneNumber = updatedPhoneNumber,
                            streetNumber = updatedStreetNumber,
                            barangay = updatedBarangay
                        )
                    )
                }
                onProfileUpdateEvent(
                    Triple(
                        ToastStatus.SUCCESSFUL,
                        "Profile updated successfully!",
                        System.currentTimeMillis()
                    )
                )
                updatedProfilePicture?.let {
                    ImageService.uploadProfilePicture(uid, it) { status ->
                        if (status) {
                            Log.d("ProfileScreen", "Profile picture uploaded successfully!")
                        } else {
                            Log.d("ProfileScreen", "Profile picture upload failed!")
                        }
                    }
                }
                saveInfoDialog = false
            },
            onDismiss = { saveInfoDialog = false }
        )
    }

    fun getFirstColorForRole(role: String): Color {
        return Green4
    }

    fun saveInfoColor(role: String): Color {
        return Green4
    }

    fun getIconColorForRole(role: String): Color {
        return Green4
    }

    fun getProfileColorForRole(role: String): Color {
        return Green4
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        Green4
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = updatedProfilePicture ?: profilePicture
                            ?: R.drawable.profile_icon
                        ),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White)
                    )
                    FloatingActionButton(
                        onClick = { openGallery() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(35.dp)
                            .offset(y = (-30).dp, x = 25.dp),
                        contentColor = Color.White.copy(0.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            tint = getIconColorForRole(role)
                        )
                    }

                    Text(
                        text = "$firstName $lastName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                    Text(
                        text = role,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        fontFamily = mintsansFontFamily,
                        color = Green1
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(
                        color = getProfileColorForRole(role),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileField(
                        value = updatedFirstName,
                        onValueChange = { updatedFirstName = it },
                        label = "First Name",
                        keyboardType = KeyboardType.Text,
                        tag = "android:id/updateFirstName"
                    )

                    ProfileField(
                        value = updatedLastName,
                        onValueChange = { updatedLastName = it },
                        label = "Last Name",
                        keyboardType = KeyboardType.Text,
                        tag = "android:id/updateLastName"
                    )

                    ProfileField(
                        value = updatedEmail,
                        onValueChange = { updatedEmail = it },
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        tag = "android:id/updateEmailField"
                    )

                    ProfileField(
                        value = updatedPhoneNumber,
                        prefix = { Text(text = "+63") },
                        onValueChange = {
                            if (it.length <= 10) {
                                updatedPhoneNumber = it
                            }
                        },
                        label = "Phone Number",
                        keyboardType = KeyboardType.Phone,
                        tag = "android:id/updatePhoneNumber",
                    )

                    ProfileField(
                        value = updatedStreetNumber,
                        onValueChange = { updatedStreetNumber = it },
                        label = "Street No.",
                        keyboardType = KeyboardType.Text,
                        tag = "android:id/updateStreetNumber"
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = updatedBarangay,
                            onValueChange = { updatedBarangay = it },
                            label = { Text("Barangay") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 8.dp)
                                .semantics { testTag = "android:id/updateBarangay" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            locationData?.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(text = location.barangay) },
                                    onClick = {
                                        updatedBarangay = location.barangay
                                        expanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            testTag =
                                                "android:id/barangayDropdownItem_${location.barangay}"
                                        }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            showPasswordDialog = true
                        },
                        modifier = Modifier
                            .align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = saveInfoColor(role = role),
                            contentColor = Green1
                        )
                    ) {
                        Text("Change Password")
                    }

                    if (showPasswordDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showPasswordDialog = false
                                passwordInput.value = ""
                                newPasswordInput.value = ""
                            },
                            title = { Text("Change Password", modifier = Modifier.semantics { testTag = "android:id/dialogTitle" }) },
                            text = {
                                var isValidPassword by remember { mutableStateOf(listOf("Password is valid!")) }

                                Column {
                                    TextField(
                                        value = passwordInput.value,
                                        onValueChange = { passwordInput.value = it },
                                        label = { Text("Password") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.semantics { testTag = "android:id/passwordInputField" }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = newPasswordInput.value,
                                        onValueChange = {
                                            newPasswordInput.value = it
                                            isValidPassword = isValidPassword(it)
                                        },
                                        label = { Text("New Password") },
                                        isError = "Password is valid!" !in isValidPassword,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.semantics { testTag = "android:id/passwordInputField" }
                                    )

                                    if ("Password is valid!" !in isValidPassword) {
                                        Column {
                                            isValidPassword.forEach { error ->
                                                Text(
                                                    text = error,
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                var isButtonEnabled by remember { mutableStateOf(true) }

                                Button(
                                    onClick = {
                                        if (isButtonEnabled) {
                                            isButtonEnabled = false
                                            userViewModel.changePassword(passwordInput.value, newPasswordInput.value)
                                            passwordInput.value = ""
                                            newPasswordInput.value = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .semantics { testTag = "android:id/confirmButton" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green2,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        showPasswordDialog = false
                                    },
                                    modifier = Modifier.semantics { testTag = "android:id/cancelButton" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Gray,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    LaunchedEffect(passwordState) {
                        when (passwordState) {
                            is PasswordState.SUCCESS -> {
                                onProfileUpdateEvent(
                                    Triple(
                                        ToastStatus.SUCCESSFUL,
                                        "Password Changed Successfully!",
                                        System.currentTimeMillis()
                                    )
                                )
                                showPasswordDialog = false
                            }
                            is PasswordState.ERROR -> {
                                onProfileUpdateEvent(
                                    Triple(
                                        ToastStatus.WARNING,
                                        (passwordState as PasswordState.ERROR).message,
                                        System.currentTimeMillis()
                                    )
                                )
                                showPasswordDialog = false
                            } else -> {}
                        }
                        userViewModel.resetPasswordState()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { saveInfoDialog = true },
                    enabled = (updatedEmail != email ||
                            updatedFirstName != firstName ||
                            updatedLastName != lastName ||
                            updatedPhoneNumber != phoneNumber ||
                            updatedStreetNumber != streetNumber ||
                            updatedBarangay != barangay ||
                            (updatedProfilePicture != profilePicture && updatedProfilePicture != null)) &&
                            updatedEmail.isNotEmpty() &&
                            updatedFirstName.isNotEmpty() &&
                            updatedLastName.isNotEmpty() &&
                            updatedPhoneNumber.isNotEmpty() &&
                            updatedStreetNumber.isNotEmpty() &&
                            isValidPhoneNumber(updatedPhoneNumber) &&
                            isValidEmail(updatedEmail),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = saveInfoColor(role),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .semantics { testTag = "android:id/saveButton" }
                ) {
                    Text(text = "Save")
                }
                Button(
                    onClick = { logoutDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getFirstColorForRole(role),
                        contentColor = Green1
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .semantics { testTag = "android:id/logoutButton" }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    value: String,
    prefix: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        prefix = prefix,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .semantics { testTag = tag },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
        )
    )
}

@Composable
fun Profile(
    navController: NavController,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel,
    onLogoutEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onProfileUpdateEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val userData by userViewModel.userData.observeAsState()

    userData?.let {
        val firstName = it.firstname
        val lastName = it.lastname
        val email = it.email
        val phoneNumber = it.phoneNumber
        val streetNumber = it.streetNumber
        val barangay = it.barangay
        val role = it.role

        ProfileScreen(
            navController,
            userViewModel,
            locationViewModel,
            firstName,
            lastName,
            email,
            phoneNumber,
            streetNumber,
            barangay,
            role,
            onLogoutEvent = { event -> onLogoutEvent(event) },
            onProfileUpdateEvent = { event -> onProfileUpdateEvent(event) }
        )
    }
}