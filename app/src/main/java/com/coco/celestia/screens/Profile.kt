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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.LogoutDialog
import com.coco.celestia.components.dialogs.SaveInfoDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.EditDetailsBg
import com.coco.celestia.ui.theme.GreenGradientBrush
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.viewmodel.LocationViewModel
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
                    onLogoutEvent = {},
                    onProfileUpdateEvent = {}
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
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
    onLogoutEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    onProfileUpdateEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState()
    val locationData by locationViewModel.locationData.observeAsState()
    var expanded by remember { mutableStateOf(false) }
    var updatedEmail by remember { mutableStateOf(email) }
    var updatedPhoneNumber by remember { mutableStateOf(phoneNumber) }
    var updatedStreetNumber by remember { mutableStateOf(streetNumber) }
    var updatedBarangay by remember { mutableStateOf(barangay) }
    var saveButtonEnabled by remember { mutableStateOf(false) }
    var saveInfoDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userState) {
        userViewModel.fetchUser(uid)
        locationViewModel.fetchLocations("")
    }

    if (logoutDialog) {
        LogoutDialog(
            onDismiss = { logoutDialog = false },
            onLogout = {
                userViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
                onLogoutEvent(Triple(ToastStatus.INFO, "Logged out.", System.currentTimeMillis()))
                logoutDialog = false
            }
        )
    }

    saveButtonEnabled =
        (updatedEmail != email ||
                updatedPhoneNumber != phoneNumber ||
                updatedStreetNumber != streetNumber ||
                updatedBarangay != barangay) &&
                updatedEmail.isNotEmpty() &&
                updatedPhoneNumber.isNotEmpty() &&
                updatedStreetNumber.isNotEmpty() &&
                isValidEmail(updatedEmail)

    if (saveInfoDialog) {
        SaveInfoDialog(
            onSave = {
                userData?.let {
                    userViewModel.updateUser(
                        uid,
                        it.copy(
                            email = updatedEmail,
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
                saveInfoDialog = false
            },
            onDismiss = { saveInfoDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 80.dp),
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = GreenGradientBrush,
                    shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(100.dp)
                )

                Text(
                    text = "$firstName $lastName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                )
                //TODO: Add role below name
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = EditDetailsBg,
                    shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
                )
                .padding(16.dp)
        ) {
            Column {

                OutlinedTextField(
                    value = updatedEmail,
                    onValueChange = {
                        updatedEmail = it
                    },
                    label = { Text(text = "Email") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.semantics { testTag = "android:id/updateEmailField" }
                )
                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = updatedPhoneNumber,
                    onValueChange = { updatedPhoneNumber = it },
                    label = { Text(text = "Phone Number") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.semantics { testTag = "android:id/updatePhoneNumber" }
                )
                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = updatedStreetNumber,
                    onValueChange = { updatedStreetNumber = it },
                    label = { Text(text = "Street No.") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.semantics { testTag = "android:id/updateStreetNumber" }
                )
                Spacer(modifier = Modifier.height(8.dp))


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = updatedBarangay,
                        onValueChange = { updatedBarangay = it },
                        label = { Text("Barangay") },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .semantics { testTag = "android:id/updateBarangay" }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        locationData?.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(text = location.barangay) },
                                onClick = {
                                    updatedBarangay = location.barangay
                                    expanded = false
                                },
                                modifier = Modifier.semantics {
                                    testTag = "android:id/barangayDropdownItem_${location.barangay}"
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center){
            Button(
                onClick = { saveInfoDialog = true },
                enabled = saveButtonEnabled,
                modifier = Modifier.semantics { testTag = "android:id/saveButton" }
            ) {
                Text(text = "Save")
            }
            Button(
                onClick = { logoutDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.DarkGray
                ),
                modifier = Modifier
                    .padding(8.dp)
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
            onLogoutEvent = { event -> onLogoutEvent(event) },
            onProfileUpdateEvent = { event -> onProfileUpdateEvent(event) }
        )
    }
}

