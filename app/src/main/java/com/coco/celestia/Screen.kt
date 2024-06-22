package com.coco.celestia

sealed class Screen(val route: String) {
    object Login: Screen(route = "login")
    object Home: Screen(route = "home")
    object Register: Screen(route = "register")
    object AddOrder : Screen("add_order")
    object OrderDetails : Screen("add_order/{productType}") {
        fun createRoute(productType: ProductType) = "add_order/$productType"
    }
    object OrderConfirmation : Screen("add_order/{productType}/{orderType}") {
        fun createRoute(productType: ProductType, orderType: Int) = "add_order/$productType/$orderType"
    }
}

// TODO: Add more screens or separate them by making another Screen class here