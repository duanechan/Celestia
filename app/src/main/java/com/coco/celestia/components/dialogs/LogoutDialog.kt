package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag

@Composable
fun LogoutDialog(onDismiss: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Logging out")
        },
        text = {
            Text(text = "Do you want to log out?")
        },
        confirmButton = {
            Button(
                onClick = { onLogout() },
//                modifier = Modifier.semantics { testTag = "android:id/confirmLogout" }
                modifier = Modifier
                    .testTag("android:id/confirmLogout")
                    .semantics { contentDescription = "confirmLogoutCD" }
            ) {
                Text(text = "LogoutEyoo")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
//                modifier = Modifier.semantics { testTag = "android:id/cancelLogout" }
                modifier = Modifier
                    .testTag("android:id/cancelLogout")
                    .semantics { contentDescription = "cancelLogoutCD" }
            ) {
                Text(text = "Cancel")
            }
        }
    )
}