package com.coco.celestia.screens.farmer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green2
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White2
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DisplayRequestDetails (
    navController: NavController,
    specialRequestViewModel: SpecialRequestViewModel,
    specialRequestUID: String,
    farmerEmail: String,
    product: String
) {
    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchSpecialRequests("", "", true)
    }

    val specialRequests by specialRequestViewModel.specialReqData.observeAsState()
    val specialRequest = specialRequests?.find { it.specialRequestUID == specialRequestUID }
    var requiredQuantity by remember { mutableIntStateOf(0) }

    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(specialRequest?.dateRequested ?: "", inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    val date = dateTime.format(dateFormatter)

    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    var checked by remember { mutableStateOf(true) }
    var updateStatusDialog by remember { mutableStateOf(false) }

    val trackRecord = remember { mutableStateListOf(*specialRequest?.trackRecord!!.toTypedArray()) }
    val farmerTrackRecord = remember {
        val farmer = specialRequest?.assignedMember?.find { it.email == farmerEmail }
        mutableStateListOf(*(farmer?.farmerTrackRecord?.toTypedArray() ?: emptyArray()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        Row(
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
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text(
                        text = "Date of Request: ",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = date
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "Request ID: ",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = specialRequest?.specialRequestUID?.split("-")?.take(4)
                            ?.joinToString("-") ?: ""
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Details",
                fontWeight = FontWeight.Bold
            )

            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier
                    .graphicsLayer(
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

        if (checked) {
            if (specialRequest != null) {
                DisplayDetails(
                    specialRequest = specialRequest
                )
            }
        }

        Text(
            text = "Assigned to you",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )

        specialRequest?.assignedMember?.forEach { member ->
            if (member.email == farmerEmail && member.product == product) {
                Column (
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
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = member.product,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                        )

                        Text(
                            text = "${member.quantity}kg",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }

                    val delivered = member.quantity - member.remainingQuantity
                    if (delivered != 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quantity Delivered",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(16.dp)
                            )

                            Text(
                                text = "${delivered}kg",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                    }
                }

            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Assign Milestone
                specialRequest?.assignedMember?.forEach { member ->
                    if (member.email == farmerEmail && member.product == product) {
                        if (member.status != "Delivering to Coop") {
                            if (member.status == "Completed" || member.status == "Calamity Affected") {
                                Button(
                                    onClick = { },
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color.White,
                                        disabledContainerColor = Green4
                                    ),
                                    enabled = false,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Text(
                                        text =  if (member.status == "Completed") "Order is Completed" else "Order is affected by Unforeseen Event/s",
                                        color = DarkGreen,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        updateStatusDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color.White,
                                        containerColor = Green1
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .padding(top = 16.dp)
                                ) {
                                    Text("Assign Milestone")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.dashboard),
                                        contentDescription = "Assign Milestone Icon",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { },
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.White,
                                    disabledContainerColor = Green4
                                ),
                                enabled = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Waiting for Coop to Receive the Products",
                                    color = DarkGreen,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
//                // Notify Unforeseen Circumstances
//                Button(
//                    onClick = {
//                        specialRequestViewModel.notify(NotificationType.FarmerCalamityAffected, specialRequest!!)
//                    },
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        contentColor = Color.White,
//                        containerColor = Green1
//                    ),
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp)
//                ) {
//                    Text(
//                        text = "Notify Unforeseen Circumstances",
//                        color = Color.White,
//                        fontSize = 12.sp,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
            }

            // Track Progress
            specialRequest?.assignedMember?.forEach { member ->
                if (member.email == farmerEmail && member.product == product && member.farmerTrackRecord.isNotEmpty()) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.FarmerProgressTracking.createRoute(member.trackingID))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Green1
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Track Progress")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.deliveryicon),
                                contentDescription = "Track Order Icon",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (updateStatusDialog) {
        if (specialRequest != null) {
            specialRequest.assignedMember.map { member ->
                if (member.email == farmerEmail && member.product == product) {
                    requiredQuantity = member.remainingQuantity
                }
            }
            DisplayUpdateStatus(
                product = product,
                onConfirm = { status, description, quantity, imageUri ->
                    if (imageUri != null) {
                        specialRequestViewModel.uploadStatusImage(imageUri) { imageUrl ->
                            updateSpecialRequestWithStatus(
                                specialRequest,
                                status,
                                description,
                                quantity,
                                imageUrl,
                                farmerEmail,
                                product,
                                formattedDateTime,
                                trackRecord,
                                farmerTrackRecord,
                                specialRequestViewModel
                            )
                        }
                    } else {
                        updateSpecialRequestWithStatus(
                            specialRequest,
                            status,
                            description,
                            quantity,
                            null,
                            farmerEmail,
                            product,
                            formattedDateTime,
                            trackRecord,
                            farmerTrackRecord,
                            specialRequestViewModel
                        )
                    }
                    updateStatusDialog = false
                },
                onDismiss = { updateStatusDialog = false },
                specialRequestViewModel = specialRequestViewModel,
                specialRequest = specialRequest,
                requiredQuantity = requiredQuantity
            )
        }
    }
}

//Changed Status to Milestone
@OptIn(ExperimentalCoilApi::class)
@Composable
fun DisplayUpdateStatus(
    product: String,
    requiredQuantity: Int,
    onConfirm: (String, String, Int, Uri?) -> Unit,
    onDismiss: () -> Unit,
    specialRequestViewModel: SpecialRequestViewModel,
    specialRequest: SpecialRequest?
) {
    var status by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(0) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showNotificationPopup by remember { mutableStateOf(false) }
    var quantityExceeded by remember { mutableStateOf(false) }
    var emptyMilestone by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val statusList = listOf(
        "Soil Preparation", "Seed Sowing", "Growing",
        "Pre-Harvest", "Harvesting", "Post-Harvest",
        "Delivering to Coop", "Calamity Affected"
    )

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Milestone For:") },
        text = {
            Column {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = 2.dp,
                            color = Green2,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    label = { Text("Product") },
                    value = product,
                    onValueChange = { },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    enabled = false,
                )

                if (product.isNotEmpty()) {
                    if (emptyMilestone) {
                        Text(
                            text = "Milestone Empty!",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 2.dp,
                                color = Green2,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { statusExpanded = !statusExpanded },
                        label = { Text("Select Milestone") },
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
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusList.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    status = it
                                    statusExpanded = false
                                },
                                text = { Text(text = it) }
                            )
                        }
                    }
                }

                if (status == "Delivering to Coop") {
                    val radioOptions = listOf("Full Delivery", "Partial Delivery")
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

                    Column (Modifier.selectableGroup()) {
                        radioOptions.forEach { text ->
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {  
                                RadioButton(
                                    selected = (text == selectedOption),
                                    onClick = null
                                )

                                Text(
                                    text = text
                                )
                            }
                        }
                    }

                    if (selectedOption == "Partial Delivery") {
                        Text(
                            text = "* Maximum quantity is ${requiredQuantity}kg",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (quantityExceeded) {
                            Text(
                                text = "Quantity Exceeded!",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        OutlinedTextField(
                            value = if (quantity == 0) "" else quantity.toString(),
                            onValueChange = { quantity = it.toIntOrNull() ?: 0 },
                            label = { Text("Quantity to Deliver") },
                            placeholder = { Text("Enter Quantity") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                errorContainerColor = Color.White,
                                focusedIndicatorColor = Green1,
                                unfocusedIndicatorColor = Green4
                            )
                        )
                    } else {
                        quantity = requiredQuantity
                    }
                }

                if (status.isNotEmpty()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Update Progress") },
                        placeholder = { Text("Update the cooperative with your progress...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedIndicatorColor = Green1,
                            unfocusedIndicatorColor = Green4
                        )
                    )
                }

                if (status == "Calamity Affected") {
                    Button(
                        onClick = {
                            specialRequestViewModel.notify(
                                NotificationType.FarmerCalamityAffected,
                                specialRequest!!
                            )
                            showNotificationPopup = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green1
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Notify Unforeseen Circumstances",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (status.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { launcher.launch("image/*") },
                            border = BorderStroke(1.dp, Gray),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.camera),
                                    contentDescription = "Camera Icon",
                                    modifier = Modifier.size(24.dp),
                                    tint = Gray
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "Show proof of progress.",
                                        color = Gray,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "*Proof of Progress is optional.",
                                        color = Gray.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        selectedImageUri?.let { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Image(
                                    painter = rememberImagePainter(
                                        data = uri,
                                        builder = {
                                            crossfade(true)
                                        }
                                    ),
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    emptyMilestone = status.isEmpty()
                    quantityExceeded = quantity > requiredQuantity

                    if (!quantityExceeded && !emptyMilestone) {
                        onConfirm(status, description, quantity, selectedImageUri)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showNotificationPopup) {
        AlertDialog(
            onDismissRequest = { showNotificationPopup = false },
            title = { Text("Notification Sent") },
            text = { Text("The cooperative has been notified about unforeseen circumstances.") },
            confirmButton = {
                Button(onClick = { showNotificationPopup = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DisplayDetails (
    specialRequest: SpecialRequest
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row (
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
                text = specialRequest.subject,
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

        Row (
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
                text = specialRequest.description,
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

        Column (
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            specialRequest.products.forEachIndexed { index, product ->
                Row (
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .padding(2.dp)
                ){
                    Text(
                        text = "${index + 1}. ${product.name}: ${product.quantity} kg"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            ) {
                Text(
                    text = "Target Delivery Date: ",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
                Text(
                    text = specialRequest.targetDate
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Collection Method: ",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
                Text(
                    text = specialRequest.collectionMethod,
                )
            }
            if (specialRequest.collectionMethod == Constants.COLLECTION_DELIVERY) {
                Text(
                    text = "Delivery Location:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "Pick Up Location:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            }

            Text(
                text = specialRequest.deliveryAddress,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
            )

            Text(
                text = "Additional Request/s:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = specialRequest.additionalRequest.ifEmpty { "N/A" },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(2.dp)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

private fun updateSpecialRequestWithStatus(
    specialRequest: SpecialRequest,
    status: String,
    description: String,
    quantity: Int,
    imageUrl: String?,
    farmerEmail: String,
    product: String,
    formattedDateTime: String,
    trackRecord: MutableList<TrackRecord>,
    farmerTrackRecord: MutableList<TrackRecord>,
    specialRequestViewModel: SpecialRequestViewModel
) {
    specialRequest.assignedMember.map { member ->
        if (member.email == farmerEmail && member.product == product) {
            val addTrack = TrackRecord(
                description = "Farmer ${member.name} status: $status - $description",
                dateTime = formattedDateTime,
                imageUrl = imageUrl
            )
            val addFarmerTrack = TrackRecord(
                description = "In Progress ($status): $description",
                dateTime = formattedDateTime,
                imageUrl = imageUrl
            )
            farmerTrackRecord.add(addFarmerTrack)
            trackRecord.add(addTrack)
        }
    }

    specialRequestViewModel.updateSpecialRequest(
        specialRequest.copy(
            assignedMember = specialRequest.assignedMember.map { member ->
                if (member.email == farmerEmail && member.product == product) {
                    member.copy(
                        status = status,
                        farmerTrackRecord = farmerTrackRecord,
                        deliveredQuantity = quantity
                    )
                } else {
                    member
                }
            },
            trackRecord = trackRecord
        )
    )
}