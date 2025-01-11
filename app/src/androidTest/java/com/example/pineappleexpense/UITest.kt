package com.example.pineappleexpense

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class UITest {
    @get:Rule
    val rule = createComposeRule()

    //test that clicking the camera icon navigates to the camera page
    @Test
    fun goToCamera() {
        rule.setContent { MainScreen(navController = rememberNavController()) }

        //click the camera button
        rule.onNode(hasText("Camera") and hasClickAction()).performClick()

        //assert that the top of the page shows "Camera" (different from the icon text because its non-clickable)
        rule.onNode(hasText("Camera") and hasNoClickAction()).assertExists()
    }
}