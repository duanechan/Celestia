package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import com.coco.celestia.ui.theme.*

@Composable
fun FarmerPlanHarvestDialog(
    farmerName: String,
    onDismiss: () -> Unit,
    onConfirm: (plantingDate: String, durationInDays: Int, quantity: Int) -> Unit
) {
    val context = LocalContext.current
    var plantingDate by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(1) }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedUnit by remember { mutableStateOf("Days") }
    var plantingDateError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var isUnitMenuExpanded by remember { mutableStateOf(false) }
    val durationUnits = listOf("Days", "Weeks", "Months")

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            plantingDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(selectedDate.time)
            plantingDateError = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(
                color = BgColor,
                shape = RoundedCornerShape(16.dp)
            ),
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        containerColor = Sand2,
        shape = RoundedCornerShape(16.dp),
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        confirmButton = {
            TextButton(
                onClick = {
                    println("Submitting harvest plan for farmer: $farmerName")
                    plantingDateError = plantingDate.isBlank()
                    durationError = duration <= 0
                    quantityError = quantity <= 0

                    if (!plantingDateError && !durationError && !quantityError) {
                        val durationInDays = when (selectedUnit) {
                            "Weeks" -> duration * 7
                            "Months" -> duration * 30
                            else -> duration
                        }
                        onConfirm(plantingDate, durationInDays, quantity)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm", color = OliveGreen)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = Copper)
            }
        },
        title = {
            Text(
                text = "Plan Harvest",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Cocoa,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            HarvestPlanContent(
                plantingDate = plantingDate,
                duration = duration,
                quantity = quantity,
                selectedUnit = selectedUnit,
                plantingDateError = plantingDateError,
                durationError = durationError,
                quantityError = quantityError,
                isUnitMenuExpanded = isUnitMenuExpanded,
                durationUnits = durationUnits,
                onPlantingDateClick = { datePickerDialog.show() },
                onQuantityChange = { newValue ->
                    val number = newValue.filter { it.isDigit() }
                    if (number.isNotEmpty()) {
                        number.toIntOrNull()?.let {
                            if (it in 1..9999) quantity = it
                        }
                    }
                },
                onQuantityIncrement = { quantity = (quantity + 1).coerceAtMost(9999) },
                onQuantityDecrement = { quantity = (quantity - 1).coerceAtLeast(1) },
                onDurationChange = { newValue ->
                    val number = newValue.filter { it.isDigit() }
                    if (number.isNotEmpty()) {
                        number.toIntOrNull()?.let {
                            if (it in 1..999) duration = it
                        }
                    }
                },
                onDurationIncrement = { duration = (duration + 1).coerceAtMost(999) },
                onDurationDecrement = { duration = (duration - 1).coerceAtLeast(1) },
                onUnitMenuExpandedChange = { isUnitMenuExpanded = it },
                onUnitSelected = { unit ->
                    selectedUnit = unit
                    isUnitMenuExpanded = false
                }
            )
        }
    )
}

@Composable
private fun HarvestPlanContent(
    plantingDate: String,
    duration: Int,
    quantity: Int,
    selectedUnit: String,
    plantingDateError: Boolean,
    durationError: Boolean,
    quantityError: Boolean,
    isUnitMenuExpanded: Boolean,
    durationUnits: List<String>,
    onPlantingDateClick: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onQuantityIncrement: () -> Unit,
    onQuantityDecrement: () -> Unit,
    onDurationChange: (String) -> Unit,
    onDurationIncrement: () -> Unit,
    onDurationDecrement: () -> Unit,
    onUnitMenuExpandedChange: (Boolean) -> Unit,
    onUnitSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Planting Date
        Text(text = "Planting Date", fontWeight = FontWeight.Bold, color = Cocoa)
        OutlinedTextField(
            value = plantingDate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlantingDateClick() }
                .background(
                    color = Apricot,
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text("Select planting date", color = Cocoa.copy(alpha = 0.7f)) },
            isError = plantingDateError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
            ),
            trailingIcon = {
                IconButton(onClick = onPlantingDateClick) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date", tint = Cocoa)
                }
            }
        )
        if (plantingDateError) {
            Text("Please select a planting date.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quantity
        Text(text = "Enter Quantity to Plant (Kg)", fontWeight = FontWeight.Bold, color = Cocoa)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(
                        color = Apricot,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = quantity.toString(),
                        onValueChange = onQuantityChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Cocoa.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )
                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .fillMaxHeight()
                    ) {
                        IconButton(
                            onClick = onQuantityIncrement,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Increase quantity",
                                tint = Cocoa
                            )
                        }
                        IconButton(
                            onClick = onQuantityDecrement,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Decrease quantity",
                                tint = Cocoa
                            )
                        }
                    }
                }
            }
        }
        if (quantityError) {
            Text("Please enter a valid quantity.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duration
        Text(text = "Enter Duration", fontWeight = FontWeight.Bold, color = Cocoa)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(
                        color = Apricot,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = duration.toString(),
                        onValueChange = onDurationChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Cocoa.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )
                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .fillMaxHeight()
                    ) {
                        IconButton(
                            onClick = onDurationIncrement,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Increase duration",
                                tint = Cocoa
                            )
                        }
                        IconButton(
                            onClick = onDurationDecrement,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Decrease duration",
                                tint = Cocoa
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.width(120.dp)
            ) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { onUnitMenuExpandedChange(true) }) {
                            Icon(Icons.Default.ArrowDropDown, "Select unit", tint = Cocoa)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Apricot,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Cocoa.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = isUnitMenuExpanded,
                    onDismissRequest = { onUnitMenuExpandedChange(false) }
                ) {
                    durationUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit, color = Cocoa) },
                            onClick = { onUnitSelected(unit) }
                        )
                    }
                }
            }
        }
        if (durationError) {
            Text("Please enter a valid duration.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
    }
}