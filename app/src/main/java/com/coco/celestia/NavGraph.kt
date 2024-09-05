package com.coco.celestia

import android.view.SurfaceControl.Transaction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    contactViewModel: ContactViewModel = viewModel(),
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
            ForgotPasswordScreen(navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(route = Screen.Farmer.route) {
            FarmerDashboard()
            FarmerNavDrawer(navController, userViewModel, orderViewModel)
        }
        composable(route = Screen.Client.route) {
            ClientDashboard()
            ClientNavDrawer(navController, userViewModel, orderViewModel)
        }
        composable(route = Screen.Admin.route) {
            AdminDashboard()
            AdminNavDrawer(navController)
        }
        composable(route = Screen.Coop.route) {
            CoopDashboard()
            CoopNavDrawer(navController)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(route = Screen.AddOrder.route) {
            AddOrderPanel(navController)
        }
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
