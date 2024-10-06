package com.coco.celestia.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButtons(
    userViewModel: UserViewModel,
    onClearSelect: () -> Unit
) {
    var showPopUpEdit by remember { mutableStateOf(false) }
    var showPopUpDelete by remember { mutableStateOf(false) }
    val selectedUsers by userViewModel.selectedUsers.observeAsState(emptyList())
    val roles = listOf("Coffee", "Meat")
    val user = selectedUsers[0]
    var updatedEmail by remember { mutableStateOf(user?.email ?: "") }
    var updatedRole by remember { mutableStateOf(user?.role ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val content = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircleButton(
            onClick = {
                if (selectedUsers.size < 2) {
                    showPopUpEdit = true
                }
            },
            icon = Icons.Default.Edit,
            enabled = selectedUsers.size < 2
        )
        CircleButton(onClick = {
            showPopUpDelete = true },
            icon = Icons.Default.Delete,
            enabled = true
        )
    }

    if (showPopUpEdit) {
        AlertDialog(
            onDismissRequest = { showPopUpEdit = false },
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
                        userViewModel.clearSelectedUsers() // Callback to clear selected users
                        user?.let {
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
                        showPopUpEdit = false
                    }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showPopUpEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showPopUpDelete) {
        AlertDialog(
            onDismissRequest = { showPopUpDelete = false },
            title = { Text(text = "Delete User?") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this user?",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        onClearSelect() // Callback to clear selected users
                        // TODO: Delete selected users
                        showPopUpDelete = false
                    }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showPopUpDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CircleButton(onClick: () -> Unit, icon: ImageVector, enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(DarkBlue)
            .alpha(if (!enabled) 0.5f else 1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}