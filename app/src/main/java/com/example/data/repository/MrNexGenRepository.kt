package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MrNexGenRepository(private val db: AppDatabase) {

    // --- MAPPERS ---
    fun UserEntity.toModel(): User = User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        company = company,
        city = city,
        role = role,
        isVerified = isVerified,
        isActive = isActive,
        avatarUrl = avatarUrl,
        createdAt = createdAt
    )

    fun ServiceEntity.toModel(): ServiceItem = ServiceItem(
        id = id,
        title = title,
        category = category,
        shortDesc = shortDesc,
        fullDesc = fullDesc,
        features = features,
        technologies = technologies,
        startingPrice = startingPrice,
        deliveryTime = deliveryTime,
        iconName = iconName,
        imageUrl = imageUrl,
        isPublished = isPublished,
        isFeatured = isFeatured
    )

    fun ServiceRequestEntity.toModel(): ServiceRequest = ServiceRequest(
        id = id,
        requestId = requestId,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        userPhone = userPhone,
        serviceId = serviceId,
        serviceTitle = serviceTitle,
        projectTitle = projectTitle,
        description = description,
        budget = budget,
        deadline = deadline,
        status = status,
        rejectionReason = rejectionReason,
        adminNotes = adminNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun PortfolioEntity.toModel(): PortfolioItem = PortfolioItem(
        id = id,
        title = title,
        category = category,
        shortDesc = shortDesc,
        fullDesc = fullDesc,
        clientName = clientName,
        projectUrl = projectUrl,
        technologies = technologies,
        coverImageUrl = coverImageUrl,
        galleryImages = galleryImages,
        isPublished = isPublished,
        isFeatured = isFeatured
    )

    fun BlogEntity.toModel(): BlogPost = BlogPost(
        id = id,
        title = title,
        excerpt = excerpt,
        content = content,
        category = category,
        authorName = authorName,
        readTimeMinutes = readTimeMinutes,
        tags = tags,
        coverImageUrl = coverImageUrl,
        isPublished = isPublished,
        publishedAt = publishedAt
    )

    fun ContactMessageEntity.toModel(): ContactMessage = ContactMessage(
        id = id,
        messageId = messageId,
        name = name,
        email = email,
        phone = phone,
        subject = subject,
        message = message,
        status = status,
        createdAt = createdAt
    )

    fun SupportMessageEntity.toModel(): SupportMessage = SupportMessage(
        id = id,
        conversationId = conversationId,
        userId = userId,
        senderName = senderName,
        senderRole = senderRole,
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )

    fun NotificationEntity.toModel(): AppNotification = AppNotification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        isRead = isRead,
        createdAt = createdAt,
        actionUrl = actionUrl
    )

    fun FavoriteEntity.toModel(): FavoriteItem = FavoriteItem(
        id = id,
        userId = userId,
        itemType = itemType,
        itemId = itemId,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl
    )

    fun AdminAuditLogEntity.toModel(): AdminAuditLog = AdminAuditLog(
        id = id,
        adminId = adminId,
        adminName = adminName,
        action = action,
        target = target,
        timestamp = timestamp,
        details = details
    )

    fun SupportTicketEntity.toModel(): SupportTicket = SupportTicket(
        id = id,
        ticketId = ticketId,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        subject = subject,
        category = category,
        description = description,
        status = status,
        adminResponse = adminResponse,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun FeedbackEntity.toModel(): FeedbackItem = FeedbackItem(
        id = id,
        userId = userId,
        userName = userName,
        serviceTitle = serviceTitle,
        rating = rating,
        message = message,
        isReviewed = isReviewed,
        createdAt = createdAt
    )

    suspend fun getUserById(userId: String): User? =
        db.userDao().getUserById(userId)?.toModel()

    // --- AUTHENTICATION ---
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        company: String = "",
        city: String = ""
    ): AuthResponse {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return AuthResponse(false, "Please provide a valid email address.")
        }
        if (password.length < 6) {
            return AuthResponse(false, "Password must be at least 6 characters.")
        }
        if (name.isBlank() || phone.isBlank()) {
            return AuthResponse(false, "Name and mobile number are required.")
        }

        val existing = db.userDao().getUserByEmail(trimmedEmail)
        if (existing != null) {
            return AuthResponse(false, "An account with this email already exists.")
        }

        val salt = UUID.randomUUID().toString().substring(0, 8)
        val hash = AppDatabase.hashPassword(password, salt)
        val otp = (100000 + (Math.random() * 900000).toInt()).toString()
        val expiry = System.currentTimeMillis() + 15 * 60 * 1000 // 15 mins

        val userEntity = UserEntity(
            id = "usr_" + UUID.randomUUID().toString().substring(0, 8),
            name = name.trim(),
            email = trimmedEmail,
            phone = phone.trim(),
            company = company.trim(),
            city = city.trim(),
            passwordHash = hash,
            salt = salt,
            role = UserRole.USER,
            isVerified = false,
            isActive = true,
            verificationOtp = otp,
            otpExpiry = expiry,
            avatarUrl = "",
            createdAt = System.currentTimeMillis()
        )

        db.userDao().insertUser(userEntity)

        // Generate welcoming notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userEntity.id,
                title = "Welcome to MR NexGen!",
                message = "Your verification OTP code is $otp. Enter it to activate your account.",
                type = "SYSTEM",
                isRead = false,
                createdAt = System.currentTimeMillis(),
                actionUrl = ""
            )
        )

        return AuthResponse(
            success = true,
            message = "Registration successful! A verification code has been dispatched to $trimmedEmail.",
            user = userEntity.toModel(),
            verificationOtp = otp
        )
    }

    suspend fun verifyEmail(email: String, otp: String): AuthResponse {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
            ?: return AuthResponse(false, "User not found.")

        if (user.isVerified) {
            return AuthResponse(true, "Account is already verified. You can log in.", user.toModel())
        }

        if (user.verificationOtp != otp.trim()) {
            return AuthResponse(false, "Invalid verification code.")
        }

        if (System.currentTimeMillis() > user.otpExpiry) {
            return AuthResponse(false, "Verification code has expired. Please request a new one.")
        }

        db.userDao().markUserVerified(user.id)
        val updated = db.userDao().getUserById(user.id)
        return AuthResponse(true, "Email verified successfully! You may now sign in.", updated?.toModel())
    }

    suspend fun resendVerificationOtp(email: String): AuthResponse {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
            ?: return AuthResponse(false, "No account found with this email.")

        if (user.isVerified) {
            return AuthResponse(true, "Account is already verified.", user.toModel())
        }

        val newOtp = (100000 + (Math.random() * 900000).toInt()).toString()
        val expiry = System.currentTimeMillis() + 15 * 60 * 1000
        val updated = user.copy(verificationOtp = newOtp, otpExpiry = expiry)
        db.userDao().updateUser(updated)

        return AuthResponse(
            success = true,
            message = "New verification code has been sent.",
            verificationOtp = newOtp
        )
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
            ?: return AuthResponse(false, "Invalid email or password.")

        val computedHash = AppDatabase.hashPassword(password, user.salt)
        if (computedHash != user.passwordHash) {
            return AuthResponse(false, "Invalid email or password.")
        }

        if (!user.isActive) {
            return AuthResponse(false, "Your account has been deactivated. Please contact support.")
        }

        if (!user.isVerified) {
            return AuthResponse(
                success = false,
                message = "Please verify your email address before logging in.",
                user = user.toModel(),
                verificationOtp = user.verificationOtp
            )
        }

        val sessionToken = "jwt_" + UUID.randomUUID().toString()
        return AuthResponse(
            success = true,
            message = "Login successful.",
            user = user.toModel(),
            token = sessionToken
        )
    }

    suspend fun forgotPassword(email: String): AuthResponse {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
            ?: return AuthResponse(false, "No account found with this email.")

        val otp = (100000 + (Math.random() * 900000).toInt()).toString()
        val expiry = System.currentTimeMillis() + 15 * 60 * 1000
        db.userDao().updateUser(user.copy(verificationOtp = otp, otpExpiry = expiry))

        return AuthResponse(
            success = true,
            message = "Password reset code sent to your email.",
            verificationOtp = otp
        )
    }

    suspend fun resetPassword(email: String, otp: String, newPass: String): AuthResponse {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
            ?: return AuthResponse(false, "User not found.")

        if (user.verificationOtp != otp.trim()) {
            return AuthResponse(false, "Invalid reset code.")
        }
        if (System.currentTimeMillis() > user.otpExpiry) {
            return AuthResponse(false, "Reset code has expired.")
        }
        if (newPass.length < 6) {
            return AuthResponse(false, "New password must be at least 6 characters.")
        }

        val newSalt = UUID.randomUUID().toString().substring(0, 8)
        val newHash = AppDatabase.hashPassword(newPass, newSalt)
        db.userDao().updatePassword(user.id, newHash, newSalt)

        return AuthResponse(true, "Password updated successfully. Please log in.")
    }

    suspend fun updateProfile(
        userId: String,
        name: String,
        phone: String,
        company: String,
        city: String
    ): User? {
        val user = db.userDao().getUserById(userId) ?: return null
        val updated = user.copy(
            name = name.trim(),
            phone = phone.trim(),
            company = company.trim(),
            city = city.trim()
        )
        db.userDao().updateUser(updated)
        return updated.toModel()
    }

    suspend fun changePassword(userId: String, oldPass: String, newPass: String): Boolean {
        val user = db.userDao().getUserById(userId) ?: return false
        val computedOld = AppDatabase.hashPassword(oldPass, user.salt)
        if (computedOld != user.passwordHash) return false
        val newSalt = UUID.randomUUID().toString().substring(0, 8)
        val newHash = AppDatabase.hashPassword(newPass, newSalt)
        db.userDao().updatePassword(userId, newHash, newSalt)
        return true
    }

    suspend fun deleteAccount(userId: String): Boolean {
        db.userDao().deleteUser(userId)
        return true
    }

    // --- SERVICES ---
    fun getPublishedServicesFlow(): Flow<List<ServiceItem>> =
        db.serviceDao().getPublishedServicesFlow().map { list -> list.map { it.toModel() } }

    fun getAllServicesFlow(): Flow<List<ServiceItem>> =
        db.serviceDao().getAllServicesFlow().map { list -> list.map { it.toModel() } }

    suspend fun createService(service: ServiceItem, admin: User) {
        val entity = ServiceEntity(
            id = "srv_" + UUID.randomUUID().toString().substring(0, 8),
            title = service.title,
            category = service.category,
            shortDesc = service.shortDesc,
            fullDesc = service.fullDesc,
            features = service.features,
            technologies = service.technologies,
            startingPrice = service.startingPrice,
            deliveryTime = service.deliveryTime,
            iconName = service.iconName,
            imageUrl = service.imageUrl,
            isPublished = service.isPublished,
            isFeatured = service.isFeatured
        )
        db.serviceDao().insertService(entity)
        logAdminAction(admin.id, admin.name, "CREATE_SERVICE", entity.title, "Created new service")
    }

    suspend fun updateService(service: ServiceItem, admin: User) {
        val entity = ServiceEntity(
            id = service.id,
            title = service.title,
            category = service.category,
            shortDesc = service.shortDesc,
            fullDesc = service.fullDesc,
            features = service.features,
            technologies = service.technologies,
            startingPrice = service.startingPrice,
            deliveryTime = service.deliveryTime,
            iconName = service.iconName,
            imageUrl = service.imageUrl,
            isPublished = service.isPublished,
            isFeatured = service.isFeatured
        )
        db.serviceDao().updateService(entity)
        logAdminAction(admin.id, admin.name, "UPDATE_SERVICE", entity.title, "Updated service details")
    }

    suspend fun deleteService(serviceId: String, admin: User) {
        db.serviceDao().deleteService(serviceId)
        logAdminAction(admin.id, admin.name, "DELETE_SERVICE", serviceId, "Deleted service")
    }

    // --- SERVICE REQUESTS ---
    fun getUserRequestsFlow(userId: String): Flow<List<ServiceRequest>> =
        db.serviceRequestDao().getRequestsByUserFlow(userId).map { list -> list.map { it.toModel() } }

    fun getAllRequestsFlow(): Flow<List<ServiceRequest>> =
        db.serviceRequestDao().getAllRequestsFlow().map { list -> list.map { it.toModel() } }

    suspend fun submitServiceRequest(
        user: User,
        serviceId: String,
        serviceTitle: String,
        projectTitle: String,
        description: String,
        budget: String,
        deadline: String
    ): ServiceRequest {
        val now = System.currentTimeMillis()
        val randomSuffix = (10000 + (Math.random() * 90000).toInt()).toString()
        val customId = "MRN-SR-2026-$randomSuffix"

        val entity = ServiceRequestEntity(
            id = "req_" + UUID.randomUUID().toString().substring(0, 8),
            requestId = customId,
            userId = user.id,
            userName = user.name,
            userEmail = user.email,
            userPhone = user.phone,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            projectTitle = projectTitle,
            description = description,
            budget = budget,
            deadline = deadline,
            status = RequestStatus.PENDING,
            rejectionReason = "",
            adminNotes = "New request received from client portal.",
            createdAt = now,
            updatedAt = now
        )

        db.serviceRequestDao().insertRequest(entity)

        // Notification for user
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = "Service Request Submitted",
                message = "Your request $customId for $serviceTitle has been received and queued for review.",
                type = "REQUEST",
                isRead = false,
                createdAt = now,
                actionUrl = customId
            )
        )

        return entity.toModel()
    }

    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: RequestStatus,
        rejectionReason: String,
        adminNotes: String,
        admin: User
    ) {
        val req = db.serviceRequestDao().getRequestById(requestId) ?: return
        val now = System.currentTimeMillis()
        db.serviceRequestDao().updateStatus(requestId, newStatus, rejectionReason, adminNotes, now)

        val statusLabel = when (newStatus) {
            RequestStatus.PENDING -> "Pending"
            RequestStatus.UNDER_REVIEW -> "Under Review"
            RequestStatus.APPROVED -> "Approved"
            RequestStatus.IN_PROGRESS -> "In Progress"
            RequestStatus.COMPLETED -> "Completed"
            RequestStatus.REJECTED -> "Rejected"
        }

        // Notification to user
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = req.userId,
                title = "Request Status Update",
                message = "Your request ${req.requestId} (${req.serviceTitle}) is now $statusLabel.",
                type = "STATUS",
                isRead = false,
                createdAt = now,
                actionUrl = req.requestId
            )
        )

        logAdminAction(
            admin.id,
            admin.name,
            "UPDATE_REQUEST_STATUS",
            req.requestId,
            "Changed status to $statusLabel"
        )
    }

    // --- PORTFOLIO ---
    fun getPortfolioFlow(): Flow<List<PortfolioItem>> =
        db.portfolioDao().getPublishedPortfolioFlow().map { list -> list.map { it.toModel() } }

    fun getAllPortfolioFlow(): Flow<List<PortfolioItem>> =
        db.portfolioDao().getAllPortfolioFlow().map { list -> list.map { it.toModel() } }

    suspend fun savePortfolio(item: PortfolioItem, admin: User) {
        val entity = PortfolioEntity(
            id = if (item.id.isBlank()) "port_" + UUID.randomUUID().toString().substring(0, 8) else item.id,
            title = item.title,
            category = item.category,
            shortDesc = item.shortDesc,
            fullDesc = item.fullDesc,
            clientName = item.clientName,
            projectUrl = item.projectUrl,
            technologies = item.technologies,
            coverImageUrl = item.coverImageUrl,
            galleryImages = item.galleryImages,
            isPublished = item.isPublished,
            isFeatured = item.isFeatured
        )
        if (item.id.isBlank()) {
            db.portfolioDao().insertPortfolio(entity)
            logAdminAction(admin.id, admin.name, "CREATE_PORTFOLIO", entity.title, "Added project")
        } else {
            db.portfolioDao().updatePortfolio(entity)
            logAdminAction(admin.id, admin.name, "UPDATE_PORTFOLIO", entity.title, "Updated project")
        }
    }

    suspend fun deletePortfolio(id: String, admin: User) {
        db.portfolioDao().deletePortfolio(id)
        logAdminAction(admin.id, admin.name, "DELETE_PORTFOLIO", id, "Deleted project")
    }

    // --- BLOGS ---
    fun getBlogsFlow(): Flow<List<BlogPost>> =
        db.blogDao().getPublishedBlogsFlow().map { list -> list.map { it.toModel() } }

    fun getAllBlogsFlow(): Flow<List<BlogPost>> =
        db.blogDao().getAllBlogsFlow().map { list -> list.map { it.toModel() } }

    suspend fun saveBlog(blog: BlogPost, admin: User) {
        val entity = BlogEntity(
            id = if (blog.id.isBlank()) "blog_" + UUID.randomUUID().toString().substring(0, 8) else blog.id,
            title = blog.title,
            excerpt = blog.excerpt,
            content = blog.content,
            category = blog.category,
            authorName = blog.authorName,
            readTimeMinutes = blog.readTimeMinutes,
            tags = blog.tags,
            coverImageUrl = blog.coverImageUrl,
            isPublished = blog.isPublished,
            publishedAt = if (blog.publishedAt == 0L) System.currentTimeMillis() else blog.publishedAt
        )
        if (blog.id.isBlank()) {
            db.blogDao().insertBlog(entity)
            logAdminAction(admin.id, admin.name, "CREATE_BLOG", entity.title, "Created blog post")
        } else {
            db.blogDao().updateBlog(entity)
            logAdminAction(admin.id, admin.name, "UPDATE_BLOG", entity.title, "Updated blog post")
        }
    }

    suspend fun deleteBlog(id: String, admin: User) {
        db.blogDao().deleteBlog(id)
        logAdminAction(admin.id, admin.name, "DELETE_BLOG", id, "Deleted blog post")
    }

    // --- CONTACT ---
    fun getAllContactMessagesFlow(): Flow<List<ContactMessage>> =
        db.contactDao().getAllMessagesFlow().map { list -> list.map { it.toModel() } }

    suspend fun submitContactMessage(
        name: String,
        email: String,
        phone: String,
        subject: String,
        message: String
    ): ContactMessage {
        val randomNum = (1000 + (Math.random() * 9000).toInt()).toString()
        val msgId = "MRN-MSG-$randomNum"
        val entity = ContactMessageEntity(
            id = "msg_" + UUID.randomUUID().toString().substring(0, 8),
            messageId = msgId,
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            subject = subject.trim(),
            message = message.trim(),
            status = "NEW",
            createdAt = System.currentTimeMillis()
        )
        db.contactDao().insertMessage(entity)
        return entity.toModel()
    }

    suspend fun markContactMessageStatus(id: String, status: String) {
        db.contactDao().updateStatus(id, status)
    }

    // --- SUPPORT MESSAGING ---
    fun getConversationFlow(userId: String): Flow<List<SupportMessage>> =
        db.supportDao().getConversationFlow(userId).map { list -> list.map { it.toModel() } }

    suspend fun sendSupportMessage(
        userId: String,
        senderName: String,
        senderRole: UserRole,
        text: String
    ): SupportMessage {
        val entity = SupportMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = "conv_$userId",
            userId = userId,
            senderName = senderName,
            senderRole = senderRole,
            message = text.trim(),
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        db.supportDao().insertMessage(entity)
        return entity.toModel()
    }

    suspend fun markUserSupportMessagesRead(userId: String) {
        db.supportDao().markUserMessagesRead(userId)
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsFlow(userId: String): Flow<List<AppNotification>> =
        db.notificationDao().getNotificationsFlow(userId).map { list -> list.map { it.toModel() } }

    fun getUnreadNotificationsCountFlow(userId: String): Flow<Int> =
        db.notificationDao().getUnreadCountFlow(userId)

    suspend fun markNotificationRead(id: String) {
        db.notificationDao().markRead(id)
    }

    suspend fun markAllNotificationsRead(userId: String) {
        db.notificationDao().markAllRead(userId)
    }

    // --- FAVORITES ---
    fun getFavoritesFlow(userId: String): Flow<List<FavoriteItem>> =
        db.favoriteDao().getFavoritesFlow(userId).map { list -> list.map { it.toModel() } }

    fun isFavoriteFlow(userId: String, itemId: String): Flow<Boolean> =
        db.favoriteDao().isFavoriteFlow(userId, itemId)

    suspend fun toggleFavorite(
        userId: String,
        itemType: String,
        itemId: String,
        title: String,
        subtitle: String,
        imageUrl: String = ""
    ) {
        val exists = db.favoriteDao().isFavoriteFlow(userId, itemId)
        // Check in DB
        db.favoriteDao().removeFavorite(userId, itemId)
        val entity = FavoriteEntity(
            id = "fav_${userId}_$itemId",
            userId = userId,
            itemType = itemType,
            itemId = itemId,
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl
        )
        db.favoriteDao().insertFavorite(entity)
    }

    suspend fun removeFavorite(userId: String, itemId: String) {
        db.favoriteDao().removeFavorite(userId, itemId)
    }

    // --- ADMIN USERS & AUDIT ---
    fun getAllUsersFlow(): Flow<List<User>> =
        db.userDao().getAllUsersFlow().map { list -> list.map { it.toModel() } }

    suspend fun setUserActive(userId: String, isActive: Boolean, admin: User) {
        db.userDao().setUserActiveStatus(userId, isActive)
        logAdminAction(
            admin.id,
            admin.name,
            "TOGGLE_USER_STATUS",
            userId,
            "Set active = $isActive"
        )
    }

    fun getAuditLogsFlow(): Flow<List<AdminAuditLog>> =
        db.auditLogDao().getAuditLogsFlow().map { list -> list.map { it.toModel() } }

    // --- SUPPORT TICKETS ---
    fun getUserTicketsFlow(userId: String): Flow<List<SupportTicket>> =
        db.supportTicketDao().getUserTicketsFlow(userId).map { list -> list.map { it.toModel() } }

    fun getAllTicketsFlow(): Flow<List<SupportTicket>> =
        db.supportTicketDao().getAllTicketsFlow().map { list -> list.map { it.toModel() } }

    fun getUserPendingTicketsCountFlow(userId: String): Flow<Int> =
        db.supportTicketDao().getUserPendingTicketsCountFlow(userId)

    fun getPendingTicketsCountFlow(): Flow<Int> =
        db.supportTicketDao().getPendingTicketsCountFlow()

    suspend fun createSupportTicket(
        user: User,
        subject: String,
        category: String,
        description: String
    ): SupportTicket {
        val now = System.currentTimeMillis()
        val num = (1000 + (Math.random() * 9000).toInt()).toString()
        val ticketId = "MRN-TCK-$num"
        val entity = SupportTicketEntity(
            id = "tck_" + UUID.randomUUID().toString().substring(0, 8),
            ticketId = ticketId,
            userId = user.id,
            userName = user.name,
            userEmail = user.email,
            subject = subject.trim(),
            category = category,
            description = description.trim(),
            status = "OPEN",
            adminResponse = "Ticket received. Assigned to technical specialist.",
            createdAt = now,
            updatedAt = now
        )
        db.supportTicketDao().insertTicket(entity)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = "Support Ticket Created",
                message = "Your ticket $ticketId ($subject) has been submitted.",
                type = "SUPPORT",
                isRead = false,
                createdAt = now,
                actionUrl = ticketId
            )
        )
        return entity.toModel()
    }

    suspend fun updateTicketStatus(
        ticketId: String,
        status: String,
        response: String,
        admin: User
    ) {
        val now = System.currentTimeMillis()
        db.supportTicketDao().updateTicketStatus(ticketId, status, response, now)
        logAdminAction(admin.id, admin.name, "UPDATE_TICKET", ticketId, "Status set to $status")
    }

    // --- CLIENT FEEDBACK ---
    fun getAllFeedbackFlow(): Flow<List<FeedbackItem>> =
        db.feedbackDao().getAllFeedbackFlow().map { list -> list.map { it.toModel() } }

    fun getUserFeedbackFlow(userId: String): Flow<List<FeedbackItem>> =
        db.feedbackDao().getUserFeedbackFlow(userId).map { list -> list.map { it.toModel() } }

    suspend fun submitFeedback(
        user: User,
        serviceTitle: String,
        rating: Int,
        message: String
    ): FeedbackItem {
        val entity = FeedbackEntity(
            id = "fb_" + UUID.randomUUID().toString().substring(0, 8),
            userId = user.id,
            userName = user.name,
            serviceTitle = serviceTitle.ifBlank { "MR NexGen IT Services" },
            rating = rating.coerceIn(1, 5),
            message = message.trim(),
            isReviewed = false,
            createdAt = System.currentTimeMillis()
        )
        db.feedbackDao().insertFeedback(entity)
        return entity.toModel()
    }

    suspend fun markFeedbackReviewed(id: String, reviewed: Boolean, admin: User) {
        db.feedbackDao().markReviewed(id, reviewed)
        logAdminAction(admin.id, admin.name, "REVIEW_FEEDBACK", id, "Reviewed: $reviewed")
    }

    // --- TRAINING COURSES ---
    fun getTrainingCourses(): List<TrainingCourse> = listOf(
        TrainingCourse(
            id = "trn_001",
            title = "Summer Industrial Training",
            category = "Live Project Program",
            duration = "6-8 Weeks",
            mode = "Classroom & Online",
            certification = "Corporate Verified Certificate",
            description = "Specialized summer internship for BCA, B.Tech, MCA students covering Full Stack Web, Android Kotlin, and cloud databases with live client modules.",
            technologies = listOf("Kotlin", "Jetpack Compose", "React", "Laravel", "MySQL")
        ),
        TrainingCourse(
            id = "trn_002",
            title = "Winter Industrial Training",
            category = "Fast-track Program",
            duration = "4-6 Weeks",
            mode = "Online & Hybrid",
            certification = "Industry Recognized Certificate",
            description = "Intensive winter coding program focused on real-world Git version control, REST APIs, cloud deployments, and placement interview prep.",
            technologies = listOf("Java", "Spring Boot", "Python", "Docker")
        ),
        TrainingCourse(
            id = "trn_003",
            title = "6-Month Industrial Internship",
            category = "Comprehensive Apprenticeship",
            duration = "6 Months",
            mode = "Corporate Onsite / Hybrid",
            certification = "Experience Letter & Certificate",
            description = "Full semester commercial apprenticeship working directly alongside senior software architects on active MR NexGen client systems.",
            technologies = listOf("Full Stack", "Clean Architecture", "CI/CD", "AWS", "PostgreSQL")
        ),
        TrainingCourse(
            id = "trn_004",
            title = "Modern Android Development (Kotlin Compose)",
            category = "Specialized Track",
            duration = "8 Weeks",
            mode = "Online Live Mentorship",
            certification = "MR NexGen Specialist Certificate",
            description = "Master Jetpack Compose, Room persistence, Coroutines, Flow, Retrofit, and Material 3 design from scratch to Play Store publishing.",
            technologies = listOf("Kotlin", "Jetpack Compose", "Room DB", "Coroutines")
        ),
        TrainingCourse(
            id = "trn_005",
            title = "Full-Stack Web & API Engineering",
            category = "Specialized Track",
            duration = "10 Weeks",
            mode = "Classroom & Hands-on Lab",
            certification = "Certified Full-Stack Developer",
            description = "Build high-throughput web portals with React frontend, Node/Laravel backend APIs, authentication pipelines, and PostgreSQL databases.",
            technologies = listOf("React.js", "Node.js", "Laravel", "Tailwind CSS")
        )
    )

    private suspend fun logAdminAction(
        adminId: String,
        adminName: String,
        action: String,
        target: String,
        details: String
    ) {
        db.auditLogDao().insertLog(
            AdminAuditLogEntity(
                id = UUID.randomUUID().toString(),
                adminId = adminId,
                adminName = adminName,
                action = action,
                target = target,
                timestamp = System.currentTimeMillis(),
                details = details
            )
        )
    }
}
