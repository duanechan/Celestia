package com.coco.celestia.screens.coop.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUser(
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel,
    facilityViewModel: FacilityViewModel,
    userData: UserData,
    onDismiss: () -> Unit
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    var updatedEmail by remember { mutableStateOf(userData.email) }
    var updatedRole by remember { mutableStateOf(userData.role) }
    var expanded by remember { mutableStateOf(false) }
    val content = LocalContext.current
    var isUpdating by remember { mutableStateOf(false) }

    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(initial = FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Edit User",
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

                // Facility/Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.semantics { testTag = "android:id/EditUserRoleDropdownBox" }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = updatedRole.removePrefix("Coop"),
                        onValueChange = {},
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .semantics { testTag = "android:id/EditUserRoleField" }
                    )

                    when (facilityState) {
                        is FacilityState.LOADING -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is FacilityState.SUCCESS -> {
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.semantics { testTag = "android:id/EditUserRoleMenu" }
                            ) {
                                facilitiesData.forEach { facility ->
                                    DropdownMenuItem(
                                        text = { Text(facility.name) },
                                        onClick = {
                                            updatedRole = "Coop${facility.name}"
                                            expanded = false
                                        },
                                        modifier = Modifier.semantics {
                                            testTag = "android:id/EditUserRoleItem_${facility.name}"
                                        }
                                    )
                                }
                            }
                        }
                        is FacilityState.EMPTY -> {
                            Text(
                                text = "No facilities available",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is FacilityState.ERROR -> {
                            Text(
                                text = (facilityState as FacilityState.ERROR).message,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isUpdating = true
                    userData.let { user ->
                        userViewModel.getUserUidByEmail(user.email) { uid ->
                            if (uid != null) {
                                // Update facility emails first
                                facilityViewModel.updateFacilityEmails(
                                    oldRole = user.role,
                                    newRole = updatedRole,
                                    userEmail = updatedEmail,
                                    onSuccess = {
                                        // Then update user data
                                        userViewModel.updateUser(
                                            uid,
                                            user.copy(
                                                email = updatedEmail,
                                                role = updatedRole
                                            )
                                        )

                                        // Record transactions
                                        if (user.email != updatedEmail) {
                                            transactionViewModel.recordTransaction(
                                                uid = FirebaseAuth.getInstance().uid.toString(),
                                                transaction = TransactionData(
                                                    transactionId = "Transaction-${UUID.randomUUID()}",
                                                    type = "User_Updated",
                                                    date = formattedDateTime,
                                                    description = "User ${user.firstname}'s email updated to $updatedEmail"
                                                )
                                            )
                                        }
                                        if (user.role != updatedRole) {
                                            transactionViewModel.recordTransaction(
                                                uid = FirebaseAuth.getInstance().uid.toString(),
                                                transaction = TransactionData(
                                                    transactionId = "Transaction-${UUID.randomUUID()}",
                                                    type = "User_Updated",
                                                    date = formattedDateTime,
                                                    description = "${user.firstname} ${user.lastname}'s facility updated to ${updatedRole.removePrefix("Coop")}"
                                                )
                                            )
                                        }
                                        isUpdating = false
                                        onDismiss()
                                    },
                                    onError = { error ->
                                        isUpdating = false
                                        Toast.makeText(content, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                isUpdating = false
                                Toast.makeText(content, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isUpdating,
                modifier = Modifier.semantics { testTag = "android:id/EditUserConfirmButton" }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                enabled = !isUpdating,
                modifier = Modifier.semantics { testTag = "android:id/EditUserDismissButton" }
            ) {
                Text("Cancel")
            }
        }
    )
}