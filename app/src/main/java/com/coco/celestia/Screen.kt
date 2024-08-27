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
    object Supplier: Screen( route = "supplier_dashboard")
    object SupplierInventory: Screen( route = "supplier_inventory")
    object SupplierOrder: Screen( route = "supplier_order")
    object Client: Screen( route = "client_dashboard")
    object ClientOrder: Screen( route = "client_order")
    object ClientContact: Screen( route = "client_contact")
}

// TODO: Add more screens or separate them by making another Screen class here