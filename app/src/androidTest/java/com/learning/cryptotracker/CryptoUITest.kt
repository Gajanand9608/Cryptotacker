package com.learning.cryptotracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CryptoUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testOpenBitcoin(){
        composeRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeRule.waitUntil(5000) {
            composeRule.onNodeWithTag("loading_indicator").isNotDisplayed()
        }

        // Add a delay to observe UI updates
//        Thread.sleep(1000) // 1-second delay

        composeRule
            .onNodeWithText("BTC")
            .assertIsDisplayed()
            .performClick()

//        Thread.sleep(1000) // Another delay after clicking

        composeRule.onNodeWithText("Market Cap").assertIsDisplayed()

//        Thread.sleep(2000) // Final delay to observe the result

    }

}