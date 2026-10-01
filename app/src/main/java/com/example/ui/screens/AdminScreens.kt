package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.EmptyState
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val users by viewModel.allAdminUsers.collectAsState()
    val requests by viewModel.allAdminRequests.collectAsState()
    val services by viewModel.allServices.collectAsState()
    val blogs by viewModel.allBlogs.collectAsState()
    val messages by viewModel.contactMessages.collectAsState()

    val pendingCount = requests.count { it.status == RequestStatus.PENDING || it.status == RequestStatus.UNDER_REVIEW }
    val activeCount = requests.count { it.status == RequestStatus.APPROVED || it.status == RequestStatus.IN_PROGRESS }
    val completedCount = requests.count { it.status == RequestStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Admin Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexGenNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ADMIN CONSOLE", color = NexGenCyan, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("MR NexGen Command Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.AdminAuditLogs) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Audit Logs", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Users",
                    value = "${users.size}",
                    subtitle = "Registered accounts",
                    icon = Icons.Default.People,
                    accentColor = NexGenCyanDark,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Requests",
                    value = "${requests.size}",
                    subtitle = "$pendingCount pending review",
                    icon = Icons.Default.Assignment,
                    accentColor = StatusPending,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Active Work",
                    value = "$activeCount",
                    subtitle = "Under development",
                    icon = Icons.Default.Engineering,
                    accentColor = StatusInProgress,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Delivered",
                    value = "$completedCount",
                    subtitle = "Successfully finished",
                    icon = Icons.Default.Verified,
                    accentColor = StatusCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Management Shortcuts
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Management Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    AdminShortcutRow(Icons.Default.Assignment, "Service Requests Manager", "${requests.size} total, $pendingCount pending") {
                        viewModel.navigateTo(Screen.AdminRequests)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.SettingsApplications, "Services Catalogue Manager", "${services.size} services active") {
                        viewModel.navigateTo(Screen.AdminServices)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.People, "Client User Accounts", "${users.size} clients registered") {
                        viewModel.navigateTo(Screen.AdminUsers)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.Article, "Blog Articles Publisher", "${blogs.size} published posts") {
                        viewModel.navigateTo(Screen.AdminBlogs)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.Work, "Portfolio Projects Showcase", "Showcased projects") {
                        viewModel.navigateTo(Screen.AdminPortfolio)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.Mail, "Client Inquiries & Contact Messages", "${messages.size} total messages") {
                        viewModel.navigateTo(Screen.AdminMessages)
                    }
                    Divider(color = NexGenBorder)
                    AdminShortcutRow(Icons.Default.Shield, "Security Audit Trail", "Real-time action logs") {
                        viewModel.navigateTo(Screen.AdminAuditLogs)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestsScreen(viewModel: MainViewModel) {
    val requests by viewModel.allAdminRequests.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var requestToEdit by remember { mutableStateOf<ServiceRequest?>(null) }

    val filters = listOf("All", "Pending", "Under Review", "Approved", "In Progress", "Completed", "Rejected")

    val filtered = remember(requests, selectedFilter) {
        if (selectedFilter == "All") requests
        else requests.filter {
            val s = when (it.status) {
                RequestStatus.PENDING -> "Pending"
                RequestStatus.UNDER_REVIEW -> "Under Review"
                RequestStatus.APPROVED -> "Approved"
                RequestStatus.IN_PROGRESS -> "In Progress"
                RequestStatus.COMPLETED -> "Completed"
                RequestStatus.REJECTED -> "Rejected"
            }
            s.equals(selectedFilter, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            filters.forEach { f ->
                Tab(
                    selected = f == selectedFilter,
                    onClick = { selectedFilter = f },
                    text = { Text(f) }
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyState(
                icon = Icons.Default.AssignmentLate,
                title = "No Requests Found",
                message = "No service requests match the filter '$selectedFilter'."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { req ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(req.requestId, fontWeight = FontWeight.Bold, color = NexGenNavy, style = MaterialTheme.typography.titleSmall)
                                StatusBadge(status = req.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(req.projectTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Client: ${req.userName} (${req.userEmail})", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                            Text("Service: ${req.serviceTitle} • Budget: ${req.budget}", style = MaterialTheme.typography.bodySmall, color = NexGenCyanDark)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                color = NexGenTextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { requestToEdit = req },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Update Status & Notes", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (requestToEdit != null) {
        AdminStatusDialog(
            request = requestToEdit!!,
            onDismiss = { requestToEdit = null },
            onSave = { newStatus, reason, notes ->
                viewModel.updateRequestStatus(requestToEdit!!.id, newStatus, reason, notes)
                requestToEdit = null
            }
        )
    }
}

@Composable
fun AdminServicesScreen(viewModel: MainViewModel) {
    val services by viewModel.allServices.collectAsState()
    var serviceToEdit by remember { mutableStateOf<ServiceItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NexGenNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(services, key = { it.id }) { service ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = NexGenCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = service.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexGenNavy,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(service.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(service.shortDesc, maxLines = 1, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                            Text("Starts: ${service.startingPrice} • Delivery: ${service.deliveryTime}", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                        }

                        Row {
                            IconButton(onClick = { serviceToEdit = service }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NexGenNavy)
                            }
                            IconButton(onClick = { viewModel.deleteService(service.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ServiceEditDialog(
            initial = null,
            onDismiss = { showCreateDialog = false },
            onSave = {
                viewModel.saveService(it)
                showCreateDialog = false
            }
        )
    }

    if (serviceToEdit != null) {
        ServiceEditDialog(
            initial = serviceToEdit,
            onDismiss = { serviceToEdit = null },
            onSave = {
                viewModel.saveService(it)
                serviceToEdit = null
            }
        )
    }
}

@Composable
fun AdminUsersScreen(viewModel: MainViewModel) {
    val users by viewModel.allAdminUsers.collectAsState()
    var search by remember { mutableStateOf("") }

    val filtered = remember(users, search) {
        if (search.isBlank()) users
        else users.filter { it.name.contains(search, ignoreCase = true) || it.email.contains(search, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search users by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { u ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (u.role == UserRole.ADMIN) {
                                    Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(4.dp)) {
                                        Text("ADMIN", color = NexGenNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            }
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                            Text("Phone: ${u.phone} • Company: ${u.company.ifBlank { "Individual" }}", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                        }

                        if (u.role != UserRole.ADMIN) {
                            Switch(
                                checked = u.isActive,
                                onCheckedChange = { active ->
                                    viewModel.setUserActive(u.id, active)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBlogsScreen(viewModel: MainViewModel) {
    val blogs by viewModel.allBlogs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = NexGenNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Blog")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(blogs, key = { it.id }) { blog ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(blog.category, style = MaterialTheme.typography.labelSmall, color = NexGenCyanDark, fontWeight = FontWeight.Bold)
                            Text(blog.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Author: ${blog.authorName} • ${blog.readTimeMinutes} min", style = MaterialTheme.typography.bodySmall, color = NexGenTextMuted)
                        }
                        IconButton(onClick = { viewModel.deleteBlog(blog.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Tech Insights") }
        var excerpt by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Publish New Blog Article") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = excerpt, onValueChange = { excerpt = it }, label = { Text("Short Excerpt") }, minLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Article Content") }, minLines = 4)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.saveBlog(
                                BlogPost(
                                    id = "",
                                    title = title,
                                    excerpt = excerpt.ifBlank { title },
                                    content = content,
                                    category = category,
                                    authorName = "MR NexGen Tech Team",
                                    readTimeMinutes = 4,
                                    tags = listOf("Tech", "Development")
                                )
                            )
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminPortfolioScreen(viewModel: MainViewModel) {
    val portfolio by viewModel.allPortfolio.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = NexGenNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Portfolio")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(portfolio, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.category, style = MaterialTheme.typography.labelSmall, color = NexGenCyanDark, fontWeight = FontWeight.Bold)
                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Client: ${item.clientName}", style = MaterialTheme.typography.bodySmall, color = NexGenTextMuted)
                        }
                        IconButton(onClick = { viewModel.deletePortfolio(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Websites") }
        var clientName by remember { mutableStateOf("") }
        var shortDesc by remember { mutableStateOf("") }
        var fullDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Portfolio Showcase Project") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Websites/Android/Software)") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client Name") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = shortDesc, onValueChange = { shortDesc = it }, label = { Text("Short Description") }, minLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = fullDesc, onValueChange = { fullDesc = it }, label = { Text("Full Overview") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.savePortfolio(
                                PortfolioItem(
                                    id = "",
                                    title = title,
                                    category = category,
                                    shortDesc = shortDesc,
                                    fullDesc = fullDesc.ifBlank { shortDesc },
                                    clientName = clientName.ifBlank { "Client Project" },
                                    technologies = listOf("Full Stack", "Cloud")
                                )
                            )
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
                ) {
                    Text("Add Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminMessagesScreen(viewModel: MainViewModel) {
    val messages by viewModel.contactMessages.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (messages.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "No Inquiries Yet",
                message = "Client messages submitted via the Contact Us form will appear here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages, key = { it.id }) { msg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(msg.messageId, fontWeight = FontWeight.Bold, color = NexGenNavy, style = MaterialTheme.typography.labelSmall)
                                Text(dateFormat.format(Date(msg.createdAt)), style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("From: ${msg.name} (${msg.email})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Phone: ${msg.phone}", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Subject: ${msg.subject}", fontWeight = FontWeight.SemiBold, color = NexGenCyanDark, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsScreen(viewModel: MainViewModel) {
    val logs by viewModel.auditLogs.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (logs.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Shield,
                title = "No Audit Logs",
                message = "Administrative events, status modifications, and content creations will appear here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = NexGenNavy)
                                    Text(dateFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                                }
                                Text("Target: ${log.target} • Admin: ${log.adminName}", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                                if (log.details.isNotBlank()) {
                                    Text(log.details, style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminShortcutRow(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NexGenNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = NexGenNavy, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = NexGenTextSecondary)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NexGenTextMuted)
    }
}

@Composable
private fun AdminStatusDialog(
    request: ServiceRequest,
    onDismiss: () -> Unit,
    onSave: (RequestStatus, String, String) -> Unit
) {
    var status by remember { mutableStateOf(request.status) }
    var reason by remember { mutableStateOf(request.rejectionReason) }
    var notes by remember { mutableStateOf(request.adminNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Request ${request.requestId}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Select Stage:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                RequestStatus.values().forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = st }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = status == st, onClick = { status = st })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(st.name.replace("_", " "))
                    }
                }

                if (status == RequestStatus.REJECTED) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Rejection Reason *") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Admin Internal Notes") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(status, reason, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Update Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ServiceEditDialog(
    initial: ServiceItem?,
    onDismiss: () -> Unit,
    onSave: (ServiceItem) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Development") }
    var shortDesc by remember { mutableStateOf(initial?.shortDesc ?: "") }
    var fullDesc by remember { mutableStateOf(initial?.fullDesc ?: "") }
    var price by remember { mutableStateOf(initial?.startingPrice ?: "₹25,000") }
    var deliveryTime by remember { mutableStateOf(initial?.deliveryTime ?: "2-4 Weeks") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create New Service" else "Edit Service") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Service Title *") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Development/Mobile/Enterprise/Design/Marketing/Training)") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = shortDesc, onValueChange = { shortDesc = it }, label = { Text("Short Description") }, minLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = fullDesc, onValueChange = { fullDesc = it }, label = { Text("Full Description") }, minLines = 3)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Starting Price") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = deliveryTime, onValueChange = { deliveryTime = it }, label = { Text("Estimated Delivery") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ServiceItem(
                                id = initial?.id ?: "",
                                title = title,
                                category = category,
                                shortDesc = shortDesc,
                                fullDesc = fullDesc.ifBlank { shortDesc },
                                startingPrice = price,
                                deliveryTime = deliveryTime,
                                features = listOf("Dedicated Engineer", "Full Code Repository", "Quality Tested"),
                                technologies = listOf("Modern Architecture", "Cloud Integration")
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
