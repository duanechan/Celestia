package com.coco.celestia.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Theme's Palette
val Orange = Color(0xFFE86A33)
val LightGreen = Color(0xFF41644A)
val DarkGreen = Color(0xFF263A29)
val BgColor = Color(0xFFF2E3DB)
val DarkBlue = Color(0xFF292B4D)
val Gray = Color(0xFF3E3E3E)

//Client Palette
val LightOrange = Color(0xFFDB873A) //navbar
val LightGray = Color (0xFFE5E5E4)  //client bg
val VeryDarkGreen = Color (0xFF52614E) //client elements
val VeryDarkPurple = Color (0xFF5D4E61) //client notification and searchbar
val RavenBlack = Color (0xFF3e3e3e) //TEXT IN CLIENT
val CoffeeBean = Color (0xFF362e26) //ICON COLORS
val TreeBark = Color (0xFFB06520) //CARD IN ORDER DETAILS

// Toast Palette
val JadeGreen = Color(0xFF00bb77)
val Cinnabar = Color(0xFFe84b3d)
val MustardYellow = Color(0xFFff9900)
val RoyalBlue = Color(0xFF4169e1)

//Farmer Palette
val LightApricot = Color(0xFFFFE7BF)
val PaleGold = Color(0xFFFFC978)
val SageGreen = Color(0xFFC9C987)
val Sand = Color(0xFFD1A664)
val Copper = Color(0xFFC27B57)
val Cocoa = Color(0xFF88563d)
val SoftOrange = Color(0xFFFFCE86)
val GoldenYellow = Color(0xFFE6B979)
val Yellow4 = Color(0xFFE6B962)
val Brown1 = Color(0xFFDFC193)
val Copper3 = Color(0xFFDAB09A)
val Apricot = Color(0xFFF6E5C4)
val Apricot2 = Color(0xFFE9C276)

// Coop WhiteBG
val CoopBackground = Color(0xFFF4F4F4)

// Coop Items Palette
val GreenBeans = Color(0xFF5A8F5C)
val RoastedBeans = Color(0xFF362E26)
val Packed = Color(0xFFB06520)
val Sorted = Color(0xFF3E3E3E)
val Ordered = Color(0xFFE0A83B)
val DeliveredItem = Color(0xFF01A69E)
val Kiniing = Color(0xFFE86A33)
val RawMeat = Color(0xFFFB4949)

// Coop Order Palette
val PendingStatus = Color(0xFFDB873A)
val PreparingStatus = Color(0xFFE86A33)
val DeliveringStatus = Color(0xFF01A69E)
val CompletedStatus = Color(0xFF7CC659)

// Gradient Brush
val GreenGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF83CA95),
        Color(0xFF41644A)
    ),
    start = Offset(0f, 0f),
    end = Offset(500f, 0f)
)