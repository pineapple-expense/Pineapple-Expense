package com.example.pineappleexpense


import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.example.pineappleexpense.model.Auth0Manager
import com.example.pineappleexpense.ui.screens.AccountMapping
import com.example.pineappleexpense.ui.screens.AdminArchiveScreen
import com.example.pineappleexpense.ui.screens.AdminHome
import com.example.pineappleexpense.ui.screens.AdminProfile
import com.example.pineappleexpense.ui.screens.AdminReview
import com.example.pineappleexpense.ui.screens.AdminViewReportScreen
import com.example.pineappleexpense.ui.screens.CameraScreen
import com.example.pineappleexpense.ui.screens.EditExpense
import com.example.pineappleexpense.ui.screens.HomeScreen
import com.example.pineappleexpense.ui.screens.ReceiptPreview
import com.example.pineappleexpense.ui.screens.Registration
import com.example.pineappleexpense.ui.screens.Settings
import com.example.pineappleexpense.ui.screens.SignInTest
import com.example.pineappleexpense.ui.screens.UserArchiveScreen
import com.example.pineappleexpense.ui.screens.UserProfile
import com.example.pineappleexpense.ui.screens.ViewPreviousCSVsScreen
import com.example.pineappleexpense.ui.screens.ViewReportScreen
import com.example.pineappleexpense.ui.theme.PineappleExpenseTheme
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel


class MainActivity : ComponentActivity() {
    private lateinit var auth0: Auth0
    private lateinit var securedManager: Auth0Manager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Auth0
        auth0 = Auth0(
            getString(R.string.com_auth0_client_id), // Your Client ID from strings.xml
            getString(R.string.com_auth0_domain) // Your Domain from strings.xml
        )

        securedManager = Auth0Manager(this@MainActivity, application)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            PineappleExpenseTheme {
                MainScreen( navController, login = {loginWithBrowser(navController)}, logout = {logout(navController)}, securedManager.getCredentialsManager())
            }
        }
    }
    private fun loginWithBrowser(navController: NavHostController) {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(auth0)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope("openid profile email")
            .withAudience(getString(R.string.com_auth0_audience))
            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Called when authentication completed successfully
                override fun onSuccess(credentials: Credentials) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs

                    securedManager.storeCredentials(credentials)
                    securedManager.storeTokens(credentials)
                    securedManager.changeSessionStatus(false)
                    showUserProfile(securedManager.getAccess().toString())

                    runOnUiThread {
                        if (securedManager.getRole() == "Admin") {
                            navController.navigate("Admin Home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        else {
                            navController.navigate("Home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        Toast.makeText(
                            this@MainActivity,
                            "Logged in successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            })
    }

    private fun logout(navController: NavController) {
        WebAuthProvider.logout(auth0)
            .withScheme(getString(R.string.com_auth0_scheme)) // Match your app's scheme
            .start(this, object : Callback<Void?, AuthenticationException> {

                override fun onSuccess(payload: Void?) {
                    // Clear the stored credentials
                    securedManager.clearData()
                    securedManager.changeSessionStatus(true)
                    runOnUiThread {
                        // Navigate to the sign-in page after logging out
                        navController.navigate("SignIn") {
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                            launchSingleTop = false
                            restoreState = false
                        }
                        Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(exception: AuthenticationException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Logout failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun showUserProfile(accessToken: String) {
        var client = AuthenticationAPIClient(auth0)
        // Uses the access token obtained during login to retrieve user info
        client.userInfo(accessToken)
            .start(object: Callback<UserProfile, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    runOnUiThread {
                        // Something went wrong
                        Toast.makeText(this@MainActivity, "Failed to retrieve user info. Reason: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
                // Upon success, stores the user information in SharedPreferences
                override fun onSuccess(profile: UserProfile) {
                    securedManager.storeUserInfo(profile)
                }
            })
    }
}




@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainScreen(navController: NavHostController, login: (()-> Unit) = {}, logout: (() -> Unit) = {}, credentialsManager: SecureCredentialsManager?, onGraphSet:() -> Unit = {}) {
    val viewModel: AccessViewModel = viewModel()
    val startDestinationState = remember { mutableStateOf<String?>(null) }

    // Check credentials asynchronously
    LaunchedEffect(Unit) {
        if (viewModel.getManager() != null && viewModel.getManager().hasValidCredentials()) {
            viewModel.getManager().getCredentials(object : Callback<Credentials, CredentialsManagerException> {
                override fun onSuccess(result: Credentials) {
                    if (viewModel.getCurrentRole() == "Admin")
                        startDestinationState.value = "Admin Home" // Update state
                    else
                        startDestinationState.value = "Home"
                }
                override fun onFailure(error: CredentialsManagerException) {
                    startDestinationState.value = "SignIn" // Update state
                }
            })
        } else {
            if (!viewModel.isNewSession()) {
                logout()
                Toast.makeText(viewModel.getApplication(), "Your session has expired. Please enter your credentials to log in again.", Toast.LENGTH_LONG).show()
            }
            startDestinationState.value = "SignIn" //default
        }
    }

    //render the NavHost only when startDestinationState is resolved
    startDestinationState.value?.let { startDestination ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {

            composable("SignIn") {
                SignInTest(navController, viewModel, onLogin = login)
            }
            composable("Registration") {
                Registration(navController, viewModel)
            }
            composable("Home") {
                HomeScreen(navController, viewModel)
            }
            composable("Admin Home") {
                AdminHome(navController, viewModel)
            }
            composable("Archive") {
                UserArchiveScreen(navController, viewModel)
            }
            composable("Admin Archive") {
                AdminArchiveScreen(navController, viewModel)
            }
            composable("Profile") {
                UserProfile(navController, viewModel, logout = logout)
            }
            composable("Settings") {
                Settings(navController, viewModel)
            }
            composable("Admin Review") {
                AdminReview(navController, viewModel)
            }
            composable("Admin Profile") {
                AdminProfile(navController,viewModel, logout = logout)
            }
            composable("Camera") {
                CameraScreen(navController, viewModel)
            }
            composable("Account Mapping") {
                AccountMapping(navController, viewModel)
            }
            composable("Receipt Preview") {
                ReceiptPreview(navController, viewModel)
            }
            composable(
                route = "viewReport/{reportName}",
                arguments = listOf(navArgument("reportName") { type = NavType.StringType })
            ) { backStackEntry ->
                //retrieve report name from arguments
                val reportName = backStackEntry.arguments?.getString("reportName") ?: return@composable
                ViewReportScreen(navController, viewModel, reportName)
            }
            composable(
                route = "adminViewReport/{reportName}",
                arguments = listOf(navArgument("reportName") { type = NavType.StringType })
            ) { backStackEntry ->
                //retrieve report name from arguments
                val reportName = backStackEntry.arguments?.getString("reportName") ?: return@composable
                AdminViewReportScreen(navController, viewModel, reportName)
            }
            composable(
                route = "editExpense/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) { backStackEntry ->
                // Retrieve the expenseId from arguments
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
                EditExpense(navController, viewModel, expenseId)
            }
            composable("Previous CSV files") {
                ViewPreviousCSVsScreen(navController, viewModel)
            }
        }
        //notify callers that the navigation graph has been created
        //needed for UI navigation tests to function properly
        onGraphSet()
    }
}

