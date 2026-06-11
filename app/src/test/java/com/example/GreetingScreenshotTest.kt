package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.SpotRateLongCard
import com.example.ui.theme.AurumGold
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        SpotRateLongCard(
          metalName = "Gold Bullion (999.9)",
          symbol = "Au",
          currentPriceStr = "£1,842.50",
          percentageChange = 0.42,
          spreadStr = "£9.21",
          accentColor = AurumGold,
          indicatorPoints = listOf(0.4f, 0.45f, 0.42f, 0.48f, 0.52f, 0.5f, 0.55f, 0.58f),
          testTag = "gold_spot_card"
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
