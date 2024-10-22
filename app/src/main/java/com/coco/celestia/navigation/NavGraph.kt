package com.coco.celestia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.coco.celestia.screens.client.AddOrderPanel
import com.coco.celestia.screens.client.ConfirmOrderRequestPanel
import com.coco.celestia.screens.client.OrderDetailsPanel
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.Calendar
import com.coco.celestia.screens.ForgotPasswordScreen
import com.coco.celestia.screens.LoginScreen
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.RegisterScreen
import com.coco.celestia.screens.SplashScreen
import com.coco.celestia.screens.admin.AddUserForm
import com.coco.celestia.screens.admin.AdminAddProduct
import com.coco.celestia.screens.admin.AdminDashboard
import com.coco.celestia.screens.admin.AdminInventory
import com.coco.celestia.screens.admin.AdminUserManagement
import com.coco.celestia.screens.admin.CheckAddUser
import com.coco.celestia.screens.admin.ConfirmAddProduct
import com.coco.celestia.screens.client.ClientContact
import com.coco.celestia.screens.client.ClientDashboard
import com.coco.celestia.screens.client.ClientOrder
import com.coco.celestia.screens.client.ClientOrderDetails
import com.coco.celestia.screens.coop.AddProductForm
import com.coco.celestia.screens.coop.CoopDashboard
import com.coco.celestia.screens.coop.CoopInventory
import com.coco.celestia.screens.coop.OrderRequest
import com.coco.celestia.screens.coop.ProcessOrderPanel
import com.coco.celestia.screens.coop.ProductTypeInventory
import com.coco.celestia.screens.farmer.FarmerDashboard
import com.coco.celestia.screens.farmer.FarmerItems
import com.coco.celestia.screens.farmer.FarmerManageOrder
import com.coco.celestia.screens.farmer.FarmerProductTypeInventory
import com.coco.celestia.screens.farmer.FarmerTransactions
import com.coco.celestia.screens.farmer.details.FarmerItemDetails
import com.coco.celestia.screens.farmer.details.FarmerOrderDetails
import com.coco.celestia.screens.farmer.details.FarmerRequestDetails
import com.coco.celestia.screens.farmer.details.LoadingIndicator
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    userRole: String,
    contactViewModel: ContactViewModel = viewModel(),
    itemViewModel: FarmerItemViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    var orderData by remember { mutableStateOf(OrderData()) }
    val productName by productViewModel.productName.observeAsState("")
    var addressName by remember { mutableStateOf("") }
    var quantityAmount by remember { mutableIntStateOf(0) }
    var productType by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var emailSend by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                userViewModel = userViewModel,
                onAccessEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                mainNavController = navController,
                userViewModel = userViewModel,
                onLoginEvent =  { onEvent(it) }
            )
        }
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                userViewModel = userViewModel,
                onRegisterEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.Calendar.route) {
            onNavigate("Calendar")
            Calendar(
                userRole = userRole,
                orderViewModel = orderViewModel,
            )
        }
//        composable(route = Screen.Dashboard.route) {
//            onNavigate("Dashboard")
//            Dashboard(userRole = userRole,
//                orderViewModel = orderViewModel)
//        }
        composable(route = Screen.Farmer.route) {
            onNavigate("Dashboard")

            LaunchedEffect(Unit) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                userViewModel.fetchUser(uid)
                orderViewModel.fetchAllOrders(filter = "", role = "Farmer")
            }

            val userData by userViewModel.userData.observeAsState()
            val orderData by orderViewModel.orderData.observeAsState(emptyList())
            val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
            val selectedCategory = ""
            val searchQuery = ""

            if (userData == null || orderState == OrderState.LOADING) {
                LoadingIndicator()
            } else {
                FarmerDashboard(
                    navController = navController,
                    userData = userData,
                    orderData = orderData,
                    orderState = orderState,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    itemViewModel = viewModel()
                )
            }
        }
        composable(route = Screen.FarmerManageOrder.route) {
            onNavigate("Orders")
            FarmerManageOrder(
                navController = navController,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel,
                productViewModel = productViewModel
            )
        }
        composable(route = Screen.FarmerItems.route) {
            onNavigate("Vegetables")
            FarmerItems(navController = navController)
        }
        composable(route = Screen.FarmerTransactions.route) {
            onNavigate("Transactions")
            FarmerTransactions(navController = navController)
        }
        composable(route = Screen.Client.route) {
            onNavigate("Dashboard")
            ClientDashboard()
        }
        composable(route = Screen.ClientOrder.route) {
            onNavigate("Orders")
            ClientOrder(
                navController = navController,
                orderViewModel = orderViewModel,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.ClientContact.route) {
            onNavigate("Contacts")
            ClientContact(contactViewModel = contactViewModel)
        }
        composable(route = Screen.Admin.route) {
            onNavigate("Dashboard")
            AdminDashboard()
        }
        composable(route = Screen.AdminInventory.route) {
            onNavigate("Inventory")
            AdminInventory(
                orderViewModel = orderViewModel,
                productViewModel = productViewModel,
                navController = navController
            )
        }
        composable(route = Screen.AdminUserManagement.route) {
            onNavigate("User Management")
            AdminUserManagement(
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.AdminAddUserManagement.route) {
            LaunchedEffect(Unit) {
                emailSend = ""
                firstname = ""
                lastname = ""
                role = ""
            }

            AddUserForm(
                navController = navController,
                email = emailSend,
                firstname = firstname,
                lastname = lastname,
                role = role,
                onEmailChanged = { emailSend = it },
                onFirstNameChanged = { firstname = it},
                onLastNameChanged = { lastname = it },
                onRoleChanged = { role = it }
            )
        }
        composable(route = Screen.AdminAddUserManagementDB.route) {
            CheckAddUser(
                userViewModel = userViewModel,
                navController = navController,
                email = emailSend,
                firstname = firstname,
                lastname = lastname,
                role = role,
                onRegisterEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.AdminAddProduct.route) {
            LaunchedEffect(Unit) {
                productViewModel.updateProductName("")
                productType = ""
                productPrice = ""
            }

            AdminAddProduct(
                navController = navController,
                productPrice = productPrice,
                productName = productName,
                onProductNameChanged = { productViewModel.updateProductName(it) },
                onTypeSelected = { productType = it },
                onPriceChanged = { productPrice = it }
            )
        }
        composable(route = Screen.AdminConfirmAddProduct.route) {
            ConfirmAddProduct(
                navController = navController,
                productViewModel = productViewModel,
                productName = productName,
                productType = productType,
                productPrice = productPrice,
                onToastEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.Coop.route) {
            onNavigate("Dashboard")
            CoopDashboard()
        }
        composable(route = Screen.CoopOrder.route) {
            onNavigate("Orders")
            OrderRequest(
                navController = navController,
                userRole = userRole,
                orderViewModel = orderViewModel,
                transactionViewModel = transactionViewModel
            )
        }
        composable(route = Screen.CoopInventory.route) {
            onNavigate("Inventory")
            CoopInventory(navController = navController, role = userRole)
        }
        composable(
            route = Screen.CoopProcessOrder.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStack ->
            val orderId = backStack.arguments?.getString("orderId")
            ProcessOrderPanel(
                orderId = orderId.toString(),
                orderViewModel = orderViewModel,
            )
        }
        composable(route = Screen.AddOrder.route) {
            onNavigate("Add Order")
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
                type = productType,
                userRole = userRole
            )
        }
        composable(route = Screen.AddProductInventory.route) {
            LaunchedEffect(Unit) {
                productViewModel.updateProductName("")
                quantityAmount = 0
            }

            AddProductForm(
                userViewModel = userViewModel,
                itemViewModel = itemViewModel,
                productViewModel = productViewModel,
                quantity = quantityAmount,
                onProductNameChange = { productViewModel.onProductNameChange(it) },
                onQuantityChange = { newValue -> quantityAmount = newValue.toIntOrNull() ?: 0 }
            )
        }

        composable(route = Screen.CoopAddProductInventoryDB.route) {
            LaunchedEffect(Unit) {
                if(productName.isNotEmpty() &&
                    quantityAmount > 0)
                {
                    val product = ProductData(
                        name = productName,
                        quantity = quantityAmount,
                        type = productType
                    )
                    productViewModel.updateProductQuantity(product.name, product.quantity)
                    navController.navigate(Screen.CoopInventory.route)
                    onEvent(Triple(ToastStatus.SUCCESSFUL, "${quantityAmount}kg of $productName added to $productType inventory.", System.currentTimeMillis()))
                    productViewModel.updateProductName("")
                    quantityAmount = 0
                    productType = ""
                } else {
                    onEvent(Triple(ToastStatus.WARNING, "Please fill in all fields", System.currentTimeMillis()))
                    navController.navigate(Screen.AddProductInventory.route)
                }
            }
        }
        composable(route = Screen.FarmerAddProductInventoryDB.route) {
            LaunchedEffect(Unit) {
                if(productName.isNotEmpty() &&
                    quantityAmount > 0)
                {
                    val product = ProductData(
                        name = productName,
                        quantity = quantityAmount,
                        type = "Vegetable"
                    )
                    itemViewModel.addItem(uid, product)
                    navController.navigate(Screen.FarmerItems.route)
                    onEvent(Triple(ToastStatus.SUCCESSFUL, "${quantityAmount}kg of $productName added to your inventory.", System.currentTimeMillis()))
                    quantityAmount = 0
                    productType = ""
                } else {
                    onEvent(Triple(ToastStatus.WARNING, "Please fill in all fields", System.currentTimeMillis()))
                    navController.navigate(Screen.AddProductInventory.route)
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
            route = Screen.FarmerItemDetails.route,
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productNameDetail = backStackEntry.arguments?.getString("productName")
            val productViewModel: ProductViewModel = viewModel()
            val productData by productViewModel.productData.observeAsState(emptyList())

            FarmerItemDetails(
                navController = navController,
                productName = productNameDetail ?: ""
            )
        }
        composable(
            route = Screen.FarmerRequestDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")

            if (orderId != null) {
                onNavigate("Order Requests")
                FarmerRequestDetails(
                    navController = navController,
                    orderId = orderId,
                    onAccept = { },
                    onReject = { }
                )
            }
        }
        composable(
            route = Screen.FarmerOrderDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")

            if (orderId != null) {
                onNavigate("Order Details")
                FarmerOrderDetails(
                    navController = navController,
                    orderId = orderId
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
                productViewModel = productViewModel,
                userViewModel = userViewModel,
                onAddToCartEvent = { onEvent(it) },
                onOrder = { orderData = it },
            )
        }
        composable(route = Screen.OrderConfirmation.route) {
            onNavigate("Order Confirmation")
            ConfirmOrderRequestPanel(
                navController = navController,
                order = orderData,
                orderViewModel = orderViewModel,
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel,
                onAddToCartEvent = { onEvent(it) },
            )
        }
        composable(route = Screen.Profile.route) {
            onNavigate("Profile")
            Profile(
                navController = navController,
                userViewModel = userViewModel,
                locationViewModel = locationViewModel,
                onLogoutEvent = { event -> onEvent(event) },
                onProfileUpdateEvent = { event -> onEvent(event) }
            )
        }
        composable(
            route = "ClientOrderDetails/{orderId}/{orderCount}",
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("orderCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            val orderCount = backStackEntry.arguments?.getInt("orderCount") ?: 0
            orderId?.let {
                ClientOrderDetails(navController, it, orderCount)
            }
        }
    }
}
