package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import com.coco.celestia.R
import java.time.Month
import java.util.Locale
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.model.ProductData
import java.time.LocalDate

@Composable
fun InSeasonProductListDialog(
    products: List<ProductData>,
    onDismiss: () -> Unit
) {
    val currentMonth = LocalDate.now().month

//    val inSeasonProducts = products.filter { product ->
//        val sanitizedStartSeason = product.startSeason.trim().uppercase(Locale.ROOT)
//        val sanitizedEndSeason = product.endSeason.trim().uppercase(Locale.ROOT)
//
//        val startMonth = try {
//            Month.valueOf(sanitizedStartSeason)
//        } catch (e: IllegalArgumentException) { return@filter false }
//
//        val endMonth = try {
//            Month.valueOf(sanitizedEndSeason)
//        } catch (e: IllegalArgumentException) { return@filter false }
//
//        when {
//            startMonth.value <= endMonth.value -> {
//                currentMonth.value in startMonth.value..endMonth.value
//            }
//            else -> {
//                currentMonth.value >= startMonth.value || currentMonth.value <= endMonth.value
//            }
//        }
//    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            shape = RoundedCornerShape(12.dp),
            color = LightApricot
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "In Season Products",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        items(inSeasonProducts) { product ->
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 8.dp)
//                            ) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(60.dp)
//                                        .background(color = SoftOrange, shape = CircleShape),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Image(
//                                        painter = painterResource(id = R.drawable.plant),
//                                        contentDescription = "Plant Image",
//                                        modifier = Modifier.size(50.dp),
//                                        colorFilter = ColorFilter.tint(OliveGreen)
//                                    )
//                                }
//                                Spacer(modifier = Modifier.width(20.dp))
//                                Text(
//                                    text = product.name,
//                                    fontSize = 20.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Cocoa,
//                                    modifier = Modifier.weight(1f)
//                                )
//                            }
//                        }
//                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Close",
                            color = Cocoa,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}