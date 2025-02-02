package com.coco.celestia.screens.coop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.coco.celestia.BuildConfig
import com.coco.celestia.R
import com.coco.celestia.screens.coop.facility.ErrorScreen
import com.coco.celestia.screens.coop.facility.LoadingScreen
import com.coco.celestia.screens.coop.facility.NoFacilityScreen
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ContactState
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ContactData
import com.coco.celestia.viewmodel.model.FacilityData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Settings(navController: NavController, userRole: String) {
    val appVersion = BuildConfig.VERSION_NAME

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .padding(16.dp)
    ) {
        Text(
            text = "User Profile",
            color = Color.Gray
        )
        SettingsItem(text = "My Profile", iconResId = R.drawable.profile) {
            navController.navigate("profile")
        }

        Text(
            text = "Organization",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray
        )
        SettingsItem(text = "Organization Profile", iconResId = R.drawable.organization) {
            navController.navigate(Screen.OrganizationProfile.route)
        }
        SettingsItem(text = "Access Control", iconResId = R.drawable.access_control) {
            navController.navigate(Screen.AccessControl.route)
        }

        if (userRole.equals("Admin", ignoreCase = true)) {
            Text(
                text = "Configurations",
                modifier = Modifier.padding(top = 16.dp),
                color = Color.Gray
            )
            SettingsItem(text = "Manage Facilities", iconResId = R.drawable.facility) {
                navController.navigate(Screen.ManageFacilities.route)
            }
        } else if (userRole.startsWith("Coop", ignoreCase = true)) {
            Text(
                text = "Configurations",
                modifier = Modifier.padding(top = 16.dp),
                color = Color.Gray
            )
            SettingsItem(text = "Collection & Payment Settings", iconResId = R.drawable.productlist) {
                navController.navigate(Screen.FacilitySettings.route)
            }
        }

        Text(
            text = "Developer",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray
        )
        SettingsItem(text = "Privacy Policy", iconResId = R.drawable.privacy) {
            navController.navigate(Screen.PrivacyPolicy.route)
        }
        SettingsItem(text = "Contact Developer Team", iconResId = R.drawable.phone) {
            navController.navigate(Screen.ContactDeveloper.route)
        }

        Text(
            text = "App Version $appVersion",
            modifier = Modifier.padding(top = 16.dp),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SettingsItem(text: String, iconResId: Int, clickAction: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { clickAction() },
        colors = CardDefaults.cardColors(
            containerColor = Green4
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Green1)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun FacilitySettingsScreen(
    facilityViewModel: FacilityViewModel,
    currentUserEmail: String,
    currentUserRole: String,
    navController: NavController
) {
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        when (facilityState) {
            is FacilityState.LOADING -> {
                LoadingScreen("Loading facilities...")
            }
            is FacilityState.ERROR -> {
                ErrorScreen((facilityState as FacilityState.ERROR).message)
            }
            else -> {
                val userFacilities = if (currentUserRole.startsWith("Admin", ignoreCase = true)) {
                    facilitiesData
                } else {
                    facilitiesData.filter { facility ->
                        facility.emails.contains(currentUserEmail)
                    }
                }

                if (userFacilities.isEmpty()) {
                    NoFacilityScreen()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(userFacilities) { facility ->
                                FacilityConfigurationSettings(
                                    facility = facility,
                                    facilityViewModel = facilityViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityConfigurationSettings(
    facility: FacilityData,
    facilityViewModel: FacilityViewModel,
    navController: NavController
) {
    var isPickupEnabled by remember(facility) {
        mutableStateOf(facility.pickupLocation.isNotBlank())
    }
    var isDeliveryEnabled by remember(facility) {
        mutableStateOf(facility.deliveryDetails.isNotBlank())
    }
    var isCashEnabled by remember(facility) {
        mutableStateOf(facility.cashInstructions.isNotBlank())
    }
    var isGcashEnabled by remember(facility) {
        mutableStateOf(facility.gcashNumbers.isNotBlank())
    }

    var pickupLocation by remember(facility) {
        mutableStateOf(facility.pickupLocation)
    }
    var deliveryDetails by remember(facility) {
        mutableStateOf(facility.deliveryDetails)
    }
    var cashInstructions by remember(facility) {
        mutableStateOf(facility.cashInstructions)
    }
    var gcashNumbers by remember(facility) {
        mutableStateOf(facility.gcashNumbers)
    }

    fun disableSetting(
        onDisable: () -> Unit,
        onClear: () -> Unit
    ) {
        onDisable()
        onClear()
        facilityViewModel.updateFacilitySettings(
            facilityName = facility.name,
            pickupLocation = if (isPickupEnabled) pickupLocation else "",
            deliveryDetails = if (isDeliveryEnabled) deliveryDetails else "",
            cashInstructions = if (isCashEnabled) cashInstructions else "",
            gcashNumbers = if (isGcashEnabled) gcashNumbers else "",
            onSuccess = { /* Handle success */ },
            onError = { /* Handle error */ }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = facility.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Collection Methods Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Collection Methods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // Pickup
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pick Up")
                        Switch(
                            checked = isPickupEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    disableSetting(
                                        onDisable = { isPickupEnabled = false },
                                        onClear = { pickupLocation = "" }
                                    )
                                } else {
                                    isPickupEnabled = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Green1,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    AnimatedVisibility(visible = isPickupEnabled) {
                        OutlinedTextField(
                            value = pickupLocation,
                            onValueChange = { pickupLocation = it },
                            label = { Text("Pick Up Location") },
                            placeholder = { Text("Enter pick up location here") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .semantics { testTag = "android:id/PickUpLocation" }
                        )
                    }
                }

                // Delivery
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivery")
                        Switch(
                            checked = isDeliveryEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    disableSetting(
                                        onDisable = { isDeliveryEnabled = false },
                                        onClear = { deliveryDetails = "" }
                                    )
                                } else {
                                    isDeliveryEnabled = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Green1,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    AnimatedVisibility(visible = isDeliveryEnabled) {
                        OutlinedTextField(
                            value = deliveryDetails,
                            onValueChange = { deliveryDetails = it },
                            label = { Text("Delivery Details") },
                            placeholder = { Text("Enter delivery/courier used") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .semantics { testTag = "android:id/DeliveryDetails" }
                        )
                    }
                }
            }

            // Payment Methods Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Payment Methods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // Cash
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cash Payment")
                        Switch(
                            checked = isCashEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    disableSetting(
                                        onDisable = { isCashEnabled = false },
                                        onClear = { cashInstructions = "" }
                                    )
                                } else {
                                    isCashEnabled = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Green1,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    AnimatedVisibility(visible = isCashEnabled) {
                        OutlinedTextField(
                            value = cashInstructions,
                            onValueChange = { cashInstructions = it },
                            label = { Text("Cash Instructions") },
                            placeholder = { Text("Enter instructions for cash payments") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .semantics { testTag = "android:id/CashDetails" }
                        )
                    }
                }

                // GCash
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GCash Payment")
                        Switch(
                            checked = isGcashEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    disableSetting(
                                        onDisable = { isGcashEnabled = false },
                                        onClear = { gcashNumbers = "" }
                                    )
                                } else {
                                    isGcashEnabled = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Green1,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    AnimatedVisibility(visible = isGcashEnabled) {
                        OutlinedTextField(
                            value = gcashNumbers,
                            onValueChange = { gcashNumbers = it },
                            label = { Text("GCash Numbers") },
                            placeholder = { Text("Enter GCash number(s)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .semantics { testTag = "android:id/GCashNumber" }
                        )
                    }
                }
            }

            if (isPickupEnabled || isDeliveryEnabled || isCashEnabled || isGcashEnabled) {
                Button(
                    onClick = {
                        facilityViewModel.updateFacilitySettings(
                            facilityName = facility.name,
                            pickupLocation = if (isPickupEnabled) pickupLocation else "",
                            deliveryDetails = if (isDeliveryEnabled) deliveryDetails else "",
                            cashInstructions = if (isCashEnabled) cashInstructions else "",
                            gcashNumbers = if (isGcashEnabled) gcashNumbers else "",
                            onSuccess = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(Screen.Settings.route)
                                }
                            },
                            onError = { /* Handle error */ }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green1,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun OrganizationProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(id = R.drawable.a),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 15.dp),
            colors = CardDefaults.cardColors(
                containerColor = White1
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("App Name", style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Coco: CoopConnects", style = TextStyle(fontFamily = mintsansFontFamily, fontSize = 16.sp))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 5.dp),
            colors = CardDefaults.cardColors(
                containerColor = White1
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "About Us",
                    style = TextStyle(
                        fontFamily = mintsansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Coco: CoopConnects is an innovative digital platform tailored for the Baguio City Farmers Agriculture Cooperative (BCFAC). The app empowers farmers by linking them directly to consumers or clients while leveraging the cooperative's role as the vital facilitator in managing transactions, inventory, and logistics. It is a tool that enhances traceability, efficiency, and trust in the agricultural supply chain, creating a stronger, more connected farming community in Baguio City.",
                    style = TextStyle(
                        fontFamily = mintsansFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AccessControlScreen(
    userViewModel: UserViewModel,
    facilityViewModel: FacilityViewModel,
    currentUserEmail: String,
    currentUserRole: String
) {
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)
    val currentUserFacility = if (currentUserRole.startsWith("Coop", ignoreCase = true)) {
        facilitiesData.find { facility ->
            facility.emails.contains(currentUserEmail)
        }
    } else null

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
        facilityViewModel.fetchFacilities()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        Text(
            text = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                "System Administrators"
            } else {
                "People Who Can Access ${currentUserFacility?.name ?: "This Facility"}"
            },
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFamily = mintsansFontFamily)
        )

        when {
            facilityState == FacilityState.LOADING || userState == UserState.LOADING -> {
                Text(
                    text = "Loading...",
                    color = Color.Gray,
                    style = TextStyle(fontFamily = mintsansFontFamily)
                )
            }
            currentUserRole.startsWith("Coop") && currentUserFacility == null -> {
                Text(
                    text = "You don't have access to any facility",
                    color = Color.Gray,
                    style = TextStyle(fontFamily = mintsansFontFamily)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    val filteredUsers = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                        usersData.filter { user ->
                            user.role.equals("Admin", ignoreCase = true)
                        }
                    } else {
                        usersData.filter { user ->
                            user.role.startsWith("Coop", ignoreCase = true) &&
                                    currentUserFacility?.emails?.contains(user.email) == true
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        item {
                            Text(
                                text = if (currentUserRole.equals("Admin", ignoreCase = true)) {
                                    "No administrators found"
                                } else {
                                    "No users found for this facility"
                                },
                                color = Color.Gray,
                                style = TextStyle(fontFamily = mintsansFontFamily)
                            )
                        }
                    } else {
                        items(filteredUsers) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = White1),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = user.email,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Green1,
                                        style = TextStyle(fontFamily = mintsansFontFamily)
                                    )

                                    Text(
                                        text = user.role,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        style = TextStyle(fontFamily = mintsansFontFamily),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactDeveloper(
    contactViewModel: ContactViewModel
) {
    val contacts by contactViewModel.contactData.observeAsState(emptyList())
    val state by contactViewModel.contactState.observeAsState(ContactState.LOADING)
    val isAdmin by contactViewModel.isAdmin.observeAsState(false)
    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<ContactData?>(null) }

    LaunchedEffect(Unit) {
        contactViewModel.checkUserRole()
        contactViewModel.fetchContacts("developer,support")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (state) {
                is ContactState.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green1)
                    }
                }
                is ContactState.SUCCESS -> {
                    LazyColumn {
                        items(contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                isAdmin = isAdmin,
                                onEdit = { contactToEdit = it },
                                onDelete = { contactToDelete ->
                                    contactViewModel.deleteContact(contactToDelete.contactId)
                                }
                            )
                        }
                    }
                }
                is ContactState.EMPTY -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No developer contacts found",
                            color = Color.Gray
                        )
                    }
                }
                is ContactState.ERROR -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state as ContactState.ERROR).message,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Green1,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Contact",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { contact ->
                contactViewModel.addContact(contact)
                showAddDialog = false
            }
        )
    }

    if (contactToEdit != null) {
        AddContactDialog(
            onDismiss = { contactToEdit = null },
            onConfirm = { updatedContact ->
                contactViewModel.updateContact(updatedContact.copy(contactId = contactToEdit!!.contactId))
                contactToEdit = null
            },
            initialContact = contactToEdit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (ContactData) -> Unit,
    initialContact: ContactData? = null
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var contactNumber by remember { mutableStateOf(initialContact?.contactNumber?.toString() ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var facebook by remember { mutableStateOf(initialContact?.facebook ?: "") }
    var role by remember { mutableStateOf(initialContact?.role ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf("Developer", "Support")
    val isEditing = initialContact != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = White2
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Contact" else "Add New Contact",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facebook,
                    onValueChange = { facebook = it },
                    label = { Text("Facebook") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    role = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                ContactData(
                                    contactId = initialContact?.contactId ?: "",
                                    name = name,
                                    contactNumber = contactNumber.toLongOrNull() ?: 0,
                                    email = email,
                                    facebook = facebook,
                                    role = role
                                )
                            )
                        },
                        enabled = name.isNotBlank() && contactNumber.isNotBlank() && role.isNotBlank()
                    ) {
                        Text(if (isEditing) "Update" else "Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: ContactData,
    isAdmin: Boolean,
    onEdit: (ContactData) -> Unit,
    onDelete: (ContactData) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Green4
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        color = Green1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = contact.role,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (isAdmin) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(
                            onClick = { onEdit(contact) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Green1
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Green1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    modifier = Modifier.size(20.dp),
                    tint = Green1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = contact.contactNumber.toString())
            }

            if (contact.email.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(20.dp),
                        tint = Green1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = contact.email)
                }
            }

            if (contact.facebook.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Facebook",
                        modifier = Modifier.size(20.dp),
                        tint = Green1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = contact.facebook)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete ${contact.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(contact)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ManageFacilitiesScreen(
    facilityViewModel: FacilityViewModel,
    navController: NavController
) {
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showArchivedDialog by remember { mutableStateOf(false) }
    var selectedFacility by remember { mutableStateOf<FacilityData?>(null) }
    var hasContent by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeFacilities = remember(facilitiesData, searchQuery) {
        facilitiesData.filter { facility ->
            !facility.isArchived && (
                    searchQuery.isEmpty() ||
                            facility.name.contains(searchQuery, ignoreCase = true) ||
                            facility.emails.any { it.contains(searchQuery, ignoreCase = true) }
                    )
        }
    }

    val archivedFacilities = remember(facilitiesData) {
        facilitiesData.filter { it.isArchived }
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Facilities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Green1
                    )
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchActive) "Close Search" else "Search"
                    )
                }

                IconButton(
                    onClick = {
                        showArchivedDialog = true
                        facilityViewModel.fetchFacilities(true)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Green1
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.archived),
                        contentDescription = "View Archived"
                    )
                }
            }
        }

        // Search bar
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text("Search facilities...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green1,
                    focusedLabelColor = Green1
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Green1
                    )
                }
            )
        }

        // Main content
        when (facilityState) {
            is FacilityState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green1)
                }
            }

            is FacilityState.ERROR -> {
                Text(
                    text = (facilityState as FacilityState.ERROR).message,
                    color = Color.Red
                )
            }

            else -> {
                if (activeFacilities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No active facilities found"
                            else "No matching facilities found",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeFacilities) { facility ->
                            FacilityManagementCard(
                                facility = facility,
                                onDeleteClick = {
                                    selectedFacility = facility
                                    showConfirmationDialog = true
                                    facilityViewModel.checkFacilityContent(facility.name) { facilityHasContent ->
                                        hasContent = facilityHasContent
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete/Archive Confirmation Dialog
    if (showConfirmationDialog && selectedFacility != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                selectedFacility = null
            },
            title = { Text(if (hasContent) "Archive Facility" else "Delete Facility") },
            text = {
                Column {
                    if (hasContent) {
                        Text(
                            "This facility (${selectedFacility?.name}) has existing orders in the system. " +
                                    "To maintain data integrity and order history, the facility will be archived."
                        )
                    } else {
                        Text("Are you sure you want to delete ${selectedFacility?.name}?")
                        Text(
                            "This action cannot be undone.",
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFacility?.let { facility ->
                            if (hasContent) {
                                facilityViewModel.archiveFacility(
                                    facilityName = facility.name,
                                    onSuccess = {
                                        showConfirmationDialog = false
                                        selectedFacility = null
                                        facilityViewModel.fetchFacilities(false)
                                    },
                                    onError = { errorMessage ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = errorMessage,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            } else {
                                facilityViewModel.deleteFacility(
                                    facilityName = facility.name,
                                    onSuccess = {
                                        showConfirmationDialog = false
                                        selectedFacility = null
                                        facilityViewModel.fetchFacilities(false)
                                    },
                                    onError = { errorMessage ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = errorMessage,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasContent) Green1 else Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (hasContent) "Archive" else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        selectedFacility = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Archived Facilities Dialog
    if (showArchivedDialog) {
        AlertDialog(
            onDismissRequest = {
                showArchivedDialog = false
                facilityViewModel.fetchFacilities(false)
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Archived Facilities",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "(${archivedFacilities.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            },
            text = {
                if (archivedFacilities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No archived facilities",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(archivedFacilities) { facility ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = facility.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${facility.emails.size} members",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                if (facility.archivedDate != 0L) {
                                    Text(
                                        text = "Archived on: ${facility.archivedDate?.let {
                                            formatDate(
                                                it
                                            )
                                        }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Button(
                                    onClick = {
                                        facilityViewModel.unarchiveFacility(
                                            facilityName = facility.name,
                                            onSuccess = {
                                                showArchivedDialog = false
                                                facilityViewModel.fetchFacilities(true)
                                            },
                                            onError = { errorMessage ->
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = errorMessage,
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green1,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Unarchive")
                                }
                            }
                            if (facility != archivedFacilities.last()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchivedDialog = false
                        facilityViewModel.fetchFacilities(false)
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FacilityManagementCard(
    facility: FacilityData,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facility.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${facility.emails.size} members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Facility"
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown date"
    }
}