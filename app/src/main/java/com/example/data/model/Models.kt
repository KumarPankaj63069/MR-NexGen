package com.example.data.model

enum class UserRole {
    USER,
    ADMIN
}

enum class RequestStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val company: String = "",
    val city: String = "",
    val role: UserRole = UserRole.USER,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ServiceItem(
    val id: String,
    val title: String,
    val category: String,
    val shortDesc: String,
    val fullDesc: String,
    val features: List<String> = emptyList(),
    val technologies: List<String> = emptyList(),
    val startingPrice: String = "",
    val deliveryTime: String = "",
    val iconName: String = "Code",
    val imageUrl: String = "",
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false
)

data class ServiceRequest(
    val id: String,
    val requestId: String, // e.g. MRN-SR-2026-00001
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val serviceId: String,
    val serviceTitle: String,
    val projectTitle: String,
    val description: String,
    val budget: String,
    val deadline: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val rejectionReason: String = "",
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PortfolioItem(
    val id: String,
    val title: String,
    val category: String,
    val shortDesc: String,
    val fullDesc: String,
    val clientName: String = "Client Project",
    val projectUrl: String = "https://www.mrnexgen.com",
    val technologies: List<String> = emptyList(),
    val coverImageUrl: String = "",
    val galleryImages: List<String> = emptyList(),
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false
)

data class BlogPost(
    val id: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val category: String,
    val authorName: String = "MR NexGen Tech Team",
    val readTimeMinutes: Int = 5,
    val tags: List<String> = emptyList(),
    val coverImageUrl: String = "",
    val isPublished: Boolean = true,
    val publishedAt: Long = System.currentTimeMillis()
)

data class ContactMessage(
    val id: String,
    val messageId: String, // e.g. MRN-MSG-1001
    val name: String,
    val email: String,
    val phone: String,
    val subject: String,
    val message: String,
    val status: String = "NEW",
    val createdAt: Long = System.currentTimeMillis()
)

data class SupportMessage(
    val id: String,
    val conversationId: String,
    val userId: String,
    val senderName: String,
    val senderRole: UserRole,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "REQUEST", "STATUS", "CHAT", "PROMO", "SYSTEM"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val actionUrl: String = ""
)

data class FavoriteItem(
    val id: String,
    val userId: String,
    val itemType: String, // "SERVICE", "PORTFOLIO", "BLOG"
    val itemId: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String = ""
)

data class AdminAuditLog(
    val id: String,
    val adminId: String,
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class SupportTicket(
    val id: String,
    val ticketId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val subject: String,
    val category: String, // "Technical Issue", "Service Issue", "Payment", "Account", "General Inquiry"
    val description: String,
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
    val adminResponse: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FeedbackItem(
    val id: String,
    val userId: String,
    val userName: String,
    val serviceTitle: String,
    val rating: Int, // 1 to 5 stars
    val message: String,
    val isReviewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TrainingCourse(
    val id: String,
    val title: String,
    val category: String,
    val duration: String,
    val mode: String,
    val certification: String,
    val description: String,
    val technologies: List<String> = emptyList(),
    val isEnrolled: Boolean = false
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
    val token: String? = null,
    val verificationOtp: String? = null // Provided for demo/testing display convenience
)
