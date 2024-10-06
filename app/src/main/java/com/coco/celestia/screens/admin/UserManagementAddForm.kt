package com.coco.celestia.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.mintsansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserForm(
    navController: NavController,
    email: String,
    role: String,
    newEmail: (String) -> Unit,
    newRole: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Coffee", "Meat")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = { navController.navigate(Screen.AdminUserManagement.route) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Add User ✚",
                fontFamily = mintsansFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = newEmail,
            label = { Text("Email", fontFamily = mintsansFontFamily) },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                readOnly = true,
                value = role,
                onValueChange = {},
                placeholder = { Text("Select Role", fontFamily = mintsansFontFamily) },
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
                            newRole(roleItem)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}