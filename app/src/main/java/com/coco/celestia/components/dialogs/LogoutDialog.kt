package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
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
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/confirmLogout" }
            ) {
                Text(text = "Logout")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/cancelLogout" }
            ) {
                Text(text = "Cancel")
            }
        }
    )
}