package com.coco.celestia.util

import androidx.compose.ui.graphics.Color
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.LightOrange

fun bottomColorConfig(role: String): Pair<Color, Color> {
    return when (role) {
        "Admin" -> Pair(Color.White, DarkBlue)
        "Client" -> Pair(LightOrange, Color.White)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color(0xFFE0A83B), Color.White)
        else -> Pair(Color.White, Color.Black) // Default
    }
}