package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

@Composable
fun SaveInfoDialog(onSave: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Save Information")
        },
        text = {
            Text(text = "Are you sure you want to save and update your information?")
        },
        confirmButton = {
            Button(
                onClick = { onSave() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/SaveInfoConfirmButton" }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/SaveInfoDismissButton" }
            ) {
                Text(text = "Cancel")
            }
        }
    )
}