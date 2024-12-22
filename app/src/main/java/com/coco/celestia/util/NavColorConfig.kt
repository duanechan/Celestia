package com.coco.celestia.util

import androidx.compose.ui.graphics.Color
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.DarkGreen
import com.coco.celestia.ui.theme.LightOrange

// Aliases for clarity
typealias ContainerColor = Color
typealias TextColor = Color

// Top bar colors
fun topColorConfig(role: String): Pair<TextColor, ContainerColor> {
    return when (role) {
        "Admin" -> Pair(Color.White, DarkBlue)
        "Client" -> Pair(LightOrange, Color.White)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color(0xFFE0A83B), Color.White)
        else -> Pair(Color.White, Color.Black) // Default
    }
}

// Bottom bar colors
fun bottomColorConfig(role: String): Pair<TextColor, ContainerColor> {
    return when (role) {
        "Admin" -> Pair(Color.White, DarkBlue)
        "Client" -> Pair(LightOrange, Color.White)
        "Coop" -> Pair(Color.White, DarkGreen)
        "Farmer" -> Pair(Color(0xFFE0A83B), Color.White)
        else -> Pair(Color.White, Color.Black) // Default
    }
}