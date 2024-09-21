package com.coco.celestia.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.AddOrderPanel
import com.coco.celestia.AddProductForm
import com.coco.celestia.AdminDashboard
import com.coco.celestia.AdminInventory
import com.coco.celestia.AdminUserManagement
import com.coco.celestia.BottomNavigationBar
import com.coco.celestia.ClientContact
import com.coco.celestia.ClientDashboard
import com.coco.celestia.ClientOrder
import com.coco.celestia.ConfirmOrderRequestPanel
import com.coco.celestia.CoopDashboard
import com.coco.celestia.CoopInventory
import com.coco.celestia.CoopOrder
import com.coco.celestia.FarmerDashboard
import com.coco.celestia.FarmerInventory
import com.coco.celestia.FarmerManageOrder
import com.coco.celestia.FarmerProductTypeInventory
import com.coco.celestia.ForgotPasswordScreen
import com.coco.celestia.LoginScreen
import com.coco.celestia.OrderDetailsPanel
import com.coco.celestia.ProductTypeInventory
import com.coco.celestia.RegisterScreen
import com.coco.celestia.Screen
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.SplashScreen
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
        composable(route = Screen.Farmer.route) {
            FarmerDashboard()
        }
        composable(route = Screen.FarmerManageOrder.route) {
            FarmerManageOrder(
                mainNavController = navController,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel
            )
        }
        composable(route = Screen.FarmerInventory.route) {
            FarmerInventory(navController = navController)
        }
        composable(route = Screen.Client.route) {
            ClientDashboard()
        }
        composable(route = Screen.ClientOrder.route) {
            ClientOrder(
                navController = navController,
                orderViewModel = orderViewModel,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.ClientContact.route) {
            ClientContact(contactViewModel = contactViewModel)
        }
        composable(route = Screen.Admin.route) {
            AdminDashboard()
        }
        composable(route = Screen.AdminInventory.route) {
            AdminInventory(productViewModel = productViewModel)
        }
        composable(route = Screen.AdminUserManagement.route) {
            AdminUserManagement(userViewModel = userViewModel)
        }
        composable(route = Screen.Coop.route) {
            CoopDashboard()
        }
        composable(route = Screen.CoopOrder.route) {
            CoopOrder()
        }
        composable(route = Screen.CoopInventory.route) {
            CoopInventory(navController = navController)
        }
        composable(route = Screen.AddOrder.route) {
            AddOrderPanel(navController = navController)
        }
        composable(
            route = Screen.CoopProductInventory.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType })
        ) { backStack ->
            val type = backStack.arguments?.getString("type")
            ProductTypeInventory(
                navController = navController,
                type = type,
            )
        }
        composable(
            route = Screen.CoopAddProductInventory.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            AddProductForm(navController = navController, type = type)
        }
        composable(
            route = Screen.FarmerProductInventory.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType })
        ) { backStack ->
            val type = backStack.arguments?.getString("type")
            FarmerProductTypeInventory(
                navController = navController,
                type = type,
            )
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
        composable(route = Screen.Profile.route) {
            Profile(
                navController = navController,
                userViewModel = userViewModel,
                locationViewModel = locationViewModel
            )
        }
    }
}
