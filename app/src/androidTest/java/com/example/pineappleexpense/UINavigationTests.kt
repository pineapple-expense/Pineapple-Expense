package com.example.pineappleexpense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import org.junit.Rule
import org.junit.Test

class UINavigationTests {
    @get:Rule
    val rule = createComposeRule()

    //test that clicking the camera icon navigates to the camera page
    @Test
    fun goToCamera() {
        var navController: NavController
        rule.setContent {
            navController = setContentHome()
        }

        //click the camera button
        rule.onNode(hasText("Camera") and hasClickAction()).performClick()

        //the camera screen is showing if the camera tag is found (set in CameraScreen.kt)
        rule.onNodeWithTag("CameraScreen").assertExists()
    }

    @Test
    fun goToArchive() {
        var navController: NavController
        rule.setContent {
            navController = setContentHome()
        }

        rule.onNode(hasText("Archive") and hasClickAction()).performClick()

        rule.onNodeWithTag("ArchiveScreen").assertExists()
    }

    @Test
    fun goBackHome() {
        var navController: NavController
        rule.setContent {
            navController = setContentHome()
        }

        rule.onNode(hasText("Archive") and hasClickAction()).performClick()
        rule.onNode(hasText("Review") and hasClickAction()).performClick()

        rule.onNodeWithTag("HomeScreen").assertExists()
    }

    @Test
    fun goToProfile() {
        var navController: NavController
        rule.setContent {
            navController = setContentHome()
        }

        rule.onNodeWithTag("ProfileIcon", useUnmergedTree = true).performClick()

        rule.onNodeWithTag("UserProfile").assertExists()
    }

    @Test
    fun goToSettingAndClickBackButton() {
        var navController: NavController
        rule.setContent {
            navController = setContentHome()
        }

        rule.onNodeWithTag("SettingsIcon", useUnmergedTree = true).performClick()

        rule.onNodeWithTag("Settings").assertExists()

        rule.onNodeWithTag("BackButton", useUnmergedTree = true).performClick()

        rule.onNodeWithTag("HomeScreen").assertExists()
    }
}

@Composable
fun setContentHome(): NavController {
    val navController = rememberNavController()
    val isGraphSet = remember { mutableStateOf(false) }

    MainScreen(
        navController = navController,
        credentialsManager = null,
        onGraphSet = { isGraphSet.value = true }
    )

    LaunchedEffect(isGraphSet.value) {
        if (isGraphSet.value) {
            navController.navigate("Home") {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    return navController
}
