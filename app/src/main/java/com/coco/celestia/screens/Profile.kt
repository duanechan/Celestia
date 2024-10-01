package com.coco.celestia.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.CelestiaTheme
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
                    barangay
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
    barangay: String
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
                Toast.makeText(navController.context, "Logout", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(navController.context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                saveInfoDialog = false
            },
            onDismiss = { saveInfoDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 80.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_icon),
            contentDescription = "Profile Icon",
            modifier = Modifier.size(100.dp)
        )
        Text(text = "$firstName $lastName", fontWeight = FontWeight.Bold, fontSize = 25.sp)

        OutlinedTextField(
            value = updatedEmail,
            onValueChange = {
                updatedEmail = it
            },
            label = { Text(text = "Email") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = updatedPhoneNumber,
            onValueChange = { updatedPhoneNumber = it },
            label = { Text(text = "Phone Number") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        OutlinedTextField(
            value = updatedStreetNumber,
            onValueChange = { updatedStreetNumber = it },
            label = { Text(text = "Street No.") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = updatedBarangay,
                onValueChange = { updatedBarangay = it },
                label = { Text("Barangay") },
                readOnly = true,
                modifier = Modifier.menuAnchor()
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
                        }
                    )
                }
            }
        }
        Button(
            onClick = { saveInfoDialog = true },
            enabled = saveButtonEnabled,
        ) {
            Text(text = "Save")
        }
        Button(
            onClick = { logoutDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun Profile(
    navController: NavController,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel
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
            barangay
        )
    }
}

