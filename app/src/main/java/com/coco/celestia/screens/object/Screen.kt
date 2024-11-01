package com.coco.celestia.screens.`object`

sealed class Screen(val route: String) {
    data object Splash: Screen(route = "splash")
    data object Login: Screen(route = "login")
    data object ForgotPassword: Screen(route = "forgot_password")
    data object Register: Screen(route = "register")
    data object Profile: Screen(route = "profile")
    data object Calendar: Screen(route = "calendar")
    data object Dashboard: Screen(route = "dashboard")
    data object AddOrder : Screen("add_order")
    data object OrderDetails : Screen("add_order/{type}") {
        fun createRoute(type: String) = "add_order/$type"
    }
    data object OrderConfirmation : Screen("add_order/{type}/{name}/{quantity}") {
        fun createRoute(type: String, name: String, quantity: Int) =
            "add_order/$type/$name/$quantity"
    }
    //admin
    data object Admin: Screen(route = "admin_dashboard")
    data object AdminInventory: Screen( route = "admin_inventory")
    data object AdminUserManagement: Screen( route = "admin_user_management")
    data object AdminAddUserManagement: Screen( route = "admin_add_user_management")
    data object AdminAddUserManagementDB: Screen( route = "admin_add_user_management_db")
    data object AdminUserManagementAuditLogs: Screen(route = "admin_add_user_management_logs")
    data object AdminAddProduct: Screen( route = "admin_add_product")
    data object AdminConfirmAddProduct: Screen( route = "admin_confirm_add_product")
    data object AdminProfile: Screen( route = "admin_profile")
    //coop
    data object Coop: Screen( route = "coop_dashboard")
    data object CoopInventory: Screen( route = "coop_inventory")
    data object CoopOrder: Screen( route = "coop_order")
    data object CoopProductInventory: Screen("coop_inventory/{type}") {
        fun createRoute(type: String) = "coop_inventory/$type"
    }
    data object AddProductInventory: Screen("coop_add_product_inventory/{type}") {
        fun createRoute(type: String) = "coop_add_product_inventory/$type"
    }
    data object CoopAddProductInventoryDB: Screen("coop_add_product_inventory_db")
    data object CoopProcessOrder: Screen("coop_order/process/{orderId}") {
        fun createRoute(orderId: String) = "coop_order/process/${orderId}"
    }
    //client
    data object Client: Screen( route = "client_dashboard")
    data object ClientOrder: Screen( route = "client_order")
    data object ClientContact: Screen( route = "client_contact")
    data object Cart: Screen(route = "client/cart")
    data object CheckoutEvent: Screen(route = "client/cart/checkout")
    //farmer
    data object Farmer: Screen( route = "farmer_dashboard")
    data object FarmerManageOrder: Screen( route = "farmer_manage_order")
    data object FarmerItems: Screen( route = "farmer_items")
    data object FarmerTransactions: Screen( route = "farmer_transactions")
    data object FarmerAddProduct: Screen(route = "farmer_add_product")
    data object FarmerItemDetails : Screen(route = "farmer_item_detail/{productName}") {
        fun createRoute(type: String) = "farmer_item_detail/$type"
    }
    data object FarmerProductInventory: Screen("farmer_inventory/{type}") {
        fun createRoute(type: String) = "farmer_inventory/$type"
    }
    data object FarmerRequestDetails : Screen(route = "farmer_request_details/{orderId}") {
        fun createRoute(type: String) = "farmer_request_details/$type"
    }
    data object FarmerOrderDetails : Screen(route = "farmer_order_details/{orderId}") {
        fun createRoute(orderId: String) = "farmer_order_details/$orderId"
    }
}