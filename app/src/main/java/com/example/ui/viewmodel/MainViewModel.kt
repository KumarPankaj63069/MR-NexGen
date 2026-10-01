package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.MrNexGenRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MrNexGenRepository(db)
    private val prefs = application.getSharedPreferences("mr_nexgen_auth_prefs", android.content.Context.MODE_PRIVATE)

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val currentScreen: StateFlow<Screen> = _screenStack.map { it.lastOrNull() ?: Screen.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Screen.Splash)

    // Current Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    init {
        // Restore persistent login session
        viewModelScope.launch {
            val savedUserId = prefs.getString("saved_user_id", null)
            val savedToken = prefs.getString("saved_token", null)
            if (!savedUserId.isNullOrBlank()) {
                val user = repository.getUserById(savedUserId)
                if (user != null && user.isActive && user.isVerified) {
                    _currentUser.value = user
                    _authToken.value = savedToken ?: "persisted_token"
                }
            }
            // Check for application updates on startup
            checkForAppUpdates(isManual = false)
        }
    }

    // Version Management & Remote Update State
    private val _appVersionInfo = MutableStateFlow(AppVersionInfo())
    val appVersionInfo: StateFlow<AppVersionInfo> = _appVersionInfo.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _isForceUpdate = MutableStateFlow(false)
    val isForceUpdate: StateFlow<Boolean> = _isForceUpdate.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    // Global Selected Items
    private val _selectedService = MutableStateFlow<ServiceItem?>(null)
    val selectedService: StateFlow<ServiceItem?> = _selectedService.asStateFlow()

    private val _selectedRequest = MutableStateFlow<ServiceRequest?>(null)
    val selectedRequest: StateFlow<ServiceRequest?> = _selectedRequest.asStateFlow()

    private val _selectedPortfolio = MutableStateFlow<PortfolioItem?>(null)
    val selectedPortfolio: StateFlow<PortfolioItem?> = _selectedPortfolio.asStateFlow()

    private val _selectedBlog = MutableStateFlow<BlogPost?>(null)
    val selectedBlog: StateFlow<BlogPost?> = _selectedBlog.asStateFlow()

    // Global Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // App Preferences
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    // Transient UI status / Snackbar message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Live Data Flows
    val publishedServices = repository.getPublishedServicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices = repository.getAllServicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedPortfolio = repository.getPortfolioFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPortfolio = repository.getAllPortfolioFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedBlogs = repository.getBlogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlogs = repository.getAllBlogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactMessages = repository.getAllContactMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminRequests = repository.getAllRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminUsers = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs = repository.getAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userRequests: StateFlow<List<ServiceRequest>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserRequestsFlow(user.id) else flowOf(emptyList<ServiceRequest>())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userNotifications: StateFlow<List<AppNotification>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsFlow(user.id) else flowOf(emptyList<AppNotification>())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationsCountFlow(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userFavorites: StateFlow<List<FavoriteItem>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getFavoritesFlow(user.id) else flowOf(emptyList<FavoriteItem>())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val supportMessages: StateFlow<List<SupportMessage>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getConversationFlow(user.id) else flowOf(emptyList<SupportMessage>())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trainingCourses = MutableStateFlow(repository.getTrainingCourses()).asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userTickets: StateFlow<List<SupportTicket>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserTicketsFlow(user.id) else flowOf(emptyList<SupportTicket>())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userPendingTicketsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserPendingTicketsCountFlow(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAdminTickets = repository.getAllTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeedback = repository.getAllFeedbackFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiAssistantActive = MutableStateFlow(true)
    val isAiAssistantActive: StateFlow<Boolean> = _isAiAssistantActive.asStateFlow()

    fun toggleAiAssistant(active: Boolean) {
        _isAiAssistantActive.value = active
    }

    // --- NAVIGATION ---
    fun navigateTo(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        // Prevent duplicate top of stack
        if (current.lastOrNull() != screen) {
            current.add(screen)
            _screenStack.value = current
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            return true
        }
        return false
    }

    fun replaceTop(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        if (current.isNotEmpty()) {
            current.removeAt(current.size - 1)
        }
        current.add(screen)
        _screenStack.value = current
    }

    fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
        navigateTo(Screen.Home)
    }

    fun selectService(service: ServiceItem, navigate: Boolean = true) {
        _selectedService.value = service
        if (navigate) navigateTo(Screen.ServiceDetail)
    }

    fun selectRequest(request: ServiceRequest, navigate: Boolean = true) {
        _selectedRequest.value = request
        if (navigate) navigateTo(Screen.ServiceRequestDetail)
    }

    fun selectPortfolio(item: PortfolioItem, navigate: Boolean = true) {
        _selectedPortfolio.value = item
        if (navigate) navigateTo(Screen.PortfolioDetail)
    }

    fun selectBlog(blog: BlogPost, navigate: Boolean = true) {
        _selectedBlog.value = blog
        if (navigate) navigateTo(Screen.BlogDetail)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    // --- AUTH ACTIONS ---
    fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email, pass)
            if (res.success) {
                _currentUser.value = res.user
                _authToken.value = res.token
                prefs.edit()
                    .putString("saved_user_id", res.user?.id)
                    .putString("saved_token", res.token)
                    .apply()
                _toastMessage.value = "Welcome back, ${res.user?.name}!"
                if (res.user?.role == UserRole.ADMIN) {
                    navigateTo(Screen.AdminDashboard)
                } else {
                    navigateTo(Screen.UserDashboard)
                }
                onResult(true, res.message)
            } else {
                onResult(false, res.message)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        company: String,
        city: String,
        onResult: (Boolean, String, String?) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.register(name, email, phone, pass, company, city)
            if (res.success) {
                _toastMessage.value = "Account created! Verification code sent."
                navigateTo(Screen.EmailVerification(email))
                onResult(true, res.message, res.verificationOtp)
            } else {
                onResult(false, res.message, null)
            }
        }
    }

    fun verifyEmail(email: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.verifyEmail(email, otp)
            if (res.success) {
                _toastMessage.value = "Verification successful! You can now log in."
                navigateTo(Screen.Login)
                onResult(true, res.message)
            } else {
                onResult(false, res.message)
            }
        }
    }

    fun resendVerification(email: String, onResult: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.resendVerificationOtp(email)
            if (res.success) {
                _toastMessage.value = "Verification code resent."
                onResult(true, res.message, res.verificationOtp)
            } else {
                onResult(false, res.message, null)
            }
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.forgotPassword(email)
            if (res.success) {
                _toastMessage.value = "Reset code sent."
                navigateTo(Screen.ResetPassword(email))
                onResult(true, res.message, res.verificationOtp)
            } else {
                onResult(false, res.message, null)
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPassword(email, otp, newPass)
            if (res.success) {
                _toastMessage.value = "Password updated! Please log in."
                navigateTo(Screen.Login)
                onResult(true, res.message)
            } else {
                onResult(false, res.message)
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _authToken.value = null
        _toastMessage.value = "Logged out successfully"
        _screenStack.value = listOf(Screen.Home)
    }

    fun updateProfile(name: String, phone: String, company: String, city: String, onDone: (Boolean) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = repository.updateProfile(user.id, name, phone, company, city)
            if (updated != null) {
                _currentUser.value = updated
                _toastMessage.value = "Profile updated successfully."
                navigateBack()
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onDone: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val ok = repository.changePassword(user.id, oldPass, newPass)
            if (ok) {
                _toastMessage.value = "Password changed successfully."
                onDone(true, "Password changed successfully")
            } else {
                onDone(false, "Current password does not match.")
            }
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(user.id)
            logout()
            onDone()
        }
    }

    // --- SERVICE REQUEST ACTIONS ---
    fun submitServiceRequest(
        serviceId: String,
        serviceTitle: String,
        projectTitle: String,
        description: String,
        budget: String,
        deadline: String,
        onComplete: (ServiceRequest?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            _toastMessage.value = "Please log in to submit a service request."
            navigateTo(Screen.Login)
            onComplete(null)
            return
        }
        viewModelScope.launch {
            val req = repository.submitServiceRequest(
                user = user,
                serviceId = serviceId,
                serviceTitle = serviceTitle,
                projectTitle = projectTitle,
                description = description,
                budget = budget,
                deadline = deadline
            )
            _selectedRequest.value = req
            _toastMessage.value = "Service request ${req.requestId} submitted!"
            navigateTo(Screen.ServiceRequestDetail)
            onComplete(req)
        }
    }

    fun updateRequestStatus(
        requestId: String,
        status: RequestStatus,
        reason: String,
        notes: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, status, reason, notes, admin)
            _toastMessage.value = "Request status updated to $status"
        }
    }

    // --- FAVORITES ---
    fun toggleFavorite(itemType: String, itemId: String, title: String, subtitle: String, imageUrl: String = "") {
        val user = _currentUser.value
        if (user == null) {
            _toastMessage.value = "Please log in to save favorites."
            navigateTo(Screen.Login)
            return
        }
        viewModelScope.launch {
            val isFav = userFavorites.value.any { it.itemId == itemId }
            if (isFav) {
                repository.removeFavorite(user.id, itemId)
                _toastMessage.value = "Removed from saved items."
            } else {
                repository.toggleFavorite(user.id, itemType, itemId, title, subtitle, imageUrl)
                _toastMessage.value = "Saved to favourites!"
            }
        }
    }

    // --- NOTIFICATIONS ---
    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.id)
            _toastMessage.value = "All notifications marked as read"
        }
    }

    // --- MESSAGING & AI ASSISTANT ---
    fun sendSupportMessage(text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendSupportMessage(
                userId = user.id,
                senderName = user.name,
                senderRole = user.role,
                text = text
            )

            if (_isAiAssistantActive.value) {
                kotlinx.coroutines.delay(600)
                val aiResponse = generateAiAssistantResponse(text)
                repository.sendSupportMessage(
                    userId = user.id,
                    senderName = "MR NexGen Assistant",
                    senderRole = UserRole.ADMIN,
                    text = aiResponse
                )
            }
        }
    }

    private fun generateAiAssistantResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("web") || q.contains("website") || q.contains("portal") ->
                "[AI Assistant] MR NexGen builds scalable web applications using React, Next.js, Laravel, and Node.js microservices. Starting packages begin at ₹25,000. Would you like to submit your project requirements via the Service Request form?"
            q.contains("android") || q.contains("app") || q.contains("mobile") ->
                "[AI Assistant] Our mobile engineering team develops native Android apps with Kotlin and Jetpack Compose adhering to Google Material 3 and offline-first Room databases. Estimated delivery is 3-6 weeks."
            q.contains("training") || q.contains("internship") || q.contains("course") || q.contains("student") ->
                "[AI Assistant] MR NexGen provides certified Summer Training, Winter Training, and 6-Month Industrial Internships on LIVE client codebases with senior mentoring and ISO-verified certificates. You can explore the full course catalog in the Training tab!"
            q.contains("price") || q.contains("cost") || q.contains("budget") || q.contains("quote") ->
                "[AI Assistant] Our pricing is transparent: Web Development from ₹25,000, Android Apps from ₹35,000, UI/UX Systems from ₹20,000, and certified industrial training from ₹8,000. You can request a custom quotation anytime."
            q.contains("admin") || q.contains("human") || q.contains("talk") || q.contains("person") ->
                "[AI Assistant] Switching you to our Human Technical Admin. You can also reach our engineering desk directly at +91 98765 43210 or info@mrnexgen.com."
            else ->
                "[AI Assistant] Hello! I am the MR NexGen Assistant. I can assist you with understanding our IT services, project scopes, industrial training programs, or company information. How may I help you today?"
        }
    }

    // --- SUPPORT TICKETS ---
    fun createSupportTicket(subject: String, category: String, description: String, onDone: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            _toastMessage.value = "Please sign in to raise a support ticket."
            navigateTo(Screen.Login)
            onDone(false)
            return
        }
        viewModelScope.launch {
            val ticket = repository.createSupportTicket(user, subject, category, description)
            _toastMessage.value = "Ticket ${ticket.ticketId} submitted!"
            navigateBack()
            onDone(true)
        }
    }

    fun updateTicketStatus(ticketId: String, status: String, response: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, status, response, admin)
            _toastMessage.value = "Ticket updated to $status"
        }
    }

    // --- FEEDBACK ---
    fun submitFeedback(serviceTitle: String, rating: Int, message: String, onDone: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            _toastMessage.value = "Please sign in to provide feedback."
            navigateTo(Screen.Login)
            onDone(false)
            return
        }
        viewModelScope.launch {
            repository.submitFeedback(user, serviceTitle, rating, message)
            _toastMessage.value = "Thank you for your valuable feedback!"
            navigateBack()
            onDone(true)
        }
    }

    fun markFeedbackReviewed(id: String, reviewed: Boolean) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markFeedbackReviewed(id, reviewed, admin)
            _toastMessage.value = "Feedback marked as reviewed"
        }
    }

    fun submitContact(name: String, email: String, phone: String, subject: String, msg: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val sent = repository.submitContactMessage(name, email, phone, subject, msg)
            _toastMessage.value = "Thank you! Inquiry ${sent.messageId} received."
            onDone(true, sent.messageId)
        }
    }

    // --- ADMIN CRUD ---
    fun saveService(service: ServiceItem) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            if (service.id.isBlank()) {
                repository.createService(service, admin)
                _toastMessage.value = "Service added successfully"
            } else {
                repository.updateService(service, admin)
                _toastMessage.value = "Service updated successfully"
            }
        }
    }

    fun deleteService(serviceId: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteService(serviceId, admin)
            _toastMessage.value = "Service deleted"
        }
    }

    fun savePortfolio(item: PortfolioItem) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.savePortfolio(item, admin)
            _toastMessage.value = "Portfolio project saved"
        }
    }

    fun deletePortfolio(id: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deletePortfolio(id, admin)
            _toastMessage.value = "Portfolio project deleted"
        }
    }

    fun saveBlog(blog: BlogPost) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.saveBlog(blog, admin)
            _toastMessage.value = "Blog saved"
        }
    }

    fun deleteBlog(id: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteBlog(id, admin)
            _toastMessage.value = "Blog deleted"
        }
    }

    fun setUserActive(userId: String, isActive: Boolean) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.setUserActive(userId, isActive, admin)
            _toastMessage.value = if (isActive) "User account activated" else "User account deactivated"
        }
    }

    // --- APPLICATION UPDATE & VERSIONING ---
    fun dismissUpdateDialog() {
        if (!_isForceUpdate.value) {
            _showUpdateDialog.value = false
        }
    }

    fun checkForAppUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            try {
                val policy = repository.getAppVersionPolicy()
                _appVersionInfo.value = policy
                val currentCode = com.example.BuildConfig.VERSION_CODE
                if (policy.forceUpdate || currentCode < policy.minimumSupportedVersionCode) {
                    _isForceUpdate.value = true
                    _showUpdateDialog.value = true
                } else if (policy.latestVersionCode > currentCode) {
                    _isForceUpdate.value = false
                    _showUpdateDialog.value = true
                } else if (isManual) {
                    _toastMessage.value = "MR NexGen is up to date (Version ${com.example.BuildConfig.VERSION_NAME})"
                }
            } catch (e: Exception) {
                if (isManual) {
                    _toastMessage.value = "Unable to check updates. Please try again later."
                }
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    /**
     * Demo / testing utility to simulate a remote backend version update trigger
     */
    fun simulateRemoteUpdateCheck(
        latestVersion: String = "1.0.1",
        latestVersionCode: Int = 2,
        minimumSupportedVersionCode: Int = 1,
        forceUpdate: Boolean = false
    ) {
        viewModelScope.launch {
            val policy = AppVersionInfo(
                latestVersion = latestVersion,
                latestVersionCode = latestVersionCode,
                minimumSupportedVersionCode = minimumSupportedVersionCode,
                minimumSupportedVersion = if (forceUpdate) latestVersion else "1.0.0",
                updateUrl = "market://details?id=${com.example.BuildConfig.APPLICATION_ID}",
                playStoreWebUrl = "https://play.google.com/store/apps/details?id=${com.example.BuildConfig.APPLICATION_ID}",
                forceUpdate = forceUpdate,
                releaseNotes = "New certified internship features, faster response ticketing, and UI optimizations."
            )
            _appVersionInfo.value = policy
            val currentCode = com.example.BuildConfig.VERSION_CODE
            if (policy.forceUpdate || currentCode < policy.minimumSupportedVersionCode) {
                _isForceUpdate.value = true
                _showUpdateDialog.value = true
            } else if (policy.latestVersionCode > currentCode) {
                _isForceUpdate.value = false
                _showUpdateDialog.value = true
            }
        }
    }
}

