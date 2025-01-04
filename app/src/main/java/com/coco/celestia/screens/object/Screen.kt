package com.coco.celestia.screens.`object`

sealed class Screen(val route: String) {
    data object Splash: Screen(route = "splash")
    data object Login: Screen(route = "login")
    data object ForgotPassword: Screen(route = "forgot_password")
    data object Register: Screen(route = "register")
    data object PrivacyPolicy: Screen(route = "privacy")
    data object Profile: Screen(route = "profile")
    data object Notifications: Screen(route = "notifications")
    data object Calendar: Screen(route = "calendar")
    data object AddOrder : Screen("add_order")
    data object OrderDetails : Screen("add_order/{type}") {
        fun createRoute(type: String) = "add_order/$type"
    }
    data object ClientOrderDetails : Screen("client_order_details/{orderId}") {
        fun createRoute(orderId: String) = "client_order_details/$orderId"
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
    data object AdminSpecialRequests: Screen( route = "admin_orders/{status}") {
        fun createRoute(status: String) = "admin_orders/$status"
    }
    data object AdminSettings: Screen( route = "admin_setting")
    data object AdminClients: Screen( route = "admin_clients")
    data object OrganizationProfile: Screen(route = "organization_profile")
    data object AccessControl: Screen(route = "access_control")
    data object AdminClientDetails : Screen(route = "client_details/{email}") {
        fun createRoute(email: String) = "client_details/$email"
    }
    //coop
    data object Coop: Screen( route = "coop_dashboard")
    data object CoopInventory: Screen( route = "coop_inventory")
    data object CoopInStoreProducts : Screen("coop_instore_products/{facilityName}") {
        fun createRoute(facilityName: String) = "coop_instore_products/$facilityName"
    }

    data object CoopOnlineProducts : Screen("coop_online_products/{facilityName}") {
        fun createRoute(facilityName: String) = "coop_online_products/$facilityName"
    }
    data object CoopOrder: Screen( route = "coop_order")
    data object CoopSales: Screen( route = "coop_sales")
    data object CoopReports: Screen( route = "coop_reports")
    data object CoopPurchases: Screen( route = "coop_purchases")
    data object CoopPurchaseForm : Screen("coop_purchase_form?draftId={draftId}") {
        fun createRouteWithDraft(draftId: String): String = "coop_purchase_form?draftId=$draftId"
        fun createRoute(): String = "coop_purchase_form"
    }
    data object CoopPurchaseDetails : Screen(route = "purchase_details/{purchaseNumber}") {
        fun createRoute(purchaseNumber: String) = "purchase_details/$purchaseNumber"
    }
    data object CoopVendors: Screen( route = "coop_vendors")
    data object CoopAddVendor: Screen( route = "add_vendor")
    data object CoopVendorDetails : Screen(route = "vendor_details/{email}") {
        fun createRoute(email: String) = "vendor_details/$email"
    }
    data object CoopProductInventory : Screen("coop_product_inventory/{type}") {
        fun createRoute(type: String) = "coop_product_inventory/$type"
    }
    data object AddProductInventory : Screen("add_product_inventory")
//    data object AddProductInventory: Screen("coop_add_product_inventory/{type}") {
//        fun createRoute(type: String) = "coop_add_product_inventory/$type"
//    }
    data object CoopAddProductInventoryDB: Screen("coop_add_product_inventory_db")
    data object CoopProcessOrder: Screen("coop_order/process/{orderId}") {
        fun createRoute(orderId: String) = "coop_order/process/${orderId}"
    }
    //client
    data object Client: Screen( route = "client_dashboard")
    data object ClientOrder: Screen( route = "client_order")
    data object ClientContact: Screen( route = "client_contact")
    data object ClientSpecialReq: Screen( route = "client_special_req")
    data object ClientAddSpecialReq: Screen( route = "client_add_special_req")
    object ProductDetails {
        const val route = "product_detail" // No arguments needed
    }
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