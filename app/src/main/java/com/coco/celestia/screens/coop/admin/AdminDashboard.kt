package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.UserData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AdminHome(
    navController: NavController,
    facilityViewModel: FacilityViewModel,
    userViewModel: UserViewModel,
    specialRequestViewModel: SpecialRequestViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var currentView by remember { mutableStateOf("Dashboard") }
    val requests by specialRequestViewModel.specialReqData.observeAsState()

    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchSpecialRequests("", "", true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        // Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationTab(
                title = "Dashboard",
                iconRes = R.drawable.dashboard,
                isActive = currentView == "Dashboard",
                onClick = {
                    currentView = "Dashboard"
                }
            )
            NavigationTab(
                title = "Add Facility",
                iconRes = R.drawable.facility,
                isActive = currentView == "Add Facility",
                onClick = {
                    currentView = "Add Facility"
                }
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(if (currentView == "Dashboard") Alignment.Start else Alignment.End),
            color = Green1,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (currentView) {
            "Dashboard" -> {
                Facilities(facilityViewModel)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Special Requests",
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SpecialRequestCard(
                        title = "To Review",
                        count = requests?.count { it.status == "To Review"}.toString(),
                        onClick = {
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("To Review"))
                        }
                    )
                    SpecialRequestCard(
                        title = "In Progress",
                        count = requests?.count { it.status == "In Progress"}.toString(),
                        onClick = {
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("In Progress"))
                        }
                    )
                    SpecialRequestCard(
                        title = "Cancelled",
                        count = requests?.count { it.status == "Cancelled"}.toString(),
                        onClick = {
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("Cancelled"))
                        }
                    )
                    SpecialRequestCard(
                        title = "Turned Down",
                        count = requests?.count { it.status == "Turned Down"}.toString(),
                        onClick = {
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("Turned Down"))
                        }
                    )
                }
            }
            "Add Facility" -> {
                AddFacilityForm(
                    navController = navController,
                    facilityViewModel = facilityViewModel,
                    userViewModel = userViewModel,
                    onEvent = { onEvent(it) })
            }
        }
    }
}

@Composable
fun Facilities(facilityViewModel: FacilityViewModel) {
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Text(
        text = "My Facilities",
        color = Green1,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
    )
    when (facilityState) {
        FacilityState.LOADING -> {
            Text(
                text = "Loading...",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        FacilityState.EMPTY -> {
            Text(
                text = "No facilities yet.",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        is FacilityState.ERROR -> {
            Text(
                text = "Error occurred: ${(facilityState as FacilityState.ERROR).message}",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        FacilityState.SUCCESS -> {
            // TODO: WIP for now. Ginawa ko munang LazyRow para lang ma display.
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(facilitiesData) { _, facility ->
                    FacilityCard(facility)
                }
            }
        }
    }
}

@Composable
fun FacilityCard(facility: FacilityData) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp),
        colors = CardDefaults.cardColors(containerColor = Green4)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = facility.icon),
                    tint = Green1,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Text(text = facility.name, color = Green1)
            }
        }
    }
}

@Composable
fun NavigationTab(
    title: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                colorFilter = if (isActive) ColorFilter.tint(Green1) else null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isActive) Green1 else Color.Black,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFacilityForm(
    navController: NavController,
    userViewModel: UserViewModel,
    facilityViewModel: FacilityViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var facilityFocused by remember { mutableStateOf(false) }
    var iconPickerShown by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableIntStateOf(R.drawable.facility) }
    var expanded by remember { mutableStateOf(false) }
    var noMembersSelected by remember { mutableStateOf(false) }

    val usersData by userViewModel.usersData.observeAsState(initial = emptyList())
    val noFacilityUsers = usersData.filter { it.role == "Coop" }
    var selectedUsers by remember { mutableStateOf(setOf<UserData>()) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Green4),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .border(BorderStroke(2.dp, Green1), RoundedCornerShape(5.dp))
                            .size(75.dp)
                    ) {
                        if (selectedIcon == R.drawable.facility) {
                            Image(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Facility Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { iconPickerShown = true }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = selectedIcon),
                                contentDescription = "Facility Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { iconPickerShown = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Facility Name",
                        color = Green1,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "Enter name of Facility",
                            fontStyle = FontStyle.Italic,
                            color = if (facilityFocused) Color.Transparent else Color.Gray,
                            fontFamily = mintsansFontFamily
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .onFocusChanged { focusState ->
                            facilityFocused = focusState.isFocused
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = White1,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Users",
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when {
                            noMembersSelected -> "No Members for Now"
                            selectedUsers.isEmpty() -> "Select users"
                            else -> "${selectedUsers.size} user(s) selected"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = White1,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .exposedDropdownSize()
                    ) {
                        // No Members for Now option
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("No Members for Now", fontFamily = mintsansFontFamily)
                                    if (noMembersSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Green1
                                        )
                                    }
                                }
                            },
                            onClick = {
                                noMembersSelected = !noMembersSelected
                                if (noMembersSelected) {
                                    selectedUsers = emptySet()
                                }
                                expanded = false
                            }
                        )

                        Divider()

                        if (noFacilityUsers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No users available without a facility") },
                                onClick = { },
                                enabled = false
                            )
                        } else {
                            noFacilityUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("${user.firstname} ${user.lastname}", fontFamily = mintsansFontFamily)
                                            if (selectedUsers.contains(user)) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = Green1
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        noMembersSelected = false
                                        selectedUsers = if (selectedUsers.contains(user)) {
                                            selectedUsers - user
                                        } else {
                                            selectedUsers + user
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (selectedUsers.isNotEmpty() && !noMembersSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selected Users:",
                        color = Green1,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
                    )
                    selectedUsers.forEach { user ->
                        Text(
                            text = "${user.firstname} ${user.lastname}",
                            color = Green1,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && (selectedUsers.isNotEmpty() || noMembersSelected)) {
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma", Locale.US)
                    val formattedDateTime = currentDateTime.format(formatter).toString()

                    facilityViewModel.createFacility(
                        icon = selectedIcon,
                        name = name,
                        emails = if (noMembersSelected) mutableListOf() else selectedUsers.map { it.email }.toMutableList(),
                        onComplete = {
                            onEvent(Triple(ToastStatus.SUCCESSFUL, "$name facility added.", System.currentTimeMillis()))
                            if (!noMembersSelected) {
                                userViewModel.assignFacility(selectedUsers.map { it.email }, name)
                            }
                            navController.navigate(Screen.Admin.route)
                        },
                        onError = { onEvent(Triple(ToastStatus.FAILED, it, System.currentTimeMillis())) }
                    )
                } else {
                    onEvent(Triple(
                        ToastStatus.FAILED,
                        if (name.isEmpty()) "Please enter a facility name."
                        else "Please select users or choose 'No Members for Now'",
                        System.currentTimeMillis()
                    ))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Green1),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "ADD", color = Color.White)
        }
    }

    if (iconPickerShown) {
        IconPicker(
            selected = selectedIcon,
            onDismiss = { iconPickerShown = false },
            onConfirm = {
                selectedIcon = it
                iconPickerShown = false
            }
        )
    }
}

// Icon picker for facility creation
@Composable
fun IconPicker(
    selected: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIcon by remember { mutableIntStateOf(selected) }
    // If you wanna add icons to the icon picker, prefix the file with 'fac_icon_'.
    var icons = R.drawable::class.java.fields
        .filter { it.name.startsWith("fac_icon_") }
        .map { it.getInt(null) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Pick an icon for the facility:",
                fontFamily = mintsansFontFamily,
                fontSize = 16.sp,
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(icons) { _, icon ->
                    Row {
                        IconButton(
                            onClick = { selectedIcon = icon },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (selectedIcon == icon) JadeGreen else Color.Transparent,
                                contentColor = if (selectedIcon == icon) White1 else Color.DarkGray
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel", fontFamily = mintsansFontFamily)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIcon) },
                enabled = selectedIcon != selected
            ) {
                Text(text = "Confirm", fontFamily = mintsansFontFamily)
            }
        },
    )
}

@Composable
fun SpecialRequestCard(title: String, count: String, onClick: () -> Unit) {
    val imageRes = when (title) {
        "To Review" -> R.drawable.preparing
        "In Progress" -> R.drawable.progress
        "Cancelled" -> R.drawable.cancelled
        "Turned Down" -> R.drawable.cancel
        else -> R.drawable.warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Green4),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "$title Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Green1)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Green1,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = count,
                color = Green1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}