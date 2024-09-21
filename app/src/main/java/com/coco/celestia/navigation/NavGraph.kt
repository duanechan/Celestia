package com.coco.celestia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.*
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.SplashScreen
import com.coco.celestia.viewmodel.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    onAddProduct: (String) -> Unit,
    onSaveProduct: (String, String, String, Int) -> Unit,
    contactViewModel: ContactViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
) {
    var productName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var addressName by remember { mutableStateOf("") }
    var quantityAmount by remember { mutableIntStateOf(0) }

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
                orderViewModel = orderViewModel,
                productViewModel = productViewModel
            )
        }
        composable(route = Screen.FarmerInventory.route) {
            FarmerInventory(navController = navController)
        }
        composable(Screen.FarmerManageOrderRequest.route) {
            ManageOrderRequest()
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
            OrderRequestPanel()
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
                type = type
            )
        }
        composable(route = Screen.CoopAddProductInventory.route) {
            AddProductForm(
                productName = productName,
                farmerName = farmerName,
                address = addressName,
                quantity = quantityAmount,
                onProductNameChange = { productName = it },
                onFarmerNameChange = { farmerName = it },
                onAddressChange = { addressName = it },
                onQuantityChange = { newValue -> quantityAmount = newValue.toIntOrNull() ?: 0 }
            )
        }
        composable(
            route = Screen.FarmerProductInventory.route,
            arguments = listOf(
                navArgument("productName") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName")
            val quantity = backStackEntry.arguments?.getInt("quantity")
            val product = ProductData(name = productName ?: "", quantity = quantity ?: 0)

            FarmerProductTypeInventory(product = product)
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
                navArgument("quantity") { type = NavType.IntType }
            )
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
