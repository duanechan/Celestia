package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import com.coco.celestia.ui.theme.mintsansFontFamily

@Composable
fun ExitDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Exiting", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily)
        },
        text = {
            Text(text = "Are you sure you want to leave the app?", fontFamily = mintsansFontFamily)
        },
        confirmButton = {
            Button(
                onClick = { onExit() },
                modifier = Modifier.semantics { testTag = "android:id/exitDialogConfirmButton" }
            ) {
                Text(text = "Exit", fontFamily = mintsansFontFamily)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.semantics { testTag = "android:id/exitDialogDismissButton" }
            ) {
                Text(text = "Cancel", fontFamily = mintsansFontFamily)
            }
        },
        modifier = Modifier.semantics { testTag = "android:id/ExitDialog" }
    )
}