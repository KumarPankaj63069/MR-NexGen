package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RequestStatus
import com.example.ui.components.BlogCard
import com.example.ui.components.MainDashboardSummary
import com.example.ui.components.PortfolioCard
import com.example.ui.components.ServiceCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val services by viewModel.publishedServices.collectAsState()
    val portfolio by viewModel.publishedPortfolio.collectAsState()
    val blogs by viewModel.publishedBlogs.collectAsState()
    val favorites by viewModel.userFavorites.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userRequests by viewModel.userRequests.collectAsState()
    val pendingTicketsCount by viewModel.userPendingTicketsCount.collectAsState()
    val trainingCourses by viewModel.trainingCourses.collectAsState()
    val activeProjectsCount = userRequests.count { it.status == RequestStatus.APPROVED || it.status == RequestStatus.IN_PROGRESS }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- HERO SECTION ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NexGenNavy, NexGenNavyLight)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MR NexGen IT Services",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (currentUser != null) {
                            Text(
                                text = "Hi, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "there"}",
                                color = NexGenCyan,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Powering Your Digital Future",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Professional IT services, corporate software development, certified training, and scalable cloud solutions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Banner Image
                    Image(
                        painter = painterResource(id = R.drawable.hero_mrnexgen_tech),
                        contentDescription = "Tech Workspace Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Services) },
                            colors = ButtonDefaults.buttonColors(containerColor = NexGenCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Explore Services", color = NexGenNavyDark, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.ContactUs) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color.White, Color.White))),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Contact Us", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- CLIENT DASHBOARD SUMMARY COMPONENT ---
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                MainDashboardSummary(
                    userName = currentUser?.name ?: "Guest",
                    company = currentUser?.company ?: "MR NexGen Client Portal",
                    city = currentUser?.city ?: "",
                    activeProjectsCount = activeProjectsCount,
                    pendingTicketsCount = pendingTicketsCount,
                    trainingCoursesCount = trainingCourses.size,
                    onActiveProjectsClick = {
                        if (currentUser == null) viewModel.navigateTo(Screen.Login)
                        else viewModel.navigateTo(Screen.MyRequests)
                    },
                    onPendingTicketsClick = {
                        if (currentUser == null) viewModel.navigateTo(Screen.Login)
                        else viewModel.navigateTo(Screen.SupportTickets)
                    },
                    onTrainingCoursesClick = {
                        viewModel.navigateTo(Screen.Training)
                    }
                )
            }
        }

        // --- SERVICES PREVIEW ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Featured IT Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tailored digital engineering for modern business",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexGenTextSecondary
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(Screen.Services) }) {
                        Text("View All", color = NexGenNavy, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                services.take(4).forEach { service ->
                    val isFav = favorites.any { it.itemId == service.id }
                    ServiceCard(
                        service = service,
                        isFavorite = isFav,
                        onServiceClick = { viewModel.selectService(service) },
                        onRequestClick = {
                            viewModel.selectService(service, navigate = false)
                            viewModel.navigateTo(Screen.ServiceRequestForm)
                        },
                        onFavoriteToggle = {
                            viewModel.toggleFavorite(
                                itemType = "SERVICE",
                                itemId = service.id,
                                title = service.title,
                                subtitle = service.shortDesc
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- WHY CHOOSE US ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexGenSurfaceVariant)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Why Choose MR NexGen?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "We combine technical mastery with client-first values.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexGenTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val features = listOf(
                    Triple(Icons.Default.Groups, "Experienced Team", "Skilled developers, architects, and designers."),
                    Triple(Icons.Default.Terminal, "Modern Technology", "Kotlin Compose, React, Cloud & Microservices."),
                    Triple(Icons.Default.HeadsetMic, "Professional Support", "Dedicated support ticketing & consultation."),
                    Triple(Icons.Default.Verified, "Quality Solutions", "Security-compliant, well-tested deliverables."),
                    Triple(Icons.Default.MonetizationOn, "Affordable Services", "Transparent quotes tailored to your budget."),
                    Triple(Icons.Default.Handshake, "Client Focused", "Milestone-driven transparent progress tracking.")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    features.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (icon, title, desc) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(NexGenNavy.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = title, tint = NexGenNavy, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- TRAINING & INTERNSHIP SECTION ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexGenNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = NexGenCyan,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "TRAINING & INTERNSHIP WING",
                            color = NexGenNavyDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Industrial IT & Software Training",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Accelerate your career with Summer Training, Winter Training, 6-Month Industrial Internships, Apprenticeships, and Vocational Programs working on LIVE commercial software projects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Summer Training", "Winter Training", "Industrial Internship", "Apprenticeship").forEach { tag ->
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.ContactUs) },
                        colors = ButtonDefaults.buttonColors(containerColor = NexGenCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enquire About Training", color = NexGenNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- PORTFOLIO PREVIEW ---
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Our Portfolio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Proven projects delivered with excellence",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexGenTextSecondary
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(Screen.Portfolio) }) {
                        Text("View All", color = NexGenNavy, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                portfolio.take(3).forEach { item ->
                    PortfolioCard(
                        item = item,
                        onItemClick = { viewModel.selectPortfolio(item) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // --- LATEST BLOGS PREVIEW ---
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Latest Insights & Blogs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tech tutorials, industry trends & guides",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexGenTextSecondary
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(Screen.Blogs) }) {
                        Text("View All", color = NexGenNavy, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                blogs.take(2).forEach { blog ->
                    BlogCard(
                        blog = blog,
                        onBlogClick = { viewModel.selectBlog(blog) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // --- CTA SECTION ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Have a Project in Mind?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NexGenNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let's discuss how MR NexGen IT Services can transform your ideas into reality.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexGenTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.ContactUs) },
                        colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Contact MR NexGen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- FOOTER ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF061426))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mrnexgen_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MR NexGen IT Services",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Technology. Innovation. Growth.",
                    color = NexGenCyan,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "About",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.AboutUs) }
                    )
                    Text(
                        text = "Services",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Services) }
                    )
                    Text(
                        text = "Portfolio",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Portfolio) }
                    )
                    Text(
                        text = "Blogs",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Blogs) }
                    )
                    Text(
                        text = "Contact",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.ContactUs) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Settings) }
                    )
                    Text(
                        text = "Terms & Conditions",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Settings) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "© 2026 MR NexGen IT Services. All Rights Reserved.",
                    color = Color(0xFF475569),
                    fontSize = 11.sp
                )
                Text(
                    text = "https://www.mrnexgen.com",
                    color = NexGenCyanDark,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mrnexgen.com"))
                        context.startActivity(browserIntent)
                    }
                )
            }
        }
    }
}
