package com.example.pineappleexpense

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class UINavigationTests {
    @get:Rule
    val rule = createComposeRule()

    //test that clicking the camera icon navigates to the camera page
    @Test
    fun goToCamera() {
        rule.setContent { MainScreen(navController = rememberNavController()) }

        //click the camera button
        rule.onNode(hasText("Camera") and hasClickAction()).performClick()

        //the camera screen is showing if the camera tag is found (set in CameraScreen.kt)
        rule.onNodeWithTag("CameraScreen").assertExists()
    }

    @Test
    fun goToArchive() {
        rule.setContent { MainScreen(navController = rememberNavController()) }

        rule.onNode(hasText("Archive") and hasClickAction()).performClick()

        rule.onNodeWithTag("ArchiveScreen").assertExists()
    }

    @Test
    fun goBackHome() {
        rule.setContent { MainScreen(navController = rememberNavController()) }

        rule.onNode(hasText("Archive") and hasClickAction()).performClick()
        rule.onNode(hasText("Review") and hasClickAction()).performClick()

        rule.onNodeWithTag("HomeScreen").assertExists()
    }
}