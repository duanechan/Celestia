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
import androidx.compose.ui.text.font.FontWeight
import com.coco.celestia.ui.theme.mintsansFontFamily

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LogoutDialog(onDismiss: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Logout", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
        },
        text = {
            Text(text = "Do you want to log out?", fontFamily = mintsansFontFamily)
        },
        confirmButton = {
            Button(
                onClick = { onLogout() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/confirmLogout" }
            ) {
                Text(text = "Logout", fontFamily = mintsansFontFamily)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/cancelLogout" }
            ) {
                Text(text = "Cancel", fontFamily = mintsansFontFamily)
            }
        },
        modifier = Modifier.semantics { testTag = "android:id/LogoutDialog" }
    )
}