package com.coco.celestia

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.screens.Profile
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    contactViewModel: ContactViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                mainNavController = navController,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.Profile.route) {
            Profile(
                navController = navController,
                userViewModel = userViewModel,
                locationViewModel = locationViewModel
            )
        }

        composable(route = Screen.Farmer.route) {
            FarmerDashboard()
            FarmerNavDrawer(
                navController = navController,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel
            )
        }
        composable(route = Screen.Client.route) {
            ClientDashboard()
            ClientNavDrawer(
                navController = navController,
                contactViewModel = contactViewModel,
                locationViewModel = locationViewModel,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel
            )
        }
        composable(route = Screen.Admin.route) {
            AdminDashboard()
            AdminNavDrawer(
                mainNavController = navController,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.Coop.route) {
            CoopDashboard()
            CoopNavDrawer(
                mainNavController = navController,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.AddOrder.route) {
            AddOrderPanel(navController = navController)
        }
        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStack ->
            val type = backStack.arguments?.getString("type")
            OrderDetailsPanel(
                navController = navController,
                type = type,
                productViewModel = productViewModel
            )
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
            ConfirmOrderRequestPanel(
                navController = navController,
                type = type,
                name = name,
                quantity = quantity,
                orderViewModel = orderViewModel,
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel
            )
        }
    }
}
