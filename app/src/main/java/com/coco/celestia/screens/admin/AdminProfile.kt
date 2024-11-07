package com.coco.celestia.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.coco.celestia.components.dialogs.LogoutDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.components.dialogs.ExitDialog
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun AdminProfile(userViewModel: UserViewModel, navController: NavController) {
    var exitDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle back press to show the exit dialog or navigate to login
    BackHandler {
        exitDialog = true
    }

    if (exitDialog) {
        ExitDialog(
            onDismiss = { exitDialog = false },
            onExit = {
                // Navigate back to the login screen
                userViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (logoutDialog) {
        LogoutDialog(
            onDismiss = { logoutDialog = false },
            onLogout = {
                userViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                logoutDialog = false
            }
        )
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
            modifier = Modifier
                .size(24.dp)
                .semantics { testTag = "android:id/logoutIcon" }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Logout",
            modifier = Modifier.semantics { testTag = "android:id/logoutText" }
        )
    }
}
