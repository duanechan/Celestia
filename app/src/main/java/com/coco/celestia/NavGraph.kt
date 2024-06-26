package com.coco.celestia

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddOrderNav(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AddOrder.route
    ) {
        composable(Screen.AddOrder.route) { AddOrderPanel(navController) }
        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(navArgument("product") { type = NavType.StringType })
        ) { backStack ->
            val product = backStack.arguments?.getString("product")
            OrderDetailsPanel(navController, product)
        }
        composable(
            route = Screen.OrderConfirmation.route,
            arguments = listOf(
                navArgument("product") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType })
        ) { backStack ->
            val product = backStack.arguments?.getString("product")
            val type = backStack.arguments?.getString("type")
            val quantity = backStack.arguments?.getInt("quantity")
            ConfirmOrderRequestPanel(navController, product, type, quantity)
        }
    }
}