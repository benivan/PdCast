package com.example.pdcast.ui.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pdcast.ui.theme.PdCastTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic tests to verify Compose components compile and render correctly
 */
@RunWith(AndroidJUnit4::class)
class ComposeScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            PdCastTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("Welcome to PdCast").assertExists()
        composeTestRule.onNodeWithText("Modernized with Jetpack Compose and Media3!").assertExists()
    }

    @Test
    fun searchScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            PdCastTheme {
                SearchScreen()
            }
        }

        composeTestRule.onNodeWithText("Search Screen").assertExists()
        composeTestRule.onNodeWithText("Go Back").assertExists()
    }

    @Test
    fun playerScreen_displaysMediaControls() {
        composeTestRule.setContent {
            PdCastTheme {
                PlayerScreen()
            }
        }

        composeTestRule.onNodeWithText("Media Player").assertExists()
        composeTestRule.onNodeWithText("Go Back").assertExists()
    }

    @Test
    fun accountScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            PdCastTheme {
                AccountScreen()
            }
        }

        composeTestRule.onNodeWithText("Account Screen").assertExists()
        composeTestRule.onNodeWithText("Go Back").assertExists()
    }
}