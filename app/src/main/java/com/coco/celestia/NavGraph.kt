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
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStack ->
            val type = backStack.arguments?.getString("type")
            OrderDetailsPanel(navController, type)
        }
        composable(
            route = Screen.OrderConfirmation.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType })
        ) { backStack ->
            val type = backStack.arguments?.getString("type")
            val name = backStack.arguments?.getString("name")
            val quantity = backStack.arguments?.getInt("quantity")
            ConfirmOrderRequestPanel(navController, type, name, quantity)
        }
    }
}
