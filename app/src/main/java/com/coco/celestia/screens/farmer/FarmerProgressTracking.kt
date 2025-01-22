package com.coco.celestia.screens.farmer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
fun DisplayFarmerProgressTracking (
    trackingID: String,
    specialRequestViewModel: SpecialRequestViewModel
) {
    val assignedData by specialRequestViewModel.assignedData.observeAsState()

    LaunchedEffect(Unit) {
        specialRequestViewModel.fetchRequestByTrackingID(trackingID)
    }

    Column {
        Text(assignedData?.name ?: "")
        Text("Tracking No. ${assignedData?.trackingID}")
        assignedData?.farmerTrackRecord
            ?.sortedByDescending { it.dateTime }
            ?.forEachIndexed { index, trackRecord ->
                DisplayTrackOrder(
                    trackRecord,
                    index == assignedData!!.farmerTrackRecord.lastIndex
                )
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