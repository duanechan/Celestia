package com.coco.celestia.util

import com.coco.celestia.navigation.Routes
import com.coco.celestia.screens.`object`.Screen

fun routeHandler(userRole: String) : Routes {
    val dashboardRoute = when (userRole) {
        "Admin" -> Screen.Admin.route
        "Client" -> Screen.Client.route
        "Coop" -> Screen.Coop.route
        "CoopCoffee" -> Screen.Coop.route
        "CoopMeat" -> Screen.Coop.route
        "Farmer" -> Screen.Farmer.route
        else -> ""
    }
    val ordersRoute = when (userRole) {
        "Client" -> Screen.ClientOrder.route
        "Coop" -> Screen.CoopOrder.route
        "CoopCoffee" -> Screen.CoopOrder.route
        "CoopMeat" -> Screen.CoopOrder.route
        "Farmer" -> Screen.FarmerManageOrder.route
        else -> ""
    }
    val inventoryRoute = when (userRole) {
        "Admin" -> Screen.AdminInventory.route
        "Coop" -> Screen.CoopInventory.route
        "CoopCoffee" -> Screen.CoopInventory.route
        "CoopMeat" -> Screen.CoopInventory.route
        "Farmer" -> Screen.FarmerItems.route
        else -> ""
    }
    return Routes(
        dashboard = dashboardRoute,
        orders = ordersRoute,
        inventory = inventoryRoute
    )
}