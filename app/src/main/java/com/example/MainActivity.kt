package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.components.AdminBottomNavigation
import com.example.ui.components.CompanyBottomNavigation
import com.example.ui.components.CompanyTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
            val toastMessage by viewModel.toastMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(toastMessage) {
                toastMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearToast()
                }
            }

            // System Back navigation support
            BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Splash) {
                viewModel.navigateBack()
            }

            MyApplicationTheme(darkTheme = isDark) {
                val isSplashOrOnboarding = currentScreen is Screen.Splash || currentScreen is Screen.Onboarding
                val isAdminScreen = currentScreen is Screen.AdminDashboard ||
                        currentScreen is Screen.AdminRequests ||
                        currentScreen is Screen.AdminServices ||
                        currentScreen is Screen.AdminUsers ||
                        currentScreen is Screen.AdminBlogs ||
                        currentScreen is Screen.AdminPortfolio ||
                        currentScreen is Screen.AdminMessages ||
                        currentScreen is Screen.AdminAuditLogs ||
                        currentScreen is Screen.AdminTickets ||
                        currentScreen is Screen.AdminFeedback

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (!isSplashOrOnboarding) {
                            val screenTitle = when (currentScreen) {
                                is Screen.Home -> "MR NexGen"
                                is Screen.Services -> "Services"
                                is Screen.ServiceDetail -> "Service Details"
                                is Screen.ServiceRequestForm -> "New Project Request"
                                is Screen.ServiceRequestDetail -> "Request Tracking"
                                is Screen.Portfolio -> "Portfolio"
                                is Screen.PortfolioDetail -> "Project Specs"
                                is Screen.Blogs -> "Tech Insights"
                                is Screen.BlogDetail -> "Article"
                                is Screen.AboutUs -> "About Us"
                                is Screen.ContactUs -> "Contact MR NexGen"
                                is Screen.Login -> "Sign In"
                                is Screen.Register -> "Register"
                                is Screen.EmailVerification -> "Email Verification"
                                is Screen.ForgotPassword -> "Forgot Password"
                                is Screen.ResetPassword -> "Reset Password"
                                is Screen.UserDashboard -> "Client Dashboard"
                                is Screen.MyRequests -> "My Requests"
                                is Screen.UserProfile -> "My Profile"
                                is Screen.EditProfile -> "Edit Profile"
                                is Screen.Notifications -> "Notifications"
                                is Screen.Favorites -> "Saved Items"
                                is Screen.SupportChat -> "Technical Support"
                                is Screen.SupportTickets -> "Support Tickets"
                                is Screen.CreateTicket -> "New Ticket"
                                is Screen.Feedback -> "Client Review"
                                is Screen.Training -> "Industrial Training"
                                is Screen.AdminDashboard -> "Admin Console"
                                is Screen.AdminRequests -> "Manage Requests"
                                is Screen.AdminServices -> "Manage Services"
                                is Screen.AdminUsers -> "Manage Users"
                                is Screen.AdminBlogs -> "Manage Blogs"
                                is Screen.AdminPortfolio -> "Manage Portfolio"
                                is Screen.AdminMessages -> "Client Inquiries"
                                is Screen.AdminAuditLogs -> "Security Audit"
                                is Screen.AdminTickets -> "Manage Tickets"
                                is Screen.AdminFeedback -> "Client Reviews"
                                is Screen.Settings -> "App Settings"
                                is Screen.GlobalSearch -> "Search"
                                else -> "MR NexGen"
                            }

                            CompanyTopBar(
                                title = screenTitle,
                                canNavigateBack = currentScreen !is Screen.Home,
                                unreadNotifications = unreadNotifs,
                                onNavigateBack = { viewModel.navigateBack() },
                                onNotificationClick = { viewModel.navigateTo(Screen.Notifications) },
                                onSearchClick = { viewModel.navigateTo(Screen.GlobalSearch) },
                                onProfileClick = {
                                    if (currentUser == null) {
                                        viewModel.navigateTo(Screen.Login)
                                    } else if (currentUser?.role == UserRole.ADMIN) {
                                        viewModel.navigateTo(Screen.AdminDashboard)
                                    } else {
                                        viewModel.navigateTo(Screen.UserProfile)
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isSplashOrOnboarding) {
                            if (isAdminScreen && currentUser?.role == UserRole.ADMIN) {
                                AdminBottomNavigation(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            } else {
                                CompanyBottomNavigation(
                                    currentScreen = currentScreen,
                                    unreadNotifications = unreadNotifs,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (val scr = currentScreen) {
                            is Screen.Splash -> SplashScreen(viewModel)
                            is Screen.Onboarding -> OnboardingScreen(viewModel)
                            is Screen.Home -> HomeScreen(viewModel)
                            is Screen.Services -> ServicesScreen(viewModel)
                            is Screen.ServiceDetail -> ServiceDetailScreen(viewModel)
                            is Screen.ServiceRequestForm -> ServiceRequestFormScreen(viewModel)
                            is Screen.ServiceRequestDetail -> ServiceRequestDetailScreen(viewModel)
                            is Screen.Portfolio -> PortfolioScreen(viewModel)
                            is Screen.PortfolioDetail -> PortfolioDetailScreen(viewModel)
                            is Screen.Blogs -> BlogScreen(viewModel)
                            is Screen.BlogDetail -> BlogDetailScreen(viewModel)
                            is Screen.AboutUs -> AboutUsScreen(viewModel)
                            is Screen.ContactUs -> ContactUsScreen(viewModel)
                            is Screen.Login -> LoginScreen(viewModel)
                            is Screen.Register -> RegisterScreen(viewModel)
                            is Screen.EmailVerification -> EmailVerificationScreen(viewModel, scr.email)
                            is Screen.ForgotPassword -> ForgotPasswordScreen(viewModel)
                            is Screen.ResetPassword -> ResetPasswordScreen(viewModel, scr.email)
                            is Screen.UserDashboard -> UserDashboardScreen(viewModel)
                            is Screen.MyRequests -> MyRequestsScreen(viewModel)
                            is Screen.UserProfile -> UserProfileScreen(viewModel)
                            is Screen.EditProfile -> EditProfileScreen(viewModel)
                            is Screen.Notifications -> NotificationsScreen(viewModel)
                            is Screen.Favorites -> FavoritesScreen(viewModel)
                            is Screen.SupportChat -> SupportChatScreen(viewModel)
                            is Screen.SupportTickets -> SupportTicketsScreen(viewModel)
                            is Screen.CreateTicket -> CreateTicketScreen(viewModel)
                            is Screen.Feedback -> ClientFeedbackScreen(viewModel)
                            is Screen.Training -> TrainingCoursesScreen(viewModel)
                            is Screen.AdminDashboard -> AdminDashboardScreen(viewModel)
                            is Screen.AdminRequests -> AdminRequestsScreen(viewModel)
                            is Screen.AdminServices -> AdminServicesScreen(viewModel)
                            is Screen.AdminUsers -> AdminUsersScreen(viewModel)
                            is Screen.AdminBlogs -> AdminBlogsScreen(viewModel)
                            is Screen.AdminPortfolio -> AdminPortfolioScreen(viewModel)
                            is Screen.AdminMessages -> AdminMessagesScreen(viewModel)
                            is Screen.AdminAuditLogs -> AdminAuditLogsScreen(viewModel)
                            is Screen.AdminTickets -> AdminTicketsScreen(viewModel)
                            is Screen.AdminFeedback -> AdminFeedbackScreen(viewModel)
                            is Screen.Settings -> SettingsScreen(viewModel)
                            is Screen.GlobalSearch -> GlobalSearchScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
