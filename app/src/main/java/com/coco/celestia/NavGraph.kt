package com.coco.celestia

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AddOrderNav(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AddOrder.route
    ) {
        composable(Screen.AddOrder.route) { AddOrderPanel(navController) }
        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(navArgument("productType") { type = NavType.StringType })
        ) { backStack ->
            val productType = backStack.arguments?.getString("productType")
                ?.let { ProductType.valueOf(it) }
            OrderDetailsPanel(navController, productType)
        }
        composable(
            route = Screen.OrderConfirmation.route,
            arguments = listOf(navArgument("orderType") { type = NavType.IntType })
        ) { backStack ->
            val orderType = backStack.arguments?.getInt("orderType")
            ConfirmOrderRequestPanel(navController, orderType)
        }
    }
}