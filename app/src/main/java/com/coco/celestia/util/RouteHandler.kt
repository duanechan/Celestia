package com.coco.celestia.util

import com.coco.celestia.Routes
import com.coco.celestia.Screen

fun routeHandler(userRole: String) : Routes {
    val dashboardRoute = when (userRole) {
        "Admin" -> Screen.Admin.route
        "Client" -> Screen.Client.route
        "Coop" -> Screen.Coop.route
        "Farmer" -> Screen.Farmer.route
        else -> ""
    }
    val ordersRoute = when (userRole) {
        "Client" -> Screen.ClientOrder.route
        "Coop" -> Screen.CoopOrder.route
        "Farmer" -> Screen.FarmerManageOrder.route
        else -> ""
    }
    val inventoryRoute = when (userRole) {
        "Admin" -> Screen.AdminInventory.route
        "Coop" -> Screen.CoopInventory.route
        "Farmer" -> Screen.FarmerInventory.route
        else -> ""
    }
    return Routes(
        dashboard = dashboardRoute,
        orders = ordersRoute,
        inventory = inventoryRoute
    )
}