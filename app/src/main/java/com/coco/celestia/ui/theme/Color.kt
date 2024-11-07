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
val Gray = Color(0xFF3E3E3E)
val BrownCoffee2 = Color(0xFF633E06)

//Profile Palette
val GrayTextField = Color(0XFFdcdcdc)

//Client Palette
val LightOrange = Color(0xFFDA8359) //top bar
val ClientBG = Color (0xFFECDFCC)  //client bg
val ContainerLO = Color (0xFFf9cb9c) //container bg in client dashboard
val CLightGreen = Color (0xFFA5B68D) //order card
val LGContainer = Color (0xff93c47d) //filter and notif
val CLGText = Color (0xFFF5B971) //order status color in client order
val BBGreen = Color (0xFF6A9C89) //icon color bottom bar
val RavenBlack = Color (0xFF3e3e3e) //TEXT IN CLIENT

// Toast Palette
val JadeGreen = Color(0xFF00bb77)
val Cinnabar = Color(0xFFe84b3d)
val MustardYellow = Color(0xFFff9900)
val RoyalBlue = Color(0xFF4169e1)

//Farmer Palette
val LightApricot = Color(0xFFFFE7BF)
val PaleGold = Color(0xFFFFC978)
val SageGreen = Color(0xFFcece93)
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
val Tangerine = Color(0xFFe9b986)
val OliveGreen = Color(0xFF8d8d5f)
val Blue = Color(0xFFb9cecf)
val Sand2 = Color(0xFFd6af74)

// Coop WhiteBG
val CoopBackground = Color(0xFFFDFDFD)

// Coop Lighter Green Palette
val LightDarkGreen = Color(0xFF658A6E)
val LightBlueGreen = Color(0xFF96EAD7)

// Coop Items Palette
val GreenBeans = Color(0xFF5A8F5C)
val RoastedBeans = Color(0xFF362E26)
val Packed = Color(0xFFB06520)
val Sorted = Color(0xFF3E3E3E)
val Ordered = Color(0xFF47DEB1)
val DeliveredItem = Color(0xFF09D1C7)
val Kiniing = Color(0xFFE86A33)
val RawMeat = Color(0xFFFB4949)

// Coop Order Palette
val PendingStatus = Color(0xFF0C6478)
val PreparingStatus = Color(0xFF16909B)
val DeliveringStatus = Color(0xFF09D1C7)
val CompletedStatus = Color(0xFF82EE99)

// Admin Palette
val DarkBlue = Color(0xFF373c71)
val LightBlue = Color(0xFF40458d)
val PaleBlue = Color(0xFFced7ef)
val BrownCoffee = Color(0xFF795548)
val RedMeat = Color(0xFFFB4949)

// Coop Gradient Brush
val GreenGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF9FFEB0),
        Color(0xFF16909B)
    ),
    start = Offset(1000f, -500f),
    end = Offset(0f, 500f)
)

val ButtonGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF9FFEB0),
        Color(0xFF16909B)
    ),
    start = Offset(1000f, -500f),
    end = Offset(0f, 500f)
)

//Client Profile 
val OrangeGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFDA8359),
        Color(0xFFE9C4A0)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, 800f)
)

//Admin Profile
val BlueGradientBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF40458d),
        Color(0xFF4851ad)
 )
    //    start = Offset(0f, 0f),
//    end = Offset(500f, 0f)
)

//Farmer Profile
val FarmerGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE6B962),
        Color(0xfff2daad)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, 800f)
)

//Gray
val GrayGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x80FFFFFF),
        Color(0x80000000)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, 500f)
)
