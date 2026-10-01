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

    @Test
    fun `verify release version configuration and package ID`() {
        assertEquals("com.mrnexgen.app", BuildConfig.APPLICATION_ID)
        assertEquals("1.0.0", BuildConfig.VERSION_NAME)
        assertEquals(1, BuildConfig.VERSION_CODE)
    }

    @Test
    fun `verify AppUpdateDialog rendering for new update and force update`() {
        var updateClicked = false
        var laterClicked = false

        // 1. Optional update: Update Now & Later available
        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.components.AppUpdateDialog(
                    isForceUpdate = false,
                    versionInfo = com.example.data.model.AppVersionInfo(
                        latestVersion = "1.0.1",
                        latestVersionCode = 2,
                        releaseNotes = "New features & bug fixes"
                    ),
                    onUpdateClick = { updateClicked = true },
                    onLaterClick = { laterClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("app_update_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Update Available").assertIsDisplayed()
        composeTestRule.onNodeWithText("A new version of MR NexGen is available with improvements and new features.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_now_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_later_btn").assertIsDisplayed()

        composeTestRule.onNodeWithTag("update_later_btn").performClick()
        assertTrue(laterClicked)

        composeTestRule.onNodeWithTag("update_now_btn").performClick()
        assertTrue(updateClicked)
    }

    @Test
    fun `verify force update dialog has only Update Now and prevents dismiss`() {
        var forceUpdateClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.components.AppUpdateDialog(
                    isForceUpdate = true,
                    versionInfo = com.example.data.model.AppVersionInfo(
                        latestVersion = "2.0.0",
                        latestVersionCode = 5,
                        minimumSupportedVersionCode = 2,
                        forceUpdate = true
                    ),
                    onUpdateClick = { forceUpdateClicked = true },
                    onLaterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Update Required").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_now_btn").assertIsDisplayed()
        // Verify Later button is absent in force update mode
        composeTestRule.onNodeWithTag("update_later_btn").assertDoesNotExist()

        composeTestRule.onNodeWithTag("update_now_btn").performClick()
        assertTrue(forceUpdateClicked)
    }
}

