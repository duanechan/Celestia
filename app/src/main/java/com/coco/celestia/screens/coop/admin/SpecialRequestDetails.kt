package com.coco.celestia.screens.coop.admin

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green2
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White2
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.AssignedMember
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.ProductReq
import com.coco.celestia.viewmodel.model.ProductStatus
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

@SuppressLint("MutableCollectionMutableState")
@Composable
fun SpecialRequestDetails(
    navController: NavController,
    userViewModel: UserViewModel,
    specialRequestViewModel: SpecialRequestViewModel,
    request: SpecialRequest
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(request.dateRequested, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    var showDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var updateStatusDialog by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var action by remember { mutableStateOf("") }
    val usersData by userViewModel.usersData.observeAsState()
    val assignedMember = remember { mutableStateListOf<AssignedMember>() }
    val trackRecord = remember { mutableStateListOf(*request.trackRecord.toTypedArray()) }
    val toDeliver = remember { mutableStateListOf(*request.toDeliver.toTypedArray()) }

    var text by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    var updateEmail by remember { mutableStateOf("") }
    var updateStatus by remember { mutableStateOf("") }

    var memberEmpty by remember { mutableStateOf(false) }
    var productEmpty by remember { mutableStateOf(false) }
    var quantityEmpty by remember { mutableStateOf(false) }
    var quantityExceeded by remember { mutableStateOf(false) }

    var productPairs by remember { mutableStateOf(request.products.associate { it.name to it.quantity }.toMutableMap()) }
    val quantityPair = productPairs[product] ?: 0

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    val filteredUsers = usersData?.filter {
        (it.role == "Farmer")
    }?.map { "${it.firstname} ${it.lastname} - ${it.email}" } ?: emptyList()

    val productList = request.products.map { it.name }

    fun updateQuantity(product: String, newQuantity: Int) {
        productPairs[product] = newQuantity
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .verticalScroll(rememberScrollState())
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Date of Request: $date"
            )
        }
        //Request ID etc.
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Green4,
                    shape = RoundedCornerShape(12.dp)
                )
        ){
            Text(
                text = "Request ID",
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            )

            Text(
                text = request.specialRequestUID.split("-").take(4).joinToString("-"),
                modifier = Modifier
                    .padding(16.dp)
                    .weight(2f)
            )
        }
        // Toggle Button
        if (request.status == "In Progress" || request.status == "Completed") {
            Row (
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Details",
                    fontWeight = FontWeight.Bold
                )

                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it},
                    modifier = Modifier
                        .graphicsLayer (
                            scaleX = 0.6f,
                            scaleY = 0.6f
                        ),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Green4,
                        checkedTrackColor = Green1,
                        uncheckedThumbColor = Green2,
                        uncheckedTrackColor = Green4
                    )
                )
            }
        }

        if (checked) {
            DisplayRequestDetails(
                request
            )
        }

        if (request.status == "In Progress" || request.status == "Completed") {
            if (request.assignedMember.isEmpty()) {
                AssignAMember(
                    assignedMember,
                    productPairs,
                    onDismiss = {
                        assignedMember.clear()
                        productPairs = request.products.associate { it.name to it.quantity }.toMutableMap()
                    },
                    onSave = {
                        assignedMember.forEach { member ->
                            val assignMember = TrackRecord (
                                description = "Assigned to ${member.name}: ${member.product} ${member.quantity}kg.",
                                dateTime = formattedDateTime
                            )

                            trackRecord.add(assignMember)
                        }

                        specialRequestViewModel.updateSpecialRequest(
                            request.copy(
                                assignedMember = assignedMember,
                                trackRecord = trackRecord
                            )
                        )
                    },
                    onShowAddMember = { showAddMemberDialog = true }
                )
            } else {
                DisplayAssignedMembers(
                    request
                )
            }
        }

        if (request.status == "In Progress") {
            request.assignedMember.filter { it.status == "Delivering to Coop" }.forEach { member ->
                Button(
                    onClick = {
                        val remainingQuantity = member.remainingQuantity - member.deliveredQuantity
                        val updatedMember = member.copy(
                            remainingQuantity = remainingQuantity,
                            deliveredQuantity = 0,
                            status = if (remainingQuantity == 0) "Completed" else "Received by Coop"
                        )

                        val updatedMembers = request.assignedMember.map { assigned ->
                            if (assigned.email == member.email) {
                                val farmerTrackRecord = assigned.farmerTrackRecord.toMutableList()
                                val addTrack = TrackRecord(
                                    description = if (remainingQuantity == 0) {
                                        "Farmer ${member.name} status: Completed - Products Received by Coop"
                                    } else {
                                        "Farmer ${member.name} status: Received by Coop - Products Received by Coop"
                                    },
                                    dateTime = formattedDateTime
                                )
                                val addFarmerTrack = TrackRecord(
                                    description = if (remainingQuantity == 0) {
                                        "Completed: Products Received by Coop"
                                    } else {
                                        "Received by Coop: Products Received by Coop"
                                    },
                                    dateTime = formattedDateTime
                                )
                                val addToDeliver = ProductStatus(
                                    name = member.product,
                                    quantity = member.deliveredQuantity,
                                    status = "To Deliver"
                                )

                                farmerTrackRecord.add(addFarmerTrack)
                                trackRecord.add(addTrack)
                                toDeliver.add(addToDeliver)

                                updatedMember.copy(farmerTrackRecord = farmerTrackRecord)
                            } else assigned
                        }

                        specialRequestViewModel.updateSpecialRequest(
                            request.copy(
                                assignedMember = updatedMembers,
                                trackRecord = trackRecord,
                                toDeliver = toDeliver
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = Green1
                    ),
                    modifier = Modifier
                        .height(52.dp)
                ) {
                    Text(
                        text = "Receive ${member.product} from ${member.name}",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        // Deliver/ Pickup button
        if (request.toDeliver.any { it.status == "To Deliver" }) {
            Button(
                onClick = {
                    val updateProductStatus = request.toDeliver.map { product ->
                        val addTrack = TrackRecord(
                            description = "Delivering ${product.name}: ${product.quantity}kg",
                            dateTime = formattedDateTime
                        )
                        trackRecord.add(addTrack)

                        product.copy(
                            status = "Delivering to Client"
                        )
                    }

                    specialRequestViewModel.updateSpecialRequest(
                        request.copy(
                            trackRecord = trackRecord,
                            toDeliver = updateProductStatus
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = Green1
                ),
                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(
                    text = "Deliver Products to Client",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Accept/ Decline
        if (request.status == "To Review") {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            action = "Decline"
                            showDialog = true
                        }
                    ) {
                        Text("Decline")
                    }

                    Button(
                        onClick = {
                            action = "Accept"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(Green4)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }

        if (request.status == "In Progress" || request.status == "Completed") {
            // Track Order and Update Order
            if (request.trackRecord.isNotEmpty()) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text (
                        text = "Track Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Green1
                    )

                    Button(
                        onClick = {
                            updateStatusDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(Green4)
                    ) {
                        Text(
                            text = "Update Status",
                            color = Green2
                        )
                    }
                }

                request.trackRecord
                    .sortedByDescending { it.dateTime }
                    .forEachIndexed { index, record ->
                        DisplayTrackOrder(
                            record,
                            index == request.trackRecord.lastIndex
                        )
                    }
            }
        }
    }

    if (updateStatusDialog) {
        UpdateStatusDialog(
            request = request,
            onDismiss = { updateStatusDialog = false },
            onConfirm = { updateStatusEmail, status ->
                updateEmail = updateStatusEmail
                updateStatus = status

                request.assignedMember.map { member ->
                    if (member.email == updateEmail) {
                        val addRecord = TrackRecord(
                            description = "Farmer ${member.name} status: $updateStatus",
                            dateTime = formattedDateTime
                        )

                        trackRecord.add(addRecord)
                    }
                }
                specialRequestViewModel.updateSpecialRequest(
                    request.copy(
                            assignedMember = request.assignedMember.map { member ->
                                if (member.email == updateEmail) {
                                    member.copy(status = updateStatus)
                                } else {
                                    member
                                }
                            },
                            trackRecord = trackRecord
                        )
                )
                updateStatusDialog = false
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false},
            title = {
                Text("Confirmation")
            },
            text = {
                Text("Are you sure you want to $action this request?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (action == "Accept") {
                            val acceptOrder = TrackRecord(
                                description = "Order Request has been accepted.",
                                dateTime = formattedDateTime
                            )
                            trackRecord.add(acceptOrder)

                            specialRequestViewModel.updateSpecialRequest(
                                request.copy(
                                    status = "In Progress",
                                    dateAccepted = formattedDateTime,
                                    trackRecord = trackRecord
                                )
                            )
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("In Progress"))
                        } else {
                            val declineOrder = TrackRecord(
                                description = "Order Request has been turned down.",
                                dateTime = formattedDateTime
                            )
                            trackRecord.add(declineOrder)

                            specialRequestViewModel.updateSpecialRequest(
                                request.copy(
                                    status = "Turned Down"
                                )
                            )
                            navController.navigate(Screen.AdminSpecialRequests.createRoute("Turned Down"))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Green4)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false},
            title = {
                Text("Assign a Member")
            },
            text = {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable (
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                expanded = false
                            }
                        )
                ){
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Member",
                            modifier = Modifier.padding(2.dp)
                        )

                        if (memberEmpty) {
                            Text(
                                text = "Member cannot be Empty",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    AutocompleteTextField(
                        suggestions = filteredUsers,
                        onSuggestionClick = { selectedUser ->
                            text = selectedUser
                        }
                    )

                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Product",
                            modifier = Modifier.padding(2.dp)
                        )
                        if (productEmpty) {
                            Text(
                                text = "Product cannot be Empty",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    Column {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = Green2,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { productExpanded = !productExpanded },
                            value = product,
                            onValueChange = {
                                product = it
                                productExpanded = true
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { productExpanded = !productExpanded }) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledContainerColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            enabled = false,
                        )

                        DropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false}
                        ) {
                            productList.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        product = it
                                        productExpanded = false
                                    },
                                    text = {
                                        Text(text = it)
                                    }
                                )
                            }
                        }
                    }

                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantity",
                            modifier = Modifier.padding(2.dp)
                        )

                        if (quantityEmpty) {
                            Text(
                                text = "Quantity cannot be Empty",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        if (quantityExceeded) {
                            Text(
                                text = "Exceeds required amount",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = Green2,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        value = if (quantity == 0) "" else quantity.toString(),
                        onValueChange = { newValue ->
                            quantity = newValue.toIntOrNull() ?: 0
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        memberEmpty = text.isEmpty()
                        productEmpty = product.isEmpty()
                        quantityEmpty = quantity == 0
                        quantityExceeded = quantity > quantityPair

                        if (!quantityExceeded) {
                            val newQuantity = quantityPair - quantity
                            updateQuantity(product, newQuantity)
                        }

                        if (!memberEmpty && !productEmpty && !quantityEmpty && !quantityExceeded) {
                            email = text.substringAfter(" - ").trim()
                            val name = text.substringBefore(" - ").trim()

                            val existingMember = assignedMember.find { it.email == email && it.product == product }

                            if (existingMember != null) {
                                existingMember.quantity += quantity
                            } else {
                                val uuidPart = UUID.randomUUID().toString().take(6).uppercase()
                                val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(
                                    Date()
                                )
                                val trackingID = "REQ-$timestamp-$uuidPart"

                                val member = AssignedMember(
                                    email = email,
                                    specialRequestUID = request.specialRequestUID,
                                    name = name,
                                    product = product,
                                    quantity = quantity,
                                    trackingID = trackingID
                                )
                                assignedMember.add(member)
                            }

                            text = ""
                            product = ""
                            quantity = 0
                            showAddMemberDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Green4)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        text = ""
                        product = ""
                        quantity = 0
                        showAddMemberDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AssignAMember (
    assignedMember: SnapshotStateList<AssignedMember>,
    productPairs: MutableMap<String, Int>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShowAddMember: () -> Unit
) {
    var unfulfilled by remember { mutableStateOf(false) }
    val unfulfilledRequests = productPairs.filter { it.value != 0 }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Green4,
                shape = RoundedCornerShape(12.dp)
            )
    ){
        Row (
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assign a Member",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 16.dp)
            )

            Image(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onShowAddMember() }
            )
        }

        if (assignedMember.isNotEmpty()) {
            assignedMember.forEach { member ->
                Column (
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ){
                    Text(
                        text = "Name: ${member.name}",
                    )

                    Text(
                        text = "Product: ${member.product}"
                    )

                    Text(
                        text = "Quantity: ${member.quantity}"
                    )
                }
            }

            if (unfulfilled) {
                Column (
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Some products remain unfulfilled:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    unfulfilledRequests.forEach { request ->
                        Text(
                            text = request.key,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier
                                .padding(start = 6.dp)
                        )
                    }
                }
            }

            Box (
                modifier = Modifier.fillMaxWidth()
            ){
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            unfulfilled = unfulfilledRequests.isNotEmpty()

                            if (!unfulfilled) {
                                onSave()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Green4)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                isDropdownExpanded = newQuery.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Enter Member") },
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp)
        ) {
            val filteredSuggestions = suggestions.filter {
                it.contains(query, ignoreCase = true)
            }
            if (filteredSuggestions.isNotEmpty()) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            query = suggestion
                            onSuggestionClick(suggestion)
                            isDropdownExpanded = false
                        }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("No suggestions found") },
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun DisplayAssignedMembers (
    request: SpecialRequest
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Green4,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text(
                text = "Assigned Member/s:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row {
                Text(
                    text = "Member Name",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )

                Text(
                    text = "Product",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Quantity",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            request.assignedMember.forEach { member ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ){
                    Text(
                        text = member.name,
                        modifier = Modifier.weight(2f)
                    )

                    Text(
                        text = member.product,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = member.quantity.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // TODO: Display delivered quantity
        }
    }
}

@Composable
fun DisplayTrackOrder (
    record: TrackRecord,
    isLastItem: Boolean
) {
    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(record.dateTime, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val date = dateTime.format(dateFormatter)
    val time = dateTime.format(timeFormatter)

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 32.dp, end = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column (
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = date,
                fontWeight = FontWeight.Bold,
                color = Green1
            )

            Text(
                text = time,
                fontWeight = FontWeight.Bold,
                color = Green1
            )
        }

        Column (
            modifier = Modifier
                .padding(horizontal = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(16.dp)
            ) {
                drawCircle(color = Green2)

                if (!isLastItem) {
                    drawLine(
                        color = Green4,
                        start = Offset(size.width / 2, size.height),
                        end = Offset(size.width / 2, size.height + 80.dp.toPx()),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }
        }

        Text(
            text = record.description,
        )
    }
}

@Composable
fun UpdateStatusDialog (
    request: SpecialRequest,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var farmerExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val formattedUser = request.assignedMember.map { "${it.name} - ${it.email}" }
    val statusList = listOf(
        "Soil Preparation", "Seed Sowing", "Growing",
        "Pre-Harvest", "Harvesting", "Post-Harvest",
        "Delivering to Coop", "Calamity Affected", "Cancelled"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status For:") },
        text = {
            Column {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = Green2,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { farmerExpanded = !farmerExpanded },
                    label = { Text("Select Farmer") },
                    value = text,
                    onValueChange = {
                        text = it
                        email = text.substringAfter(" - ").trim()
                        farmerExpanded = true
                    },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { farmerExpanded = !farmerExpanded }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    enabled = false,
                )

                DropdownMenu(
                    expanded = farmerExpanded,
                    onDismissRequest = { farmerExpanded = false}
                ) {
                    formattedUser.forEach {
                        DropdownMenuItem(
                            onClick = {
                                text = it
                                email = text.substringAfter(" - ").trim()
                                farmerExpanded = false
                            },
                            text = {
                                Text(text = it)
                            }
                        )
                    }
                }

                if (text.isNotEmpty()) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .border(
                                width = 2.dp,
                                color = Green2,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { statusExpanded = !statusExpanded },
                        label = { Text("Select Status") },
                        value = status,
                        onValueChange = {
                            status = it
                            statusExpanded = true
                        },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { statusExpanded = !statusExpanded }) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledContainerColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        enabled = false,
                    )

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false}
                    ) {
                        statusList.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    status = it
                                    statusExpanded = false
                                },
                                text = {
                                    Text(text = it)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(email, status) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DisplayRequestDetails(
    request: SpecialRequest
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = "Subject",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = request.subject,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(2f)
            )
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp)
        )

        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = request.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(2f)
            )
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Request Details",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Green4,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = "Product/s and Quantity",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            request.products.forEachIndexed { index, product ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .padding(2.dp)
                ) {
                    Text(
                        text = "${index + 1}. ${product.name}: ${product.quantity} kg"
                    )
                }
            }

            Text(
                text = "Target Delivery Date: ${request.targetDate}",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = "Collection Method: ${request.collectionMethod}",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            if (request.collectionMethod == Constants.COLLECTION_DELIVERY) {
                Text(
                    text = "Delivery Location:",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "Pick Up Location:",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            }

            Text(
                text = request.deliveryAddress,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )

            Text(
                text = "Additional Request/s:",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = request.additionalRequest.ifEmpty { "N/A" },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )

            DisplayAttachments(
                requestId = request.specialRequestUID,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
            )
        }
    }
}