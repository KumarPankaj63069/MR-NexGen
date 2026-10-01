package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.RequestStatus
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val company: String,
    val city: String,
    val passwordHash: String,
    val salt: String,
    val role: UserRole,
    val isVerified: Boolean,
    val isActive: Boolean,
    val verificationOtp: String?,
    val otpExpiry: Long,
    val avatarUrl: String,
    val createdAt: Long
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val shortDesc: String,
    val fullDesc: String,
    val features: List<String>,
    val technologies: List<String>,
    val startingPrice: String,
    val deliveryTime: String,
    val iconName: String,
    val imageUrl: String,
    val isPublished: Boolean,
    val isFeatured: Boolean
)

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey val id: String,
    val requestId: String,
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
    val status: RequestStatus,
    val rejectionReason: String,
    val adminNotes: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val shortDesc: String,
    val fullDesc: String,
    val clientName: String,
    val projectUrl: String,
    val technologies: List<String>,
    val coverImageUrl: String,
    val galleryImages: List<String>,
    val isPublished: Boolean,
    val isFeatured: Boolean
)

@Entity(tableName = "blogs")
data class BlogEntity(
    @PrimaryKey val id: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val category: String,
    val authorName: String,
    val readTimeMinutes: Int,
    val tags: List<String>,
    val coverImageUrl: String,
    val isPublished: Boolean,
    val publishedAt: Long
)

@Entity(tableName = "contact_messages")
data class ContactMessageEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val name: String,
    val email: String,
    val phone: String,
    val subject: String,
    val message: String,
    val status: String,
    val createdAt: Long
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val userId: String,
    val senderName: String,
    val senderRole: UserRole,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Long,
    val actionUrl: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val itemType: String,
    val itemId: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
    @PrimaryKey val id: String,
    val adminId: String,
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long,
    val details: String
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val subject: String,
    val category: String,
    val description: String,
    val status: String, // "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
    val adminResponse: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "client_feedback")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val serviceTitle: String,
    val rating: Int,
    val message: String,
    val isReviewed: Boolean,
    val createdAt: Long
)

