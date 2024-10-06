package com.coco.celestia.screens.`object`

import com.coco.celestia.viewmodel.model.OrderData

sealed class Screen(val route: String) {
    object Splash: Screen(route = "splash")
    object Login: Screen(route = "login")
    object ForgotPassword: Screen(route = "forgot_password")
    object Register: Screen(route = "register")
    object Profile: Screen(route = "profile")
    object AddOrder : Screen("add_order")
    object OrderDetails : Screen("add_order/{type}") {
        fun createRoute(type: String) = "add_order/$type"
    }
    object OrderConfirmation : Screen("add_order/{type}/{name}/{quantity}") {
        fun createRoute(type: String, name: String, quantity: Int) =
            "add_order/$type/$name/$quantity"
    }
    //admin
    object Admin: Screen(route = "admin_dashboard")
    object AdminInventory: Screen( route = "admin_inventory")
    object AdminUserManagement: Screen( route = "admin_user_management")
    object AdminAddUserManagement: Screen( route = "admin_add_user_management")
    object AdminAddUserManagementDB: Screen( route = "admin_add_user_management_db")
    object AdminProfile: Screen( route = "admin_profile")
    //coop
    object Coop: Screen( route = "coop_dashboard")
    object CoopInventory: Screen( route = "coop_inventory")
    object CoopOrder: Screen( route = "coop_order")
    object CoopProductInventory: Screen("coop_inventory/{type}") {
        fun createRoute(type: String) = "coop_inventory/$type"
    }
    object CoopAddProductInventory: Screen("coop_add_product_inventory/{type}") {
        fun createRoute(type: String) = "coop_add_product_inventory/$type"
    }
    object CoopAddProductInventoryDB: Screen("coop_add_product_inventory_db")
    object CoopProcessOrder: Screen("coop_order/process") {
        fun createRoute(order: OrderData) = "coop_order/process/${order}"
    }
    //client
    object Client: Screen( route = "client_dashboard")
    object ClientOrder: Screen( route = "client_order")
    object ClientContact: Screen( route = "client_contact")
    object Cart: Screen(route = "client/cart")
    object CheckoutEvent: Screen(route = "client/cart/checkout")
    //farmer
    object Farmer: Screen( route = "farmer_dashboard")
    object FarmerManageOrder: Screen( route = "farmer_manage_order")
    object FarmerItems: Screen( route = "farmer_items")
    object FarmerItemDetails : Screen(route = "farmer_item_detail/{productName}") {
        fun createRoute(type: String) = "farmer_item_detail/$type"
    }
    object FarmerProductInventory: Screen("farmer_inventory/{type}") {
        fun createRoute(type: String) = "farmer_inventory/$type"
    }
    object FarmerRequestDetails : Screen(route = "farmer_request_details/{orderId}") {
        fun createRoute(type: String) = "farmer_request_details/$type"
    }
    object FarmerOrderDetails : Screen(route = "farmer_order_details/{orderId}") {
        fun createRoute(type: String) = "farmer_order_details/$type"
    }
}