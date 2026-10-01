package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RequestStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        ServiceEntity::class,
        ServiceRequestEntity::class,
        PortfolioEntity::class,
        BlogEntity::class,
        ContactMessageEntity::class,
        SupportMessageEntity::class,
        NotificationEntity::class,
        FavoriteEntity::class,
        AdminAuditLogEntity::class,
        SupportTicketEntity::class,
        FeedbackEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serviceDao(): ServiceDao
    abstract fun serviceRequestDao(): ServiceRequestDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun blogDao(): BlogDao
    abstract fun contactDao(): ContactDao
    abstract fun supportDao(): SupportDao
    abstract fun notificationDao(): NotificationDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mr_nexgen_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()

            // 1. Initial Admin User
            val adminSalt = "admin_salt_mrnexgen"
            val adminHash = hashPassword("Admin@123", adminSalt)
            val adminUser = UserEntity(
                id = "usr_admin_001",
                name = "MR NexGen Admin",
                email = "admin@mrnexgen.com",
                phone = "+91 98765 43210",
                company = "MR NexGen IT Services",
                city = "Bangalore",
                passwordHash = adminHash,
                salt = adminSalt,
                role = UserRole.ADMIN,
                isVerified = true,
                isActive = true,
                verificationOtp = null,
                otpExpiry = 0L,
                avatarUrl = "",
                createdAt = now
            )

            // 2. Initial Demo Client User
            val clientSalt = "client_salt_mrnexgen"
            val clientHash = hashPassword("User@123", clientSalt)
            val clientUser = UserEntity(
                id = "usr_client_001",
                name = "Alex Morgan",
                email = "client@mrnexgen.com",
                phone = "+91 91234 56789",
                company = "NexTech Enterprises",
                city = "New Delhi",
                passwordHash = clientHash,
                salt = clientSalt,
                role = UserRole.USER,
                isVerified = true,
                isActive = true,
                verificationOtp = null,
                otpExpiry = 0L,
                avatarUrl = "",
                createdAt = now
            )

            db.userDao().insertUser(adminUser)
            db.userDao().insertUser(clientUser)

            // 3. Initial Services
            val services = listOf(
                ServiceEntity(
                    id = "srv_web_001",
                    title = "Web Development",
                    category = "Development",
                    shortDesc = "Custom web portals, enterprise web apps, high-performance responsive websites.",
                    fullDesc = "MR NexGen delivers end-to-end web engineering solutions tailored to your business needs. From lightweight corporate landing pages to complex multi-tenant SaaS architectures, we combine cutting-edge frontend frameworks with secure backend pipelines.",
                    features = listOf("Custom CMS Integration", "Responsive Mobile-First UI", "High Speed Optimization", "REST & GraphQL APIs", "SSL & Enterprise Security"),
                    technologies = listOf("React", "Next.js", "Laravel", "PHP", "Node.js", "MySQL"),
                    startingPrice = "₹25,000",
                    deliveryTime = "2-4 Weeks",
                    iconName = "Language",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = true
                ),
                ServiceEntity(
                    id = "srv_app_002",
                    title = "Android App Development",
                    category = "Mobile",
                    shortDesc = "Modern native Android apps built with Kotlin, Jetpack Compose, and Clean Architecture.",
                    fullDesc = "We craft beautiful, high-performance mobile applications that adhere strictly to Google's Material 3 design system. Our apps feature offline caching, smooth animations, secure authentication, and seamless backend synchronizations.",
                    features = listOf("Jetpack Compose UI", "Offline-First Room DB", "Push Notifications", "Biometric Auth", "Google Play Store Publishing"),
                    technologies = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Retrofit", "Firebase"),
                    startingPrice = "₹35,000",
                    deliveryTime = "3-6 Weeks",
                    iconName = "Smartphone",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = true
                ),
                ServiceEntity(
                    id = "srv_soft_003",
                    title = "Software Development",
                    category = "Enterprise",
                    shortDesc = "Custom enterprise ERP, billing systems, CRM software, and inventory management.",
                    fullDesc = "Empower your operations with tailor-made desktop and cloud software. We automate manual workflows, build audit-compliant accounting tools, and integrate relational databases designed for scale.",
                    features = listOf("Automated Invoicing", "Multi-branch Management", "Role-Based Access Control", "Data Export/Import", "Automated Backups"),
                    technologies = listOf("Java", "Spring Boot", "Python", "PostgreSQL", "Electron"),
                    startingPrice = "₹50,000",
                    deliveryTime = "4-8 Weeks",
                    iconName = "Terminal",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = true
                ),
                ServiceEntity(
                    id = "srv_uiux_004",
                    title = "UI/UX Design",
                    category = "Design",
                    shortDesc = "User research, wireframing, high-fidelity Figma prototypes, and design systems.",
                    fullDesc = "Human-centered digital product design that converts visitors into loyal clients. We create interactive Figma prototypes, complete component libraries, and cross-platform design guidelines.",
                    features = listOf("User Journey Mapping", "Interactive Prototypes", "Material 3 & iOS Guidelines", "Design Tokens", "Design Handoff Assets"),
                    technologies = listOf("Figma", "Adobe XD", "Design Systems", "Prototyping"),
                    startingPrice = "₹20,000",
                    deliveryTime = "1-3 Weeks",
                    iconName = "Brush",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = false
                ),
                ServiceEntity(
                    id = "srv_dm_005",
                    title = "Digital Marketing & SEO",
                    category = "Marketing",
                    shortDesc = "Data-driven SEO strategies, social media campaigns, Google Ads, and lead generation.",
                    fullDesc = "Accelerate your digital presence and customer acquisition with MR NexGen's comprehensive digital marketing services. We manage PPC campaigns, technical on-page SEO, content marketing, and conversion optimization.",
                    features = listOf("Technical SEO Audit", "Google Ads / PPC", "Social Media Management", "Keyword Analysis", "Monthly Analytics Reports"),
                    technologies = listOf("Google Search Console", "Google Analytics", "SEMrush", "Meta Business"),
                    startingPrice = "₹15,000/mo",
                    deliveryTime = "Ongoing",
                    iconName = "Campaign",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = false
                ),
                ServiceEntity(
                    id = "srv_train_006",
                    title = "IT Training & Internship",
                    category = "Training",
                    shortDesc = "Summer, winter, and 6-month industrial internship programs with live industry projects.",
                    fullDesc = "MR NexGen provides certified industrial training for BCA, B.Tech, MCA, and Diploma students. Learn practical software development directly from industry professionals working on active commercial client projects.",
                    features = listOf("Live Project Development", "Industry Recognized Certificate", "Interview Preparation", "Hands-on Coding Labs", "Placement Assistance"),
                    technologies = listOf("Full Stack Web", "Android Kotlin", "Python & Data", "Java & Spring"),
                    startingPrice = "₹8,000",
                    deliveryTime = "6-12 Weeks",
                    iconName = "School",
                    imageUrl = "",
                    isPublished = true,
                    isFeatured = true
                )
            )
            db.serviceDao().insertServices(services)

            // 4. Initial Portfolio Items
            val portfolioItems = listOf(
                PortfolioEntity(
                    id = "port_001",
                    title = "EduLearn Pro LMS Portal",
                    category = "Websites",
                    shortDesc = "Modern educational platform with video lectures, quiz assessments, and certificate generation.",
                    fullDesc = "A comprehensive Learning Management System built for an educational institute supporting 15,000+ active students with real-time video streaming, graded quizzes, and automated PDF certification.",
                    clientName = "EduGlobal Academy",
                    projectUrl = "https://www.mrnexgen.com",
                    technologies = listOf("React", "Laravel", "MySQL", "AWS S3"),
                    coverImageUrl = "",
                    galleryImages = emptyList(),
                    isPublished = true,
                    isFeatured = true
                ),
                PortfolioEntity(
                    id = "port_002",
                    title = "ApexTrack GPS & Fleet Manager",
                    category = "Android Apps",
                    shortDesc = "Android mobile app for real-time fleet vehicle tracking and dispatch management.",
                    fullDesc = "Enterprise mobile solution providing GPS telemetry, route replay, driver behavior analytics, and geofence alerts for freight transport companies.",
                    clientName = "Apex Logistics",
                    projectUrl = "https://www.mrnexgen.com",
                    technologies = listOf("Kotlin", "Jetpack Compose", "Google Maps SDK", "WebSockets"),
                    coverImageUrl = "",
                    galleryImages = emptyList(),
                    isPublished = true,
                    isFeatured = true
                ),
                PortfolioEntity(
                    id = "port_003",
                    title = "MediCare Health Cloud ERP",
                    category = "Software",
                    shortDesc = "Multi-specialty hospital management system with electronic health records.",
                    fullDesc = "An integrated clinical software handling patient OPD/IPD admissions, doctor scheduling, pharmacy inventory, lab report dispatch, and GST-compliant invoicing.",
                    clientName = "City Health Clinic",
                    projectUrl = "https://www.mrnexgen.com",
                    technologies = listOf("Java", "Spring Boot", "PostgreSQL", "Angular"),
                    coverImageUrl = "",
                    galleryImages = emptyList(),
                    isPublished = true,
                    isFeatured = true
                )
            )
            db.portfolioDao().insertPortfolioList(portfolioItems)

            // 5. Initial Blog Posts
            val blogs = listOf(
                BlogEntity(
                    id = "blog_001",
                    title = "Modern Android Development: Jetpack Compose & Clean Architecture",
                    excerpt = "How reactive declarative UI paired with M3 design elevates mobile product quality.",
                    content = "Modern Android development has undergone a massive evolution with the adoption of Jetpack Compose. By adopting a declarative programming paradigm, developers eliminate boilerplate XML layouts and decouple UI rendering from business logic.\n\nIn this article, we explore how MR NexGen builds robust, offline-capable mobile apps using Kotlin Coroutines, Flow, and Room Database.",
                    category = "Mobile Development",
                    authorName = "MR NexGen Tech Team",
                    readTimeMinutes = 4,
                    tags = listOf("Android", "Kotlin", "Compose", "Architecture"),
                    coverImageUrl = "",
                    isPublished = true,
                    publishedAt = now - 86400000L * 2
                ),
                BlogEntity(
                    id = "blog_002",
                    title = "Why Industrial Training on Live Projects is Crucial for Freshers",
                    excerpt = "Bridge the gap between academic textbooks and real corporate IT workflows.",
                    content = "Academic degrees teach theoretical fundamentals, but corporate tech teams look for hands-on problem-solving, version control skills, and API integration capabilities.\n\nAt MR NexGen, our summer and industrial training programs place interns directly into active client codebases with senior developer mentorship.",
                    category = "Career & Training",
                    authorName = "Priya Sharma, Lead Mentor",
                    readTimeMinutes = 5,
                    tags = listOf("Training", "Internship", "Career", "Web Dev"),
                    coverImageUrl = "",
                    isPublished = true,
                    publishedAt = now - 86400000L * 5
                )
            )
            db.blogDao().insertBlogs(blogs)

            // 6. Sample Demo Service Request
            val demoRequest = ServiceRequestEntity(
                id = "req_demo_001",
                requestId = "MRN-SR-2026-00001",
                userId = "usr_client_001",
                userName = "Alex Morgan",
                userEmail = "client@mrnexgen.com",
                userPhone = "+91 91234 56789",
                serviceId = "srv_web_001",
                serviceTitle = "Web Development",
                projectTitle = "Corporate E-Commerce Portal",
                description = "We require a B2B product catalog portal with user authentication, quotation generator, and PDF invoice export.",
                budget = "₹40,000 - ₹60,000",
                deadline = "4 Weeks",
                status = RequestStatus.UNDER_REVIEW,
                rejectionReason = "",
                adminNotes = "Initial requirements verified by technical architect. Preparing project roadmap.",
                createdAt = now - 86400000L,
                updatedAt = now - 3600000L
            )
            db.serviceRequestDao().insertRequest(demoRequest)

            // 7. Initial Notification for Client
            val notif = NotificationEntity(
                id = "notif_001",
                userId = "usr_client_001",
                title = "Service Request Received",
                message = "Your request MRN-SR-2026-00001 for Web Development is now under review by our tech team.",
                type = "STATUS",
                isRead = false,
                createdAt = now - 3600000L,
                actionUrl = ""
            )
            db.notificationDao().insertNotification(notif)

            // 8. Sample Support Ticket for Client
            val demoTicket = SupportTicketEntity(
                id = "tck_001",
                ticketId = "MRN-TCK-1001",
                userId = "usr_client_001",
                userName = "Alex Morgan",
                userEmail = "client@mrnexgen.com",
                subject = "API Integration Query for E-Commerce",
                category = "Technical Issue",
                description = "Need clarification on payment gateway webhook endpoints for the React/Laravel portal.",
                status = "OPEN",
                adminResponse = "Technical lead assigned. Documentation shared via portal.",
                createdAt = now - 7200000L,
                updatedAt = now - 1800000L
            )
            db.supportTicketDao().insertTicket(demoTicket)

            // 9. Sample Feedback
            val demoFeedback = FeedbackEntity(
                id = "fb_001",
                userId = "usr_client_001",
                userName = "Alex Morgan",
                serviceTitle = "Web Development",
                rating = 5,
                message = "Exceptional code quality and timely delivery from MR NexGen team! Highly recommended.",
                isReviewed = true,
                createdAt = now - 86400000L
            )
            db.feedbackDao().insertFeedback(demoFeedback)
        }

        fun hashPassword(password: String, salt: String): String {
            val bytes = (password + salt).toByteArray(Charsets.UTF_8)
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
