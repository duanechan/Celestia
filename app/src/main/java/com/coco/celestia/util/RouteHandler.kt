package com.coco.celestia.util

import com.coco.celestia.navigation.Routes
import com.coco.celestia.screens.`object`.Screen

fun routeHandler(userRole: String): Routes {
    val dashboardRoute = when {
        userRole == "Admin" -> Screen.Admin.route
        userRole == "Client" -> Screen.Client.route
        userRole == "Farmer" -> Screen.Farmer.route
        userRole.startsWith("Coop") -> Screen.Coop.route
        else -> Screen.Login.route
    }

    val ordersRoute = when {
        userRole == "Client" -> Screen.ClientOrder.route
        userRole == "Farmer" -> Screen.FarmerManageOrder.route
        userRole.startsWith("Coop") -> Screen.CoopOrder.route
        else -> dashboardRoute
    }

    val inventoryRoute = when {
        userRole == "Admin" -> Screen.AdminInventory.route
        userRole == "Farmer" -> Screen.FarmerItems.route
        userRole.startsWith("Coop") -> Screen.CoopInventory.route
        else -> dashboardRoute
    }

    return Routes(
        dashboard = dashboardRoute,
        orders = ordersRoute,
        inventory = inventoryRoute
    )
}