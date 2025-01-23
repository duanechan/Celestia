package com.coco.celestia.screens.farmer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green2
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.model.TrackRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DisplayFarmerProgressTracking(
    trackingID: String,
    specialRequestViewModel: SpecialRequestViewModel
) {
    val assignedData by specialRequestViewModel.assignedData.observeAsState()

    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchRequestByTrackingID(trackingID)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Green4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = assignedData?.name ?: "Loading...",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tracking No. ${assignedData?.trackingID ?: ""}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        assignedData?.farmerTrackRecord
            ?.sortedByDescending { it.dateTime }
            ?.forEachIndexed { index, trackRecord ->
                DisplayTrackOrder(
                    record = trackRecord,
                    isLastItem = index == assignedData!!.farmerTrackRecord.lastIndex
                )
            }
    }
}

@Composable
fun DisplayTrackOrder(
    record: TrackRecord,
    isLastItem: Boolean
) {
    val inputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val dateTime = LocalDateTime.parse(record.dateTime, inputFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val date = dateTime.format(dateFormatter)
    val time = dateTime.format(timeFormatter)

    var cardHeight by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
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

        Column(
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
                        end = Offset(size.width / 2, size.height + cardHeight.toFloat()),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .wrapContentWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    cardHeight = size.height
                }
        ) {
            Card(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = record.description,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}