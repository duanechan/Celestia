package com.coco.celestia.util

import androidx.compose.ui.graphics.Color
import com.coco.celestia.ui.theme.CompletedStatus
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.ui.theme.PendingStatus
import com.coco.celestia.ui.theme.PreparingStatus

fun orderStatusConfig(status: String): Color {
    return when (status) {
        "PENDING" -> PendingStatus
        "PREPARING" -> PreparingStatus
        "DELIVERING" -> DeliveringStatus
        "COMPLETED" -> CompletedStatus
        else -> Color.Gray
    }
}