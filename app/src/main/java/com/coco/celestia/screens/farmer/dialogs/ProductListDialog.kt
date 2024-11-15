package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.ui.theme.*

@Composable
fun ProductListDialog(
    items: List<ProductData>,
    onDismiss: () -> Unit
) {
    val sortedItems = items.sortedByDescending { it.quantity }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "All Products",
                color = Cocoa,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedItems) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = item.name.replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                color = if (item.name.isEmpty()) Color.Gray else Cocoa,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var animationPlayed by remember { mutableStateOf(false) }
                                    val animatedWidth by animateDpAsState(
                                        targetValue = if (animationPlayed) (item.quantity.toFloat() / 3000.toFloat() * 200).dp else 0.dp,
                                        animationSpec = tween(durationMillis = 1000)
                                    )

                                    LaunchedEffect(Unit) {
                                        animationPlayed = true
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(if (item.name.isEmpty()) 0.dp else animatedWidth)
                                            .height(40.dp)
                                            .background(
                                                if (item.name.isEmpty()) Color.LightGray else SoftOrange,
                                                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                                            )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .background(
                                                Color.Gray.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                                            )
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 8.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 14.sp,
                                        color = if (item.name.isEmpty()) Color.Gray else Sand,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Cocoa)
            }
        },
        containerColor = LightApricot
    )
}