package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BlogCard
import com.example.ui.components.EmptyState
import com.example.ui.components.PortfolioCard
import com.example.ui.components.ServiceCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current
    var showLegalDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        Text("Application Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = NexGenNavy)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleTheme() }
                    )
                }

                Divider(color = NexGenBorder)

                SettingsRow(Icons.Default.Language, "Official Company Website", "https://www.mrnexgen.com") {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mrnexgen.com"))
                    context.startActivity(browserIntent)
                }

                Divider(color = NexGenBorder)

                SettingsRow(Icons.Default.Security, "Privacy Policy", "How we safeguard data") {
                    showLegalDialog = "Privacy Policy"
                }

                Divider(color = NexGenBorder)

                SettingsRow(Icons.Default.Gavel, "Terms of Service", "Standard client service agreements") {
                    showLegalDialog = "Terms & Conditions"
                }

                Divider(color = NexGenBorder)

                SettingsRow(Icons.Default.SupportAgent, "Technical Support Helpdesk", "Open client ticketing") {
                    viewModel.navigateTo(Screen.SupportChat)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ABOUT APP SECTION ---
        Text("About App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("about_app_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = NexGenNavy.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = "Device icon",
                                tint = NexGenNavy,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MR NexGen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version ${com.example.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexGenCyanDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Build ${com.example.BuildConfig.VERSION_CODE} • ${com.example.BuildConfig.APPLICATION_ID}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexGenTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = NexGenBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Release Channel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Google Play Distribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexGenTextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.checkForAppUpdates(isManual = true) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("check_updates_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check for Updates", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Development & Review testing simulation affordances
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            viewModel.simulateRemoteUpdateCheck(
                                latestVersion = "1.0.1",
                                latestVersionCode = 2,
                                minimumSupportedVersionCode = 1,
                                forceUpdate = false
                            )
                        },
                        modifier = Modifier.testTag("simulate_update_btn")
                    ) {
                        Text("Simulate Update", fontSize = 11.sp, color = NexGenNavy)
                    }

                    TextButton(
                        onClick = {
                            viewModel.simulateRemoteUpdateCheck(
                                latestVersion = "2.0.0",
                                latestVersionCode = 5,
                                minimumSupportedVersionCode = 2,
                                forceUpdate = true
                            )
                        },
                        modifier = Modifier.testTag("simulate_force_update_btn")
                    ) {
                        Text("Simulate Force Update", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    if (showLegalDialog != null) {
        val title = showLegalDialog!!
        val text = if (title == "Privacy Policy") {
            "MR NexGen IT Services values user privacy. We do not sell or lease user information to third parties. All client service requests, contact messages, and authentication credentials are encrypted using industry-standard hashing and security protocols."
        } else {
            "All software engineering services and industrial training programs provided by MR NexGen IT Services are subject to formal project milestone schedules. Unauthorized redistribution of training materials or source assets is prohibited."
        }

        AlertDialog(
            onDismissRequest = { showLegalDialog = null },
            title = { Text(title) },
            text = { Text(text, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(onClick = { showLegalDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
fun GlobalSearchScreen(viewModel: MainViewModel) {
    var query by remember { mutableStateOf("") }
    val services by viewModel.publishedServices.collectAsState()
    val portfolio by viewModel.publishedPortfolio.collectAsState()
    val blogs by viewModel.publishedBlogs.collectAsState()
    val favorites by viewModel.userFavorites.collectAsState()

    val matchingServices = remember(services, query) {
        if (query.isBlank()) emptyList()
        else services.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.shortDesc.contains(query, ignoreCase = true)
        }
    }

    val matchingPortfolio = remember(portfolio, query) {
        if (query.isBlank()) emptyList()
        else portfolio.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.technologies.any { t -> t.contains(query, ignoreCase = true) }
        }
    }

    val matchingBlogs = remember(blogs, query) {
        if (query.isBlank()) emptyList()
        else blogs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.tags.any { t -> t.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search services, projects, blogs...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("global_search_input")
        )

        if (query.isBlank()) {
            EmptyState(
                icon = Icons.Default.ManageSearch,
                title = "Search MR NexGen",
                message = "Search across all IT services, portfolio projects, and technical blogs."
            )
        } else if (matchingServices.isEmpty() && matchingPortfolio.isEmpty() && matchingBlogs.isEmpty()) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No Matches Found",
                message = "No services, projects, or blogs matched '$query'. Try another search term."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (matchingServices.isNotEmpty()) {
                    item {
                        Text("Matching Services (${matchingServices.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = NexGenNavy)
                    }
                    items(matchingServices, key = { "srv_${it.id}" }) { srv ->
                        val isFav = favorites.any { it.itemId == srv.id }
                        ServiceCard(
                            service = srv,
                            isFavorite = isFav,
                            onServiceClick = { viewModel.selectService(srv) },
                            onRequestClick = {
                                viewModel.selectService(srv, navigate = false)
                                viewModel.navigateTo(Screen.ServiceRequestForm)
                            },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(
                                    itemType = "SERVICE",
                                    itemId = srv.id,
                                    title = srv.title,
                                    subtitle = srv.shortDesc
                                )
                            }
                        )
                    }
                }

                if (matchingPortfolio.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Matching Portfolio Projects (${matchingPortfolio.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = NexGenCyanDark)
                    }
                    items(matchingPortfolio, key = { "port_${it.id}" }) { port ->
                        PortfolioCard(
                            item = port,
                            onItemClick = { viewModel.selectPortfolio(port) }
                        )
                    }
                }

                if (matchingBlogs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Matching Blogs (${matchingBlogs.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF047857))
                    }
                    items(matchingBlogs, key = { "blog_${it.id}" }) { blog ->
                        BlogCard(
                            blog = blog,
                            onBlogClick = { viewModel.selectBlog(blog) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = NexGenNavy, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = NexGenTextSecondary)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NexGenTextMuted)
    }
}
