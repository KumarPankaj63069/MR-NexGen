package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isVerified = 1, verificationOtp = null WHERE id = :userId")
    suspend fun markUserVerified(userId: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun setUserActiveStatus(userId: String, isActive: Boolean)

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt WHERE id = :userId")
    suspend fun updatePassword(userId: String, newHash: String, newSalt: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isPublished = 1 ORDER BY isFeatured DESC, title ASC")
    fun getPublishedServicesFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY title ASC")
    fun getAllServicesFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: String): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: String)
}

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun getAllRequestsFlow(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsByUserFlow(userId: String): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): ServiceRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity)

    @Update
    suspend fun updateRequest(request: ServiceRequestEntity)

    @Query("UPDATE service_requests SET status = :status, rejectionReason = :reason, adminNotes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: RequestStatus, reason: String, notes: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM service_requests WHERE status = :status")
    fun getCountByStatusFlow(status: RequestStatus): Flow<Int>
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio WHERE isPublished = 1 ORDER BY isFeatured DESC, title ASC")
    fun getPublishedPortfolioFlow(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio ORDER BY title ASC")
    fun getAllPortfolioFlow(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio WHERE id = :id LIMIT 1")
    suspend fun getPortfolioById(id: String): PortfolioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioList(list: List<PortfolioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(item: PortfolioEntity)

    @Update
    suspend fun updatePortfolio(item: PortfolioEntity)

    @Query("DELETE FROM portfolio WHERE id = :id")
    suspend fun deletePortfolio(id: String)
}

@Dao
interface BlogDao {
    @Query("SELECT * FROM blogs WHERE isPublished = 1 ORDER BY publishedAt DESC")
    fun getPublishedBlogsFlow(): Flow<List<BlogEntity>>

    @Query("SELECT * FROM blogs ORDER BY publishedAt DESC")
    fun getAllBlogsFlow(): Flow<List<BlogEntity>>

    @Query("SELECT * FROM blogs WHERE id = :id LIMIT 1")
    suspend fun getBlogById(id: String): BlogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlogs(blogs: List<BlogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlog(blog: BlogEntity)

    @Update
    suspend fun updateBlog(blog: BlogEntity)

    @Query("DELETE FROM blogs WHERE id = :id")
    suspend fun deleteBlog(id: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact_messages ORDER BY createdAt DESC")
    fun getAllMessagesFlow(): Flow<List<ContactMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ContactMessageEntity)

    @Query("UPDATE contact_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getConversationFlow(userId: String): Flow<List<SupportMessageEntity>>

    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun getAllSupportMessagesFlow(): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: SupportMessageEntity)

    @Query("UPDATE support_messages SET isRead = 1 WHERE userId = :userId AND senderRole != 'USER'")
    suspend fun markUserMessagesRead(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCountFlow(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notif: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesFlow(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND itemId = :itemId)")
    fun isFavoriteFlow(userId: String, itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND itemId = :itemId")
    suspend fun removeFavorite(userId: String, itemId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogsFlow(): Flow<List<AdminAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminAuditLogEntity)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTicketsFlow(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTicketsFlow(): Flow<List<SupportTicketEntity>>

    @Query("SELECT COUNT(*) FROM support_tickets WHERE status != 'RESOLVED' AND status != 'CLOSED'")
    fun getPendingTicketsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM support_tickets WHERE userId = :userId AND status != 'RESOLVED' AND status != 'CLOSED'")
    fun getUserPendingTicketsCountFlow(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status, adminResponse = :response, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTicketStatus(id: String, status: String, response: String, updatedAt: Long)
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM client_feedback ORDER BY createdAt DESC")
    fun getAllFeedbackFlow(): Flow<List<FeedbackEntity>>

    @Query("SELECT * FROM client_feedback WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFeedbackFlow(userId: String): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)

    @Query("UPDATE client_feedback SET isReviewed = :reviewed WHERE id = :id")
    suspend fun markReviewed(id: String, reviewed: Boolean)
}

