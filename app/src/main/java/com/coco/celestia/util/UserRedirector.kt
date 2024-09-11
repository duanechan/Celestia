package com.coco.celestia.util

import androidx.navigation.NavController
import com.coco.celestia.Screen

fun redirectUser(role: String, navController: NavController) {
    when (role) {
        "Farmer" -> navController.navigate(Screen.Farmer.route)
        "Client" -> navController.navigate(Screen.Client.route)
        "Admin" -> navController.navigate(Screen.Admin.route)
        "Coop" -> navController.navigate(Screen.Coop.route)
        // TODO: Handle unknown role
        else -> navController.navigate(Screen.Home.route)
    }
}