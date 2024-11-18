package com.coco.celestia.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.sun.mail.imap.protocol.UID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUser(
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel,
    userData: UserData,
    onDismiss: () -> Unit
) {
    val roles = listOf("Coffee", "Meat")
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    var updatedEmail by remember { mutableStateOf(userData.email) }
    var updatedRole by remember { mutableStateOf(userData.role) }
    var expanded by remember { mutableStateOf(false) }
    val content = LocalContext.current

    if (updatedRole == "CoopCoffee") {
        updatedRole = "Coffee"
    } else if (updatedRole == "CoopMeat") {
        updatedRole = "Meat"
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Enter your input",
                modifier = Modifier.semantics { testTag = "android:id/EditUserDialogTitle" }
            )
        },
        text = {
            Column(
                modifier = Modifier.semantics { testTag = "android:id/EditUserForm" }
            ) {
                // Email Field
                OutlinedTextField(
                    value = updatedEmail,
                    onValueChange = { updatedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/EditUserEmailField" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.semantics { testTag = "android:id/EditUserRoleDropdownBox" }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = updatedRole,
                        onValueChange = {},
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .semantics { testTag = "android:id/EditUserRoleField" }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.semantics { testTag = "android:id/EditUserRoleMenu" }
                    ) {
                        roles.forEach { roleItem ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(roleItem) },
                                onClick = {
                                    updatedRole = roleItem
                                    expanded = false
                                },
                                modifier = Modifier.semantics { testTag = "android:id/EditUserRoleItem_$roleItem" }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Ensure data updates before refreshing the list
                    userData.let {
                        userViewModel.getUserUidByEmail(it.email) { uid ->
                            if (uid != null) {
                                // Convert role back to storage value
                                updatedRole = if (updatedRole == "Coffee") {
                                    "CoopCoffee"
                                } else {
                                    "CoopMeat"
                                }
                                // Perform the update
                                userViewModel.updateUser(
                                    uid,
                                    it.copy(
                                        email = updatedEmail,
                                        role = updatedRole
                                    )
                                )
                                if(it.email != updatedEmail) {
                                    transactionViewModel.recordTransaction(
                                        uid = FirebaseAuth.getInstance().uid.toString(),
                                        transaction = TransactionData(
                                            transactionId = "Transaction-${UUID.randomUUID()}",
                                            type = "User_Updated",
                                            date = formattedDateTime,
                                            description = "User ${it.firstname}'s email updated to $updatedEmail"
                                        )
                                    )
                                }
                                if(it.role != updatedRole) {
                                    transactionViewModel.recordTransaction(
                                        uid = FirebaseAuth.getInstance().uid.toString(),
                                        transaction = TransactionData(
                                            transactionId = "Transaction-${UUID.randomUUID()}",
                                            type = "User_Updated",
                                            date = formattedDateTime,
                                            description = "${it.firstname} ${it.lastname}'s role updated to $updatedRole"
                                        )
                                    )
                                }
                                // Reset the state after saving
                                updatedEmail = ""
                                updatedRole = roles.first()
                            } else {
                                Toast.makeText(content, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                    onDismiss()
                },
                modifier = Modifier.semantics { testTag = "android:id/EditUserConfirmButton" }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // Reset the form when dismissed
                    updatedEmail = ""
                    updatedRole = roles.first()
                    onDismiss()
                },
                modifier = Modifier.semantics { testTag = "android:id/EditUserDismissButton" }
            ) {
                Text("Cancel")
            }
        }
    )
}