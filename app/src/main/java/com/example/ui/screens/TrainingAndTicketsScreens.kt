package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.SupportTicket
import com.example.data.model.TrainingCourse
import com.example.ui.components.EmptyState
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

// ==========================================
// 1. TRAINING COURSES SCREEN
// ==========================================
@Composable
fun TrainingCoursesScreen(viewModel: MainViewModel) {
    val courses by viewModel.trainingCourses.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var enrolledSuccessCourse by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Industrial", "Fast-track", "Specialized")
    val filteredCourses = if (selectedCategory == "All") {
        courses
    } else {
        courses.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    if (enrolledSuccessCourse != null) {
        AlertDialog(
            onDismissRequest = { enrolledSuccessCourse = null },
            icon = { Icon(Icons.Default.School, contentDescription = null, tint = NexGenCyanDark) },
            title = { Text("Application Received!") },
            text = {
                Text(
                    "Thank you for registering for \"$enrolledSuccessCourse\". Our technical training coordinator will contact you via email and phone within 24 hours with syllabus details and batch timings."
                )
            },
            confirmButton = {
                Button(
                    onClick = { enrolledSuccessCourse = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
                ) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Training Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexGenNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = NexGenCyan,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MR NEXGEN ACADEMY",
                            color = NexGenNavyDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Industrial IT Training & Internships",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Work on live commercial software projects, modern Kotlin, React, and cloud architectures under certified corporate mentors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NexGenNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // List of courses
        items(filteredCourses, key = { it.id }) { course ->
            CourseItemCard(
                course = course,
                onEnrollClick = { enrolledSuccessCourse = course.title }
            )
        }
    }
}

@Composable
private fun CourseItemCard(
    course: TrainingCourse,
    onEnrollClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("course_item_${course.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = course.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexGenCyanDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = NexGenSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = course.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexGenNavy,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodySmall,
                color = NexGenTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Technologies Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                course.technologies.take(4).forEach { tech ->
                    Surface(
                        color = NexGenNavy.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tech,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NexGenNavy,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.certification,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexGenTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onEnrollClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Text("Apply Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 2. SUPPORT TICKETS SCREEN
// ==========================================
@Composable
fun SupportTicketsScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val tickets by viewModel.userTickets.collectAsState()

    if (currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sign In to View Tickets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Track technical issues and communicate directly with engineers.", color = NexGenTextSecondary)
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.CreateTicket) },
                containerColor = NexGenCyanDark,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_create_ticket")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Ticket")
            }
        }
    ) { padding ->
        if (tickets.isEmpty()) {
            EmptyState(
                title = "No Tickets Yet",
                message = "Have a question, server issue, or need technical help? Open a new support ticket.",
                icon = Icons.Default.ConfirmationNumber,
                actionButtonText = "Open Support Ticket",
                onActionClick = { viewModel.navigateTo(Screen.CreateTicket) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tickets, key = { it.id }) { ticket ->
                    SupportTicketItemCard(ticket = ticket)
                }
            }
        }
    }
}

@Composable
private fun SupportTicketItemCard(ticket: SupportTicket) {
    val statusColor = when (ticket.status) {
        "OPEN" -> StatusPending
        "IN_PROGRESS" -> StatusInProgress
        "RESOLVED" -> StatusCompleted
        else -> NexGenTextSecondary
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("ticket_card_${ticket.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.ticketId,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexGenNavy,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = ticket.status.replace("_", " "),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ticket.subject,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodySmall,
                color = NexGenTextSecondary,
                maxLines = 3
            )

            if (ticket.adminResponse.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NexGenSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "MR NexGen Engineer Response:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NexGenNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ticket.adminResponse,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. CREATE TICKET SCREEN
// ==========================================
@Composable
fun CreateTicketScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical Support") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf("Technical Support", "Deployment / Cloud", "Bug Report", "Consultation", "Billing")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Create Support Ticket",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Describe your issue and an engineer will respond promptly.",
            style = MaterialTheme.typography.bodySmall,
            color = NexGenTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            placeholder = { Text("e.g. Issue connecting to REST API") },
            modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(3).forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Detailed Description") },
            placeholder = { Text("Include error messages, steps to reproduce, or affected service...") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth().testTag("ticket_desc_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (subject.isBlank() || description.isBlank()) {
                    viewModel.showToast("Please fill in both subject and description.")
                    return@Button
                }
                isSubmitting = true
                viewModel.createSupportTicket(
                    subject = subject,
                    category = category,
                    description = description
                ) {
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting && subject.isNotBlank() && description.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_ticket_btn")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Submit Ticket", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 4. CLIENT FEEDBACK SCREEN
// ==========================================
@Composable
fun ClientFeedbackScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var rating by remember { mutableStateOf(5) }
    var serviceTitle by remember { mutableStateOf("Web & Mobile App Development") }
    var message by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Share Your Feedback", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your insights help us continuously elevate our software engineering standard.",
            style = MaterialTheme.typography.bodySmall,
            color = NexGenTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Star rating
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { star ->
                IconButton(onClick = { rating = star }) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star $star",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = serviceTitle,
            onValueChange = { serviceTitle = it },
            label = { Text("Service Received") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Your Review & Comments") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.submitFeedback(
                    serviceTitle = serviceTitle,
                    rating = rating,
                    message = message
                ) {
                    submitted = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Submit Review", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 5. ADMIN TICKETS SCREEN
// ==========================================
@Composable
fun AdminTicketsScreen(viewModel: MainViewModel) {
    val allTickets by viewModel.allAdminTickets.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var replyingTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var responseText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("RESOLVED") }

    if (replyingTicket != null) {
        AlertDialog(
            onDismissRequest = { replyingTicket = null },
            title = { Text("Reply to Ticket ${replyingTicket?.ticketId}") },
            text = {
                Column {
                    Text(replyingTicket?.subject ?: "", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Response") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IN_PROGRESS", "RESOLVED", "CLOSED").forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ticket = replyingTicket ?: return@Button
                        viewModel.updateTicketStatus(
                            ticketId = ticket.id,
                            status = selectedStatus,
                            response = responseText
                        )
                        replyingTicket = null
                        responseText = ""
                    }
                ) {
                    Text("Save Response")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingTicket = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(allTickets, key = { it.id }) { ticket ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable {
                    replyingTicket = ticket
                    responseText = ticket.adminResponse
                    selectedStatus = ticket.status
                }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ticket.ticketId, fontWeight = FontWeight.Bold, color = NexGenNavy)
                        Text(ticket.status, fontWeight = FontWeight.Bold, color = NexGenCyanDark, fontSize = 11.sp)
                    }
                    Text(ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("User: ${ticket.userName} (${ticket.userEmail})", style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ticket.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }
    }
}

// ==========================================
// 6. ADMIN FEEDBACK SCREEN
// ==========================================
@Composable
fun AdminFeedbackScreen(viewModel: MainViewModel) {
    val feedbackList by viewModel.allFeedback.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(feedbackList, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.userName, fontWeight = FontWeight.Bold)
                        Text("★ ${item.rating}/5", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                    }
                    Text(item.serviceTitle, style = MaterialTheme.typography.labelSmall, color = NexGenCyanDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
