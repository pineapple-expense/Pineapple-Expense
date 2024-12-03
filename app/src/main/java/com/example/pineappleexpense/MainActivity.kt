package com.example.pineappleexpense


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.theme.PineappleExpenseTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pineappleexpense.ui.screens.AccountMapping
import com.example.pineappleexpense.ui.screens.AdminProfile
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
            PineappleExpenseTheme {
                MainScreen(login = {loginWithBrowser()})
            }
        }
    }
    private fun loginWithBrowser() {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(auth0)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope("openid profile email")
            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                }

                // Called when authentication completed successfully
                override fun onSuccess(credentials: Credentials) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs
                    val accessToken = credentials.accessToken
                }
            })
    }
}


@Composable
fun MainScreen(login: ()-> Unit) {
    val viewModel = AccessViewModel()

    // This maps out the layout of the pages
    val navController = rememberNavController()

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
            UserProfile(navController, viewModel)
        }
        composable("Settings") {
            Settings(navController, viewModel)
        }
        composable("Admin Profile") {
            AdminProfile(navController,viewModel)
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

