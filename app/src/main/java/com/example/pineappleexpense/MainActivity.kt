package com.example.pineappleexpense


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.theme.PineappleExpenseTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pineappleexpense.ui.screens.AccountMapping
import com.example.pineappleexpense.ui.screens.AdminProfile
import com.example.pineappleexpense.ui.screens.AdminReview
import com.example.pineappleexpense.ui.screens.CameraScreen
import com.example.pineappleexpense.ui.screens.UserArchiveScreen
import com.example.pineappleexpense.ui.screens.HomeScreen
import com.example.pineappleexpense.ui.screens.ReceiptPreview
import com.example.pineappleexpense.ui.screens.Registration
import com.example.pineappleexpense.ui.screens.Settings
import com.example.pineappleexpense.ui.screens.SignIn
import com.example.pineappleexpense.ui.screens.UserProfile
import com.example.pineappleexpense.ui.screens.ViewPreviousExpense
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.callback.Callback
import com.auth0.android.result.Credentials
import com.example.pineappleexpense.ui.screens.SignInTest

class MainActivity : ComponentActivity() {
    private lateinit var auth0: Auth0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Auth0
        auth0 = Auth0(
            getString(R.string.com_auth0_client_id), // Your Client ID from strings.xml
            getString(R.string.com_auth0_domain) // Your Domain from strings.xml
        )

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            PineappleExpenseTheme {
                MainScreen( navController, login = {loginWithBrowser(navController)}, logout = {logout(navController)})
            }
        }
    }
    private fun loginWithBrowser(navController: NavHostController) {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(auth0)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope("openid profile email")
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
                    val accessToken = credentials.accessToken
                    runOnUiThread {
                        // Navigate to the Home screen
                        navController.navigate("Home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
                    runOnUiThread {
                        // Navigate to the sign-in page after logging out
                        navController.navigate("SignIn") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
}


@Composable
fun MainScreen(navController: NavHostController, login: (()-> Unit) = {}, logout: (() -> Unit) = {}) {
    val viewModel = AccessViewModel()


    NavHost(
        navController = navController,
        startDestination = "SignIn",
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
        composable("Archive") {
            UserArchiveScreen(navController, viewModel)
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
    }
}

