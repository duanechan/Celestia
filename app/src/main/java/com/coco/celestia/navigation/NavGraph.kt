package com.coco.celestia.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coco.celestia.screens.client.AddOrderPanel
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.Calendar
import com.coco.celestia.screens.ForgotPasswordScreen
import com.coco.celestia.screens.LoginScreen
import com.coco.celestia.screens.Notifications
import com.coco.celestia.screens.PrivacyPolicy
import com.coco.celestia.screens.Profile
import com.coco.celestia.screens.RegisterScreen
import com.coco.celestia.screens.SplashScreen
import com.coco.celestia.screens.client.AddSpecialReq
import com.coco.celestia.screens.client.BasketScreen
import com.coco.celestia.screens.coop.admin.AddUserForm
import com.coco.celestia.screens.coop.admin.AdminUserManagement
import com.coco.celestia.screens.coop.admin.CheckAddUser
import com.coco.celestia.screens.coop.admin.UserManagementAuditLogs
import com.coco.celestia.screens.client.ClientContact
import com.coco.celestia.screens.client.ClientDashboard
import com.coco.celestia.screens.client.ClientOrder
import com.coco.celestia.screens.client.ClientOrderDetails
import com.coco.celestia.screens.client.ClientSpecialReqDetails
import com.coco.celestia.screens.client.DisplaySpecialReq
import com.coco.celestia.screens.client.OrderSummary
import com.coco.celestia.screens.client.ProductCatalog
import com.coco.celestia.screens.client.ProductDetailScreen
import com.coco.celestia.screens.coop.AccessControlScreen
import com.coco.celestia.screens.coop.admin.AdminClients
import com.coco.celestia.screens.coop.admin.AdminHome
import com.coco.celestia.screens.coop.admin.AdminSpecialRequests
import com.coco.celestia.screens.coop.Settings
import com.coco.celestia.screens.coop.admin.ClientDetails
import com.coco.celestia.screens.coop.OrganizationProfileScreen
import com.coco.celestia.screens.coop.admin.SpecialRequestDetails
import com.coco.celestia.screens.coop.facility.forms.AddProductForm
import com.coco.celestia.screens.coop.facility.forms.CoopAddInventory
import com.coco.celestia.screens.coop.facility.CoopDashboard
import com.coco.celestia.screens.coop.facility.CoopInventory
import com.coco.celestia.screens.coop.facility.CoopInventoryDetails
import com.coco.celestia.screens.coop.facility.CoopProductInventory
import com.coco.celestia.screens.coop.facility.CoopPurchases
import com.coco.celestia.screens.coop.facility.CoopReports
import com.coco.celestia.screens.coop.facility.CoopSales
import com.coco.celestia.screens.coop.facility.CoopSalesDetails
import com.coco.celestia.screens.coop.facility.forms.CoopVendorAddForm
import com.coco.celestia.screens.coop.facility.OrderRequest
import com.coco.celestia.screens.coop.facility.PurchaseOrderDetailsScreen
import com.coco.celestia.screens.coop.facility.VendorDetailsScreen
import com.coco.celestia.screens.coop.facility.Vendors
import com.coco.celestia.screens.coop.facility.forms.CoopPurchaseForm
import com.coco.celestia.screens.coop.facility.forms.SalesAddForm
import com.coco.celestia.screens.farmer.DisplayRequestDetails
import com.coco.celestia.screens.farmer.FarmerDashboard
import com.coco.celestia.screens.farmer.FarmerItems
import com.coco.celestia.screens.farmer.FarmerManageOrder
import com.coco.celestia.screens.farmer.FarmerProductTypeInventory
import com.coco.celestia.screens.farmer.FarmerTransactions
import com.coco.celestia.screens.farmer.details.FarmerItemDetails
import com.coco.celestia.screens.farmer.details.FarmerOrderDetails
import com.coco.celestia.screens.farmer.details.FarmerOrderMilestones
import com.coco.celestia.screens.farmer.details.FarmerRequestDetails
import com.coco.celestia.screens.farmer.details.LoadingIndicator
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.NotificationService
import com.coco.celestia.viewmodel.ContactViewModel
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.PurchaseOrderViewModel
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.VegetableViewModel
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NavGraph(
    navController: NavHostController,
    userRole: String,
    contactViewModel: ContactViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel(),
    itemViewModel: FarmerItemViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    specialRequestViewModel: SpecialRequestViewModel = viewModel(),
    vendorViewModel: VendorViewModel = viewModel(),
    salesViewModel: SalesViewModel = viewModel(),
    purchaseOrderViewModel: PurchaseOrderViewModel = viewModel(),
    vegetableViewModel: VegetableViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    modifier: Modifier
) {
    val reqData by specialRequestViewModel.specialReqData.observeAsState()
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val productName by productViewModel.productName.observeAsState("")
    var quantityAmount by remember { mutableIntStateOf(0) }
    var defectBeans by remember { mutableIntStateOf(0) }
    var productType by remember { mutableStateOf("") }
    var emailSend by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0.0) }
    var isInStore by remember { mutableStateOf(true) }
    var weightUnit by remember { mutableStateOf(Constants.WEIGHT_KILOGRAMS) }
    var description by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var totalPurchases by remember { mutableStateOf(0.0) }
    var committedStock by remember { mutableStateOf(0.0) }
    var reorderPoint by remember { mutableStateOf(0.0) }
    var isDelivery by remember { mutableStateOf(false) }
    var isGcash by remember { mutableStateOf(false) }
    val notifications = remember { mutableStateListOf<Notification>() }
    var newNotification by remember { mutableIntStateOf(0) }
    var notificationFirstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
        NotificationService.observeUserNotifications(
            uid = uid,
            onNotificationsChanged = {
                if (notificationFirstLaunch) {
                    notifications.clear()
                    notifications.addAll(it)
                    notificationFirstLaunch = false
                    newNotification = it.size
                } else {
                    if (it.size > newNotification) {
                        onEvent(
                            Triple(
                                ToastStatus.SUCCESSFUL,
                                it.maxByOrNull { notification ->
                                    LocalDateTime.parse(notification.timestamp, formatter)
                                }?.message.toString(),
                                System.currentTimeMillis()
                            )
                        )
                    }
                    notifications.clear()
                    notifications.addAll(it)
                    newNotification = it.size
                }
            },
            onError = {}
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
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
                onLoginEvent = { onEvent(it) }
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
                productViewModel = productViewModel
            )
        }
        composable(route = Screen.Notifications.route) {
            onNavigate("Notifications")
            Notifications(
                role = userRole,
                navController = navController
            )
        }
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
            val searchQuery = ""

            var farmerName by remember { mutableStateOf("") }

            LaunchedEffect(userData) {
                if (userData != null) {
                    farmerName = itemViewModel.fetchFarmerName(uid)
                }
            }

            if (userData == null || orderState == OrderState.LOADING) {
                LoadingIndicator()
            } else {
                FarmerDashboard(
                    navController = navController,
                    userData = userData,
                    orderData = orderData,
                    orderState = orderState,
                    searchQuery = searchQuery,
                    itemViewModel = viewModel(),
                    productViewModel = viewModel()
                )
            }
        }
        composable(route = Screen.FarmerManageOrder.route) {
            onNavigate("Order Tabs")
            FarmerManageOrder(
                navController = navController,
                userViewModel = userViewModel,
                specialRequestViewModel = specialRequestViewModel
            )
        }
        composable(route = Screen.FarmerItems.route) {
            onNavigate("Products")
            FarmerItems(navController = navController)
        }
        composable(route = Screen.FarmerTransactions.route) {
            onNavigate("Transactions")
            FarmerTransactions(navController = navController)
        }
        composable(route = Screen.FarmerAddProduct.route) {
            onNavigate("Add Product")
            FarmerItems(navController = navController)
        }
        composable(
            route = Screen.FarmerOrderMilestones.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            FarmerOrderMilestones(navController = navController, orderId = orderId)
        }
        composable(
            route = Screen.FarmerRequestCardDetails.route,
            arguments = listOf(
                navArgument("specialReqUID") { type = NavType.StringType },
                navArgument("farmerEmail") { type = NavType.StringType },
                navArgument("product") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val specialReqUID = backStackEntry.arguments?.getString("specialReqUID") ?: ""
            val farmerEmail = backStackEntry.arguments?.getString("farmerEmail") ?: ""
            val product = backStackEntry.arguments?.getString("product") ?: ""
            DisplayRequestDetails(
                specialRequestViewModel,
                specialReqUID,
                farmerEmail,
                product
            )
        }

        composable(route = Screen.Client.route) {
            onNavigate("Home")
            ClientDashboard(
                navController = navController,
                userViewModel = userViewModel,
                productViewModel = productViewModel,
                orderViewModel = orderViewModel,
//                transactionViewModel = transactionViewModel
            )
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

        composable(route = Screen.ClientSpecialReq.route) {
            onNavigate("Requests")
            DisplaySpecialReq(
                navController
            )
        }

        composable(route = Screen.ClientAddSpecialReq.route) {
            onNavigate("Request an Order")
            AddSpecialReq(
                navController,
                specialRequestViewModel,
                userViewModel,
                vegetableViewModel
            )
        }

        composable(route = Screen.Admin.route) {
            onNavigate("Home")
            AdminHome(
                navController = navController,
                facilityViewModel = facilityViewModel,
                userViewModel = userViewModel,
                onEvent = { onEvent(it) })
        }

        composable(route = Screen.Settings.route) {
            onNavigate("Settings")
            Settings(navController = navController, userRole = userRole)
        }
        composable(route = Screen.OrganizationProfile.route) {
            onNavigate("Organizational Profile")
            OrganizationProfileScreen()
        }
        composable(route = Screen.AccessControl.route) {
            onNavigate("Access Control")
            AccessControlScreen(userViewModel = userViewModel, facilityViewModel = facilityViewModel, currentUserEmail = userEmail, currentUserRole = userRole)
        }
        composable(route = Screen.PrivacyPolicy.route) {
            onNavigate("Privacy Policy")
            PrivacyPolicy()
        }
        composable(route = Screen.AdminClients.route) {
            onNavigate("Clients & Customers")
            AdminClients(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(route = Screen.AdminClientDetails.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            onNavigate("Client Details")
            ClientDetails(email = email, userViewModel = userViewModel)
        }
        composable(route = Screen.AdminUserManagement.route) {
            onNavigate("Members")
            AdminUserManagement(
                userViewModel = userViewModel,
                facilityViewModel = facilityViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }
        composable(route = Screen.AdminUserManagementAuditLogs.route) {
            onNavigate("Audit Logs")
            UserManagementAuditLogs(
                transactionViewModel = transactionViewModel,
                navController = navController
            )
        }
        composable(route = Screen.AdminAddUserManagement.route) {
            onNavigate("Add User")
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
                facilityViewModel = facilityViewModel,
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel,
                onEmailChanged = { emailSend = it },
                onFirstNameChanged = { firstname = it },
                onLastNameChanged = { lastname = it },
                onRoleChanged = { role = it },
                onRegisterEvent = onEvent
            )
        }
        composable(route = Screen.AdminAddUserManagementDB.route) {
            onNavigate("Add User")
            CheckAddUser(
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel,
                navController = navController,
                email = emailSend,
                firstname = firstname,
                lastname = lastname,
                role = role,
                onRegisterEvent = { onEvent(it) }
            )
        }
        composable(
            route = Screen.AdminSpecialRequests.route,
            arguments = listOf(navArgument("status") { type = NavType.StringType })
        ) {
            val status = it.arguments?.getString("status") ?: ""

            onNavigate("Special Requests")
            AdminSpecialRequests(
                specialRequestViewModel,
                navController,
                status
            )
        }

        composable(
            route = Screen.AdminSpecialRequestsDetails.route,
            arguments = listOf(navArgument("requestUid") { type = NavType.StringType })
        ) {
            val requestUid = it.arguments?.getString("requestUid") ?: ""
            val selectedRequest = reqData?.find { request ->
                request.specialRequestUID == requestUid
            }

            selectedRequest?.let { request ->
                onNavigate(request.subject)
                SpecialRequestDetails(
                    navController,
                    userViewModel,
                    specialRequestViewModel,
                    request
                )
            }

        }
//        composable(route = Screen.AdminAddProduct.route) {
//            onNavigate("Add Product")
//            LaunchedEffect(Unit) {
//                productViewModel.updateProductName("")
//                productType = ""
//                productPrice = ""
//            }
//
//            AdminAddProduct(
//                productPrice = productPrice,
//                productName = productName,
//                onUpdatedProductImage = { updatedProductImage = it },
//                onProductNameChanged = { productViewModel.updateProductName(it) },
//                onTypeSelected = { productType = it },
//                onPriceChanged = { productPrice = it },
//                onToastEvent = { onEvent(it) }
//            )
//        }
//        composable(route = Screen.AdminConfirmAddProduct.route) {
//            onNavigate("Add Product")
//            if (productName.isNotEmpty() && productType.isNotEmpty() && productPrice.isNotEmpty()) {
//                ConfirmAddProduct(
//                    navController = navController,
//                    productViewModel = productViewModel,
//                    transactionViewModel = transactionViewModel,
//                    productName = productName,
//                    productType = productType,
//                    productPrice = productPrice,
//                    updatedProductImage = updatedProductImage,
//                    onToastEvent = { onEvent(it) }
//                )
//            } else {
//                onEvent(Triple(ToastStatus.WARNING, "All Fields must be filled", System.currentTimeMillis()))
//                navController.navigate(Screen.AdminAddProduct.route)
//            }
//        }
        composable(route = Screen.Coop.route) {
            onNavigate("Dashboard")
            CoopDashboard(
                orderViewModel = orderViewModel,
                productViewModel = productViewModel,
                role = userRole,
                facilityViewModel = facilityViewModel)
        }
        composable(route = Screen.CoopOrder.route) {
            onNavigate("Orders")
            OrderRequest(
                userRole = userRole,
                orderViewModel = orderViewModel,
                transactionViewModel = transactionViewModel,
                onUpdateOrder = { onEvent(it) }
            )
        }
        composable(route = Screen.CoopInventory.route) {
            onNavigate("Inventory")
            CoopInventory(
                navController = navController,
                role = userRole,
                userEmail = userEmail
            )
        }
        composable(
            route = Screen.CoopInventoryDetails.route,
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            onNavigate("Product Details")
            CoopInventoryDetails(
                navController = navController,
                productName = productName,
                productViewModel = productViewModel,
                onEvent = { onEvent(it) }
            )
        }
        composable(route = Screen.AddOrder.route) {
            onNavigate("Add Order")
            AddOrderPanel(
                navController = navController,
                orderViewModel = orderViewModel,
                productViewModel = productViewModel,
                productType = productType ?: "",
                userViewModel = userViewModel,
            )
        }
        composable(
            route = Screen.CoopInStoreProducts.route,
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""

            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

            onNavigate("In-Store Products")
            CoopProductInventory(
                navController = navController,
                facilityName = facilityName,
                currentEmail = currentEmail,
                isInStore = true
            )
        }

        composable(
            route = Screen.CoopOnlineProducts.route,
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""

            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

            onNavigate("Online Products")
            CoopProductInventory(
                navController = navController,
                facilityName = facilityName,
                currentEmail = currentEmail,
                isInStore = false
            )
        }

        composable(
            route = Screen.CoopInStoreSales.route,
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""

            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            onNavigate("In-Store Sales")
            CoopSales(
                navController = navController,
                facilityName = facilityName,
                userEmail = currentEmail,
                isInStore = true
            )
        }

        composable(
            route = Screen.CoopOnlineSales.route,
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""

            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            onNavigate("Online Sales")
            CoopSales(
                navController = navController,
                facilityName = facilityName,
                userEmail = currentEmail,
                isInStore = false
            )
        }

        composable(
            route = Screen.CoopSales.route,
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""

            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            onNavigate("Sales")
            CoopSales(
                navController = navController,
                facilityName = facilityName,
                userEmail = currentEmail,
                isInStore = false
            )
        }

        composable(Screen.CoopAddSales.route) {
            val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
            val userFacility = facilitiesData.find { facility ->
                facility.emails.contains(userEmail)
            }

            SalesAddForm(
                viewModel = salesViewModel,
                productViewModel = productViewModel,
                facilityName = userFacility?.name ?: "",
                userRole = userRole,
                onSuccess = { navController.navigateUp() },
                onCancel = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.CoopEditSales.route,
            arguments = listOf(navArgument("salesId") { type = NavType.StringType })
        ) { entry ->
            val salesId = entry.arguments?.getString("salesId")
            val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
            val userFacility = facilitiesData.find { facility ->
                facility.emails.contains(userEmail)
            }

            SalesAddForm(
                viewModel = salesViewModel,
                productViewModel = productViewModel,
                facilityName = userFacility?.name ?: "",
                userRole = userRole,
                salesNumber = salesId,
                onSuccess = { navController.navigateUp() },
                onCancel = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.CoopSalesDetails.route,
            arguments = listOf(
                navArgument("salesNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            CoopSalesDetails(
                navController = navController,
                userEmail = userEmail
            )
        }

        composable(
            route = Screen.CoopOrderDetails.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            CoopSalesDetails(
                navController = navController,
                userEmail = userEmail
            )
        }

        composable(route = Screen.AddProductInventory.route) {
            var facilityName by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf(0) }
            var notes by remember { mutableStateOf("") }
            var productId by remember { mutableStateOf("") }

            val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

            LaunchedEffect(email) {
                productViewModel.updateProductName("")
                productViewModel.updateDescription("")
                productViewModel.updateVendor("")
                productViewModel.updateNotes("")
                price = 0.0
                totalPurchases = 0.0
                committedStock = 0.0
                reorderPoint = 0.0
                isInStore = true
                isDelivery = false
                isGcash = false
                weightUnit = Constants.WEIGHT_KILOGRAMS
                facilityViewModel.fetchFacilities()

                facilityName = facilityViewModel.facilitiesData.value
                    ?.find { it.emails.contains(email) }
                    ?.name
                    .orEmpty()
            }

            onNavigate("Add Product")

            AddProductForm(
                navController = navController,
                userViewModel = userViewModel,
                productViewModel = productViewModel,
                facilityViewModel = facilityViewModel,
                vendorViewModel = vendorViewModel,
                productId = productId,
                quantity = quantity,
                price = price,
                totalPurchases = totalPurchases,
                reorderPoint = reorderPoint,
                isInStore = isInStore,
                weightUnit = weightUnit,
                notes = notes,
                onProductNameChange = { productViewModel.onProductNameChange(it) },
                onDescriptionChange = { productViewModel.updateDescription(it) },
                onNotesChange = { newValue ->
                    notes = newValue
                    productViewModel.updateNotes(newValue)
                },
                onQuantityChange = { newValue -> quantity = newValue.toIntOrNull() ?: 0 },
                onPriceChange = { newValue -> price = newValue.toDoubleOrNull() ?: 0.0 },
                onReorderPointChange = { newValue -> reorderPoint = newValue.toDoubleOrNull() ?: 0.0 },
                onIsInStoreChange = { isInStore = it },
                onWeightUnitChange = { weightUnit = it },
                onAddClick = {
                    navController.navigate(Screen.CoopInventory.route) {
                        popUpTo(Screen.CoopInventory.route) { inclusive = true }
                    }
                },
                onEvent = { onEvent(it) }
            )
        }

        composable(
            route = Screen.EditProductInventory.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val productId = Uri.decode(backStackEntry.arguments?.getString("productId") ?: "")
            var facilityName by remember { mutableStateOf("") }
            var quantity by remember { mutableIntStateOf(0) }
            var notes by remember { mutableStateOf("") }

            val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

            LaunchedEffect(Unit) {
                productViewModel.fetchProduct(productId)
                productViewModel.productData.value?.firstOrNull()?.let { product ->
                    productViewModel.updateProductName(product.name)
                    productViewModel.updateDescription(product.description)
                    productViewModel.updateVendor(product.vendor)
                    productViewModel.updateNotes(product.notes)
                    quantity = product.quantity
                    price = product.price
                    totalPurchases = product.totalPurchases
                    reorderPoint = product.reorderPoint
                    notes = product.notes
                    isInStore = product.isInStore
//                    isDelivery = product.collectionMethod == Constants.COLLECTION_DELIVERY
//                    isGcash = product.paymentMethod == Constants.PAYMENT_GCASH
                    weightUnit = product.weightUnit
                }
            }

            LaunchedEffect(email) {
                facilityViewModel.fetchFacilities()
                facilityName = facilityViewModel.facilitiesData.value
                    ?.find { it.emails.contains(email) }
                    ?.name
                    .orEmpty()
            }

            onNavigate("Edit Product")

            AddProductForm(
                navController = navController,
                userViewModel = userViewModel,
                productViewModel = productViewModel,
                facilityViewModel = facilityViewModel,
                vendorViewModel = vendorViewModel,
                productId = productId,
                quantity = quantity,
                price = price,
                totalPurchases = totalPurchases,
                reorderPoint = reorderPoint,
                isInStore = isInStore,
                weightUnit = weightUnit,
                notes = notes,
                onProductNameChange = { productViewModel.onProductNameChange(it) },
                onDescriptionChange = { productViewModel.updateDescription(it) },
                onNotesChange = { newValue ->
                    notes = newValue
                    productViewModel.updateNotes(newValue)
                },
                onQuantityChange = { newValue -> quantity = newValue.toIntOrNull() ?: 0 },
                onPriceChange = { newValue -> price = newValue.toDoubleOrNull() ?: 0.0 },
                onReorderPointChange = { newValue -> reorderPoint = newValue.toDoubleOrNull() ?: 0.0 },
                onIsInStoreChange = { isInStore = it },
                onWeightUnitChange = { weightUnit = it },
                onAddClick = {
                    navController.navigate(Screen.CoopInventory.route) {
                        popUpTo(Screen.CoopInventory.route) { inclusive = true }
                    }
                },
                onEvent = { onEvent(it) }
            )
        }

        composable(route = Screen.CoopAddProductInventoryDB.route) {
            onNavigate("Add Product")
            CoopAddInventory(
                navController = navController,
                productViewModel = productViewModel,
                transactionViewModel = transactionViewModel,
                productName = productName,
                description = description,
                quantityAmount = quantityAmount,
                productType = productType,
                price = price,
                vendor = vendor,
                totalPurchases = totalPurchases,
                committedStock = committedStock,
                reorderPoint = reorderPoint,
                isInStore = isInStore,
                weightUnit = weightUnit,
//                isDelivery = isDelivery,
//                isGcash = isGcash,
                onEvent = { onEvent(it) }
            )

            LaunchedEffect(Unit) {
                productViewModel.updateProductName("")
                productViewModel.updateDescription("")
                productViewModel.updateVendor("")
                quantityAmount = 0
                productType = ""
                price = 0.0
                totalPurchases = 0.0
                committedStock = 0.0
                reorderPoint = 0.0
                isInStore = true
                isDelivery = false
                isGcash = false
                weightUnit = Constants.WEIGHT_KILOGRAMS
            }
        }
        composable(Screen.CoopReports.route) {
            CoopReports(navController)
        }
        composable(Screen.CoopPurchases.route) {
            onNavigate("Purchase Orders")
            CoopPurchases(
                navController = navController,
                currentEmail = userEmail,
                purchaseOrderViewModel = purchaseOrderViewModel,
                facilityViewModel = facilityViewModel,
                modifier = Modifier
            )
        }
        composable(
            route = Screen.CoopPurchaseForm.route,
            arguments = listOf(
                navArgument("draftId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("purchaseNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val draftId = backStackEntry.arguments?.getString("draftId")
            val purchaseNumber = backStackEntry.arguments?.getString("purchaseNumber")

            onNavigate(
                when {
                    !purchaseNumber.isNullOrEmpty() -> "Edit Purchase Order"
                    !draftId.isNullOrEmpty() -> "Edit Draft"
                    else -> "New Purchase Order"
                }
            )

            val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
            val userFacility = facilitiesData.find { facility ->
                facility.emails.contains(userEmail)
            }

            LaunchedEffect(Unit) {
                facilityViewModel.fetchFacilities()
            }

            CoopPurchaseForm(
                purchaseOrderViewModel = purchaseOrderViewModel,
                vendorViewModel = vendorViewModel,
                facilityViewModel = facilityViewModel,
                productViewModel = productViewModel,
                facilityName = userFacility?.name ?: "",
                draftId = draftId,
                currentEmail = userEmail,
                purchaseNumber = purchaseNumber,
                onSuccess = {
                    navController.popBackStack()
                },
                navController = navController,
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.CoopPurchaseDetails.route,
            arguments = listOf(navArgument("purchaseNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val purchaseNumber = backStackEntry.arguments?.getString("purchaseNumber") ?: ""
            val facilityName = backStackEntry.arguments?.getString("facilityName") ?: ""
            PurchaseOrderDetailsScreen(
                purchaseNumber = purchaseNumber,
                purchaseOrderViewModel = purchaseOrderViewModel,
                facilityName = facilityName,
                navController = navController,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(Screen.CoopVendors.route) {
            onNavigate("Vendors")
            Vendors(
                navController = navController,
                currentEmail = userEmail,
                onAddVendor = { navController.navigate(Screen.CoopAddVendor.route) },
                viewModel = vendorViewModel,
                facilityViewModel = facilityViewModel
            )
        }
        composable(Screen.CoopAddVendor.route) {
            val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())

            val userFacility = facilitiesData.find { facility ->
                facility.emails.contains(userEmail)
            }

            CoopVendorAddForm(
                viewModel = vendorViewModel,
                facilityName = userFacility?.name ?: "",
                onSuccess = { navController.navigateUp() },
                onCancel = { navController.navigateUp() },
                locationViewModel = locationViewModel
            )
        }
        composable(
            route = Screen.CoopEditVendor.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())

            val userFacility = facilitiesData.find { facility ->
                facility.emails.contains(userEmail)
            }

            CoopVendorAddForm(
                viewModel = vendorViewModel,
                facilityName = userFacility?.name ?: "",
                email = email,
                onSuccess = { navController.navigateUp() },
                onCancel = { navController.navigateUp() },
                locationViewModel = locationViewModel
            )
        }
        composable(
            route = Screen.CoopVendorDetails.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VendorDetailsScreen(
                email = email,
                viewModel = vendorViewModel,
                onNavigateUp = { navController.navigateUp() },
                navController = navController
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
            val currentMonth = java.time.LocalDate.now().monthValue.toString()

            onNavigate("Inventory")
            FarmerProductTypeInventory(
                product = product,
                navController = navController
            )
        }
        composable(
            route = Screen.FarmerItemDetails.route,
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productNameDetail = backStackEntry.arguments?.getString("productName")
            onNavigate("Products")
            FarmerItemDetails(
                navController = navController,
                productName = productNameDetail ?: "",
                onToastEvent = { onEvent(it) }
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
                    orderId = orderId
                )
            }
        }
        composable(
            route = Screen.FarmerOrderDetails.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType }
            )
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

//        composable(Screen.FarmerNotification.route) {
//            FarmerNotification()
//        }


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
//        composable(
//            route = Screen.ClientOrderDetails.route,
//            arguments = listOf(
//                navArgument("orderId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val orderId = backStackEntry.arguments?.getString("orderId")
//            onNavigate("")
//            orderId?.let {
//                ClientOrderDetails(navController, it)
//            }
//        }

        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedProductId = backStackEntry.arguments?.getString("productId")
            if (encodedProductId != null) {
                val productId = java.net.URLDecoder.decode(encodedProductId, "UTF-8")
                val product = productViewModel.productData.value?.find { it.productId == productId }
                onNavigate(product?.name ?: "Product")

                ProductDetailScreen(
                    navController = navController,
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    productViewModel = productViewModel,
                    productId = productId,
                    onEvent = { onEvent(it) }
                )
            }
        }
        composable(
            route = Screen.ProductCatalog.route,
            arguments = listOf(
                navArgument("searchQuery") {
                    type = NavType.StringType
                    defaultValue = "none"
                },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "Client"
                },
                navArgument("showSearch") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val searchQuery = backStackEntry.arguments?.getString("searchQuery") ?: "none"
            val role = backStackEntry.arguments?.getString("role") ?: "Client"
            val showSearch = backStackEntry.arguments?.getBoolean("showSearch") ?: false

            onNavigate("Product Search")
            ProductCatalog(
                productViewModel = productViewModel,
                facilityViewModel = facilityViewModel,
                role = role,
                navController = navController,
                searchQuery = if (searchQuery == "none") "" else searchQuery,
                showSearch = showSearch
            )
        }

        composable(
            route = Screen.ClientSpecialReqDetails.route,
            arguments = listOf(navArgument("specialRequestUID") { type = NavType.StringType })
        ) { backStackEntry ->
            val specialRequestUID = backStackEntry.arguments?.getString("specialRequestUID") ?: ""
            onNavigate("Request Details")
            ClientSpecialReqDetails(
                navController = navController,
                specialRequestViewModel = specialRequestViewModel,
                specialRequestUID = specialRequestUID
            )
        }

        composable(route = Screen.Basket.route) {
            onNavigate("Basket")
            BasketScreen(
                navController = navController,
                productViewModel = productViewModel,
                userViewModel = userViewModel,
                onEvent = { onEvent(it) }
            )
        }

        composable(
            route = Screen.ClientOrderDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            ClientOrderDetails(navController = navController, orderId = orderId, viewModel = orderViewModel)
        }

        composable(
            route = Screen.OrderSummary.route,
            arguments = listOf(navArgument("items") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemsJson = backStackEntry.arguments?.getString("items") ?: ""
            val items = try {
                Json.decodeFromString<List<BasketItem>>(Uri.decode(itemsJson))
            } catch (e: Exception) {
                emptyList()
            }
            onNavigate("Order Summary")
            OrderSummary(
                navController = navController,
                userViewModel = userViewModel,
                orderViewModel = orderViewModel,
                facilityViewModel = facilityViewModel,  // Add this
                items = items,
                onEvent = { onEvent(it) }
            )
        }

        composable(
            route = "add_order/{productType}",
            arguments = listOf(navArgument("productType") { type = NavType.StringType })
        ) { backStackEntry ->
            val productType = backStackEntry.arguments?.getString("productType")
            AddOrderPanel(
                navController = navController,
                productType = productType ?: "",
                orderViewModel = viewModel(),
                productViewModel = viewModel(),
                userViewModel = viewModel()
            )
        }
    }
}