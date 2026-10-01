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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.RequestStatus
import com.example.data.model.UserRole
import com.example.ui.components.EmptyState
import com.example.ui.components.MainDashboardSummary
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserDashboardScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val requests by viewModel.userRequests.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val pendingTicketsCount by viewModel.userPendingTicketsCount.collectAsState()
    val trainingCourses by viewModel.trainingCourses.collectAsState()

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sign In to Access Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Track service requests, view project milestones and contact support.", color = NexGenTextSecondary)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Sign In")
            }
        }
        return
    }

    val pendingCount = requests.count { it.status == RequestStatus.PENDING || it.status == RequestStatus.UNDER_REVIEW }
    val activeCount = requests.count { it.status == RequestStatus.APPROVED || it.status == RequestStatus.IN_PROGRESS }
    val completedCount = requests.count { it.status == RequestStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        // --- MAIN DASHBOARD SUMMARY COMPONENT ---
        item {
            MainDashboardSummary(
                userName = currentUser?.name ?: "Client",
                company = currentUser?.company ?: "",
                city = currentUser?.city ?: "",
                activeProjectsCount = activeCount,
                pendingTicketsCount = pendingTicketsCount,
                trainingCoursesCount = trainingCourses.size,
                onActiveProjectsClick = { viewModel.navigateTo(Screen.MyRequests) },
                onPendingTicketsClick = { viewModel.navigateTo(Screen.SupportTickets) },
                onTrainingCoursesClick = { viewModel.navigateTo(Screen.Training) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.ServiceRequestForm) },
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenCyanDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Request", fontSize = 12.sp)
                }
                Button(
                    onClick = { viewModel.navigateTo(Screen.SupportTickets) },
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Support Ticket", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.SupportChat) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Chat", fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Project Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.navigateTo(Screen.MyRequests) }) {
                    Text("See All", color = NexGenNavy, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (requests.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("You haven't requested any services yet.", style = MaterialTheme.typography.bodyMedium, color = NexGenTextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Services) },
                            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
                        ) {
                            Text("Explore Services")
                        }
                    }
                }
            }
        } else {
            items(requests.take(3), key = { it.id }) { req ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectRequest(req) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.requestId, style = MaterialTheme.typography.labelSmall, color = NexGenNavy, fontWeight = FontWeight.Bold)
                            Text(req.projectTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Service: ${req.serviceTitle}", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                        }
                        StatusBadge(status = req.status)
                    }
                }
            }
        }
    }
}

@Composable
fun MyRequestsScreen(viewModel: MainViewModel) {
    val requests by viewModel.userRequests.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }

    val tabs = listOf("All", "Pending", "Under Review", "Approved", "In Progress", "Completed", "Rejected")

    val filteredList = remember(requests, selectedTab) {
        if (selectedTab == "All") requests
        else requests.filter {
            val statusStr = when (it.status) {
                RequestStatus.PENDING -> "Pending"
                RequestStatus.UNDER_REVIEW -> "Under Review"
                RequestStatus.APPROVED -> "Approved"
                RequestStatus.IN_PROGRESS -> "In Progress"
                RequestStatus.COMPLETED -> "Completed"
                RequestStatus.REJECTED -> "Rejected"
            }
            statusStr.equals(selectedTab, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            EmptyState(
                icon = Icons.Default.AssignmentLate,
                title = "No Requests Found",
                message = "You don't have any requests in the '$selectedTab' category.",
                actionButtonText = "Request a Service",
                onActionClick = { viewModel.navigateTo(Screen.Services) }
            )
        } else {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { req ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectRequest(req) }
                            .testTag("request_item_${req.requestId}")
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
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(req.projectTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Service: ${req.serviceTitle}", color = NexGenCyanDark, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Budget: ${req.budget}", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                                Text(dateFormat.format(Date(req.createdAt)), style = MaterialTheme.typography.bodySmall, color = NexGenTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen(viewModel: MainViewModel) {
    val user = viewModel.currentUser.collectAsState().value
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Profile & Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign in to view your account details and manage requests.", color = NexGenTextSecondary)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Sign In")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        // Avatar Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(NexGenNavy, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = NexGenTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (user.role == UserRole.ADMIN) Color(0xFFE0E7FF) else Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (user.role == UserRole.ADMIN) "ADMINISTRATOR" else "VERIFIED CLIENT",
                        color = if (user.role == UserRole.ADMIN) NexGenNavy else Color(0xFF047857),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Menu Items
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Default.Dashboard, title = "My Dashboard") {
                    viewModel.navigateTo(Screen.UserDashboard)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Assignment, title = "My Service Requests") {
                    viewModel.navigateTo(Screen.MyRequests)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Favorite, title = "Saved Favourites") {
                    viewModel.navigateTo(Screen.Favorites)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notifications") {
                    viewModel.navigateTo(Screen.Notifications)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Chat, title = "Support Helpdesk") {
                    viewModel.navigateTo(Screen.SupportChat)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Edit, title = "Edit Profile") {
                    viewModel.navigateTo(Screen.EditProfile)
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Lock, title = "Change Password") {
                    showPasswordDialog = true
                }
                Divider(color = NexGenBorder)
                ProfileMenuItem(icon = Icons.Default.Settings, title = "Application Settings") {
                    viewModel.navigateTo(Screen.Settings)
                }
            }
        }

        if (user.role == UserRole.ADMIN) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.navigateTo(Screen.AdminDashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenCyanDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Admin Control Panel", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { showDeleteConfirmDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Account", color = Color(0xFFEF4444), fontSize = 12.sp)
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { oldP, newP ->
                viewModel.changePassword(oldP, newP) { ok, _ ->
                    if (ok) showPasswordDialog = false
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account and associated project history?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAccount {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditProfileScreen(viewModel: MainViewModel) {
    val user = viewModel.currentUser.collectAsState().value ?: return
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var company by remember { mutableStateOf(user.company) }
    var city by remember { mutableStateOf(user.city) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        Text("Edit Profile Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Mobile Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateProfile(name, phone, company, city) {}
            },
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: MainViewModel) {
    val favorites by viewModel.userFavorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FavoriteBorder,
                title = "No Saved Favourites",
                message = "Bookmark services, blogs, and portfolio projects to access them quickly here.",
                actionButtonText = "Browse Services",
                onActionClick = { viewModel.navigateTo(Screen.Services) }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = { it.id }) { fav ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = NexGenCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = fav.itemType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexGenNavy,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(fav.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(fav.subtitle, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(fav.itemType, fav.itemId, fav.title, fav.subtitle)
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = NexGenTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val notifications by viewModel.userNotifications.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (notifications.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                    Text("Mark all as read", color = NexGenNavy, fontSize = 12.sp)
                }
            }
        }

        if (notifications.isEmpty()) {
            EmptyState(
                icon = Icons.Default.NotificationsNone,
                title = "No Notifications",
                message = "You're all caught up! Updates regarding service requests and blogs will appear here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else Color(0xFFF0FDF4)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.markNotificationRead(notif.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NexGenNavy.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (notif.type == "STATUS") Icons.Default.Sync else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NexGenNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notif.message, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dateFormat.format(Date(notif.createdAt)), style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.supportMessages.collectAsState()
    val user = viewModel.currentUser.collectAsState().value
    var inputMessage by remember { mutableStateOf("") }
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("MR NexGen Technical Support", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Direct communication with engineering leads", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start a conversation regarding your project requirements, quote, or industrial training questions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexGenTextMuted,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderRole == UserRole.USER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) NexGenNavy else NexGenSurfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NexGenCyanDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = timeFormat.format(Date(msg.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMe) Color(0xFFCBD5E1) else NexGenTextMuted,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Write your message...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendSupportMessage(inputMessage)
                        inputMessage = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(NexGenNavy, CircleShape)
                    .testTag("btn_send_chat")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
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
            Icon(icon, contentDescription = title, tint = NexGenNavy, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NexGenTextMuted)
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Current Password") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password (Min 6 chars)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPass.isBlank() || newPass.isBlank()) {
                        error = "All fields are required."
                        return@Button
                    }
                    if (newPass != confirmPass) {
                        error = "Passwords do not match."
                        return@Button
                    }
                    onConfirm(oldPass, newPass)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
