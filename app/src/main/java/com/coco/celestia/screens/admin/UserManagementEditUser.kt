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
import androidx.compose.ui.unit.dp
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUser(
    userViewModel: UserViewModel,
    userData: UserData,
    onDismiss: () -> Unit
) {
    val roles = listOf("Coffee", "Meat")
    var updatedEmail by remember { mutableStateOf(userData.email) }
    var updatedRole by remember { mutableStateOf(userData.role) }
    var expanded by remember { mutableStateOf(false) }
    val content = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Enter your input") },
        text = {
            Column {
                //to be changed
                OutlinedTextField(
                    value = updatedEmail,
                    onValueChange = { updatedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
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
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { roleItem ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(roleItem) },
                                onClick = {
                                    updatedRole = if (roleItem == "Coffee") {
                                        "CoopCoffee"
                                    } else {
                                        "CoopMeat"
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    userData.let {
                        userViewModel.getUserUidByEmail(it.email) { uid ->
                            if (uid != null) {
                                userViewModel.updateUser(
                                    uid,
                                    it.copy(
                                        email = updatedEmail,
                                        role = updatedRole
                                    )
                                )
                            } else {
                                Toast.makeText(content, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    onDismiss()
                }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = {onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
