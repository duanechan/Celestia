package com.coco.celestia.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.*
import com.coco.celestia.screens.admin.AdminDashboard
import com.coco.celestia.screens.admin.AdminUserManagement
import com.coco.celestia.screens.client.ClientDashboard
import com.coco.celestia.screens.ForgotPasswordScreen
import com.coco.celestia.screens.LoginScreen
import com.coco.celestia.screens.coop.OrderRequest
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.RegisterScreen
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.screens.SplashScreen
import com.coco.celestia.screens.admin.AddUserForm
import com.coco.celestia.screens.admin.AdminInventory
import com.coco.celestia.screens.client.ClientContact
import com.coco.celestia.screens.client.ClientOrder
import com.coco.celestia.screens.coop.AddProductForm
import com.coco.celestia.screens.coop.CoopDashboard
import com.coco.celestia.screens.coop.CoopInventory
import com.coco.celestia.screens.coop.ProcessOrderPanel
import com.coco.celestia.screens.coop.ProductTypeInventory
import com.coco.celestia.screens.farmer.FarmerDashboard
import com.coco.celestia.screens.farmer.FarmerInventory
import com.coco.celestia.screens.farmer.FarmerInventoryDetail
import com.coco.celestia.screens.farmer.FarmerManageOrder
import com.coco.celestia.screens.farmer.FarmerProductTypeInventory
import com.coco.celestia.screens.farmer.FarmerRequestDetails
import com.coco.celestia.viewmodel.*
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData

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
    var productName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var addressName by remember { mutableStateOf("") }
    var quantityAmount by remember { mutableIntStateOf(0) }
    var productType by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

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
                navController = navController,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel,
                productViewModel = productViewModel
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
            AdminUserManagement(
                userViewModel = userViewModel,
                navController = navController
            )
        }
        composable(route = Screen.AdminAddUserManagement.route) {
            AddUserForm(
                email = email,
                role = role,
                newEmail = { email = it},
                newRole = {role = it}
            )
        }
        composable(route = Screen.AdminAddUserManagementDB.route) {
            //sendEmail("coolgenrev@gmail.com", "Hello", "Test")
        }
        composable(route = Screen.Coop.route) {
            CoopDashboard()
        }
        composable(route = Screen.CoopOrder.route) {
            OrderRequest(
                navController = navController,
                orderViewModel = orderViewModel,
                transactionViewModel = transactionViewModel
            )
        }
        composable(route = Screen.CoopInventory.route) {
            CoopInventory(navController = navController)
        }
        composable(route = Screen.CoopProcessOrder.route) {
            ProcessOrderPanel(
                orderViewModel = orderViewModel,
                productViewModel = productViewModel
            )
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

            productType = type ?: ""

            ProductTypeInventory(
                navController = navController,
                type = productType
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

        composable(route = Screen.CoopAddProductInventoryDB.route) {
            LaunchedEffect(Unit) {
                if(productName.isNotEmpty() &&
                    farmerName.isNotEmpty() &&
                    addressName.isNotEmpty() &&
                    quantityAmount > 0)
                {
                    val product = ProductData(
                        name = productName,
                        quantity = quantityAmount,
                        type = productType
                    )
                    productViewModel.addProduct(product)
                    navController.navigate(Screen.CoopInventory.route)
                    Toast.makeText(navController.context, "${quantityAmount}kg of $productName added to $productType inventory.", Toast.LENGTH_SHORT).show()
                    productName = ""
                    farmerName = ""
                    addressName = ""
                    quantityAmount = 0
                    productType = ""
                } else {
                    Toast.makeText(navController.context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
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

            FarmerProductTypeInventory(product = product, navController = navController)
        }
        composable(
            route = Screen.FarmerInventoryDetail.route,
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productNameDetail = backStackEntry.arguments?.getString("productName")
            FarmerInventoryDetail(navController = navController, productName = productNameDetail ?: "")
        }
        composable(
            route = Screen.FarmerRequestDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")

            if (orderId != null) {
                FarmerRequestDetails(
                    navController = navController,
                    orderId = orderId,
                    onAccept = { }, // TODO:  Handle accept
                    onReject = { } // TODO: Handle reject
                )
            }
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
