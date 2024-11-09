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
    onConfirm: (plantingDate: String, durationInDays: Int) -> Unit
) {
    val context = LocalContext.current
    var plantingDate by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(1) }
    var selectedUnit by remember { mutableStateOf("Days") }
    var plantingDateError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf(false) }
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
                    plantingDateError = plantingDate.isBlank()
                    durationError = duration <= 0

                    if (!plantingDateError && !durationError) {
                        val durationInDays = when (selectedUnit) {
                            "Weeks" -> duration * 7
                            "Months" -> duration * 30
                            else -> duration
                        }
                        println("Farmer's Name: $farmerName")
                        onConfirm(plantingDate, durationInDays)
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
                        .clickable { datePickerDialog.show() }
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
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date", tint = Cocoa)
                        }
                    }
                )
                if (plantingDateError) {
                    Text("Please select a planting date.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
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
                                onValueChange = { newValue ->
                                    val number = newValue.filter { it.isDigit() }
                                    if (number.isNotEmpty()) {
                                        number.toIntOrNull()?.let {
                                            if (it in 1..999) duration = it
                                        }
                                    }
                                },
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
                                    onClick = { duration = (duration + 1).coerceAtMost(999) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                        tint = Cocoa
                                    )
                                }
                                IconButton(
                                    onClick = { duration = (duration - 1).coerceAtLeast(1) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decrease",
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
                                IconButton(onClick = { isUnitMenuExpanded = true }) {
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Cocoa.copy(alpha = 0.7f))
                        )

                        DropdownMenu(
                            expanded = isUnitMenuExpanded,
                            onDismissRequest = { isUnitMenuExpanded = false },
                            modifier = Modifier.width(120.dp)
                        ) {
                            durationUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit, color = Cocoa.copy(alpha = 0.7f)) },
                                    onClick = {
                                        selectedUnit = unit
                                        isUnitMenuExpanded = false
                                    }
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
    )
}