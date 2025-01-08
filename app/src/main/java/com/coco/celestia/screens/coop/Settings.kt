package com.coco.celestia.screens.coop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.BuildConfig
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

// TODO: Contact Developer Team UI
// TODO: Configurations (admin side)

@Composable
fun Settings(navController: NavController, userRole: String) {
    val appVersion = BuildConfig.VERSION_NAME

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .padding(16.dp)
    ) {
        // User Profile
        Text(
            text = "User Profile",
            color = Color.Gray
        )
        SettingsItem(text = "My Profile", iconResId = R.drawable.profile) {
            navController.navigate("profile")
        }

        // Organization
        Text(
            text = "Organization",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray
        )
        SettingsItem(text = "Organization Profile", iconResId = R.drawable.organization) {
            navController.navigate(Screen.OrganizationProfile.route)
        }
        SettingsItem(text = "Access Control", iconResId = R.drawable.access_control) {
            navController.navigate(Screen.AccessControl.route)
        }

        // Configurations - Only shown for Admin role
        if (!userRole.startsWith("Coop", ignoreCase = true)) {
            Text(
                text = "Configurations",
                modifier = Modifier.padding(top = 16.dp),
                color = Color.Gray
            )
            SettingsItem(text = "Coop Admin", iconResId = R.drawable.admin) {
                navController.navigate("coop_admin")
            }
            SettingsItem(text = "Coop Facility", iconResId = R.drawable.facility) {
                navController.navigate("coop_facility")
            }
            SettingsItem(text = "Clients", iconResId = R.drawable.client) {
                navController.navigate("clients")
            }
            SettingsItem(text = "Members", iconResId = R.drawable.members) {
                navController.navigate("members")
            }
        }

        // Developer
        Text(
            text = "Developer",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray
        )
        SettingsItem(text = "Privacy Policy", iconResId = R.drawable.privacy) {
            navController.navigate(Screen.PrivacyPolicy.route)
        }
        SettingsItem(text = "Contact Developer Team", iconResId = R.drawable.phone) {
            navController.navigate("contact_developer")
        }

        // App Version
        Text(
            text = "App Version $appVersion",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SettingsItem(text: String, iconResId: Int, clickAction: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { clickAction() },
        colors = CardDefaults.cardColors(
            containerColor = Green4
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Green1)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun OrganizationProfileScreen() {
    var aboutUsText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(id = R.drawable.a),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 15.dp),
            colors = CardDefaults.cardColors(
                containerColor = White1
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("App Name", style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Coco: CoopConnects", style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("*Contact developers to edit this.", fontSize = 12.sp, color = Color.Red, style = TextStyle(fontFamily = mintsansFontFamily))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(start = 20.dp, end = 20.dp, top = 5.dp),
            colors = CardDefaults.cardColors(
                containerColor = White1
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("About Us", style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { isEditing = !isEditing }
                    ) {
                        Text(if (isEditing) "Save" else "Edit", style = TextStyle(fontFamily = mintsansFontFamily), color = Green1)
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = aboutUsText,
                        onValueChange = { aboutUsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("Enter organization description here...", style = TextStyle(fontFamily = mintsansFontFamily)) },
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White2,
                            unfocusedContainerColor = White2,
                            disabledContainerColor = White2,
                        )
                    )
                } else {
                    Text(
                        text = aboutUsText.ifBlank { "No description provided." },
                        style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AccessControlScreen(
    userViewModel: UserViewModel,
    facilityViewModel: FacilityViewModel,
    currentUserEmail: String,
    currentUserRole: String
) {
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val currentUserFacility = if (currentUserRole.startsWith("Coop", ignoreCase = true)) {
        facilitiesData.find { facility ->
            facility.emails.contains(currentUserEmail)
        }
    } else null

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
        facilityViewModel.fetchFacilities()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        Text(
            text = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                "System Administrators"
            } else {
                "People Who Can Access ${currentUserFacility?.name ?: "This Facility"}"
            },
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFamily = mintsansFontFamily)
        )

        when {
            facilityState == FacilityState.LOADING || userState == UserState.LOADING -> {
                Text(
                    text = "Loading...",
                    color = Color.Gray,
                    style = TextStyle(fontFamily = mintsansFontFamily)
                )
            }
            currentUserRole.startsWith("Coop") && currentUserFacility == null -> {
                Text(
                    text = "You don't have access to any facility",
                    color = Color.Gray,
                    style = TextStyle(fontFamily = mintsansFontFamily)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    val filteredUsers = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                        usersData.filter { user ->
                            user.role.equals("Admin", ignoreCase = true)
                        }
                    } else {
                        usersData.filter { user ->
                            user.role.startsWith("Coop", ignoreCase = true) &&
                                    currentUserFacility?.emails?.contains(user.email) == true
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        item {
                            Text(
                                text = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                                    "No administrators found"
                                } else {
                                    "No users found for this facility"
                                },
                                color = Color.Gray,
                                style = TextStyle(fontFamily = mintsansFontFamily)
                            )
                        }
                    } else {
                        items(filteredUsers) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = White1),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = user.email,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Green1,
                                        style = TextStyle(fontFamily = mintsansFontFamily)
                                    )

                                    Text(
                                        text = user.role,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        style = TextStyle(fontFamily = mintsansFontFamily),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}