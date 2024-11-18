package com.coco.celestia.screens.farmer.dialogs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.Apricot
import com.coco.celestia.ui.theme.Cinnabar
import com.coco.celestia.ui.theme.Cocoa
import com.coco.celestia.ui.theme.OliveGreen

@Composable
fun DeleteItemDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Remove $productName", color = Cocoa, fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { testTag = "android:id/deleteItemTitle" }
            )
        },
        text = {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("Are you sure you want to remove ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Cinnabar)) {
                            append(productName)
                        }
                        append(" from your inventory completely?")
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic, fontSize = 13.sp)) {
                            append("This action cannot be undone.")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() },
                modifier = Modifier.semantics { testTag = "android:id/deleteItemConfirmButton" }
            ) {
                Text(
                    text = "Delete",
                    color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/deleteItemConfirmButtonText" }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cocoa),
            ) {
                Text(
                    text = "Cancel",
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityDismissButtonText" })
            }
        }
    )
}