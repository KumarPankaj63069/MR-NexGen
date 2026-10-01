package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.MainDashboardSummary
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MR NexGen", appName)
    }

    @Test
    fun `verify MainDashboardSummary displays welcome and summary cards`() {
        var activeProjectsClicked = false
        var pendingTicketsClicked = false
        var trainingCoursesClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                MainDashboardSummary(
                    userName = "Alex Morgan",
                    company = "NexTech Enterprises",
                    city = "Bengaluru",
                    activeProjectsCount = 3,
                    pendingTicketsCount = 2,
                    trainingCoursesCount = 5,
                    onActiveProjectsClick = { activeProjectsClicked = true },
                    onPendingTicketsClick = { pendingTicketsClicked = true },
                    onTrainingCoursesClick = { trainingCoursesClicked = true }
                )
            }
        }

        // Verify welcome message and badge
        composeTestRule.onNodeWithTag("dashboard_welcome_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome back, Alex Morgan! 👋").assertIsDisplayed()
        composeTestRule.onNodeWithText("NexTech Enterprises • Bengaluru").assertIsDisplayed()
        composeTestRule.onNodeWithText("MR NEXGEN CLIENT PORTAL").assertIsDisplayed()

        // Verify Active Projects summary card
        composeTestRule.onNodeWithTag("dashboard_card_active_projects").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Projects").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()

        // Verify Pending Tickets summary card
        composeTestRule.onNodeWithTag("dashboard_card_pending_tickets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending Tickets").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()

        // Verify Training Courses summary card
        composeTestRule.onNodeWithTag("dashboard_card_training_courses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Training Courses").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()

        // Verify interactive clicks
        composeTestRule.onNodeWithTag("dashboard_card_active_projects").performClick()
        assertTrue(activeProjectsClicked)

        composeTestRule.onNodeWithTag("dashboard_card_pending_tickets").performClick()
        assertTrue(pendingTicketsClicked)

        composeTestRule.onNodeWithTag("dashboard_card_training_courses").performClick()
        assertTrue(trainingCoursesClicked)
    }
}
