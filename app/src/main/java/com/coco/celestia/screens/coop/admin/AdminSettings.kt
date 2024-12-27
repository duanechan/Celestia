package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun AdminSettings(navController: NavController) {
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

        // Configurations
        Text(
            text = "Configurations",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray
        )
        SettingsItem(text = "Coop Admin", iconResId = R.drawable.admin) {
            navController.navigate("coop_admin") // to change
        }
        SettingsItem(text = "Coop Facility", iconResId = R.drawable.facility) {
            navController.navigate("coop_facility") // to change
        }
        SettingsItem(text = "Clients", iconResId = R.drawable.client) {
            navController.navigate("clients") // to change
        }
        SettingsItem(text = "Members", iconResId = R.drawable.members) {
            navController.navigate("members") // to change
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
            navController.navigate("contact_developer") // to change
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
fun AccessControlScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .padding(16.dp)
    ) {
        Text(
            text = "People Who can access Coop Admin",
            color = Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFamily = mintsansFontFamily)
        )
    }
}