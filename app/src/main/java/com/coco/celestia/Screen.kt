package com.coco.celestia

sealed class Screen(val route: String) {
    object Login: Screen(route = "login")
    object Home: Screen(route = "home")
    object Register: Screen(route = "register")
    object AddOrder : Screen("add_order")
    object OrderDetails : Screen("add_order/{product}") {
        fun createRoute(product: String) = "add_order/$product"
    }
    object OrderConfirmation : Screen("add_order/{product}/{type}/{quantity}") {
        fun createRoute(product: String, type: String, quantity: Int) =
            "add_order/$product/$type/$quantity"
    }
    object Admin: Screen(route = "admin_dashboard")
    object Coop: Screen( route = "coop_dashboard")
    object CoopInventory: Screen( route = "coop_inventory")
    object CoopOrder: Screen( route = "coop_order")
    object Client: Screen( route = "client_dashboard")
    object ClientOrder: Screen( route = "client_order")
    object ClientContact: Screen( route = "client_contact")
    object Farmer: Screen( route = "farmer_dashboard")
    object FarmerInventory: Screen( route = "farmer_inventory")
    object FarmerManageOrder: Screen( route = "farmer_manage_order")
}

// TODO: Add more screens or separate them by making another Screen class here