package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServiceRequestDetailScreen(viewModel: MainViewModel) {
    val request = viewModel.selectedRequest.collectAsState().value
    val currentUser = viewModel.currentUser.collectAsState().value

    if (request == null) {
        EmptyState(
            icon = Icons.Default.Info,
            title = "No Request Selected",
            message = "Please choose a service request to inspect.",
            actionButtonText = "View My Requests",
            onActionClick = { viewModel.navigateTo(Screen.MyRequests) }
        )
        return
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        // Top Request Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = request.requestId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NexGenNavy
                )
                Text(
                    text = "Submitted ${dateFormat.format(Date(request.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexGenTextSecondary
                )
            }
            StatusBadge(status = request.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Project Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = request.projectTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Service: ${request.serviceTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexGenCyanDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = NexGenBorder)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Budget", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                        Text(request.budget, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Timeline", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                        Text(request.deadline, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Client", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                        Text(request.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Project Description", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexGenTextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Timeline
        Text(
            text = "Progress Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                val stages = listOf(
                    RequestStatus.PENDING to "Request Created",
                    RequestStatus.UNDER_REVIEW to "Under Technical Review",
                    RequestStatus.APPROVED to "Approved & Milestone Set",
                    RequestStatus.IN_PROGRESS to "Development In Progress",
                    RequestStatus.COMPLETED to "Project Completed & Deployed"
                )

                val currentIndex = when (request.status) {
                    RequestStatus.PENDING -> 0
                    RequestStatus.UNDER_REVIEW -> 1
                    RequestStatus.APPROVED -> 2
                    RequestStatus.IN_PROGRESS -> 3
                    RequestStatus.COMPLETED -> 4
                    RequestStatus.REJECTED -> -1
                }

                if (request.status == RequestStatus.REJECTED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = StatusRejected)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Request Rejected", fontWeight = FontWeight.Bold, color = StatusRejected)
                            if (request.rejectionReason.isNotBlank()) {
                                Text("Reason: ${request.rejectionReason}", style = MaterialTheme.typography.bodySmall, color = StatusRejected)
                            }
                        }
                    }
                } else {
                    stages.forEachIndexed { idx, (stageStatus, stageLabel) ->
                        val isDone = idx <= currentIndex
                        val isCurrent = idx == currentIndex

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isDone) NexGenNavy else NexGenBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                if (idx < stages.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(32.dp)
                                            .background(if (idx < currentIndex) NexGenNavy else NexGenBorder)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stageLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurface else NexGenTextMuted
                                )
                                if (isCurrent) {
                                    Text(
                                        text = "Current Stage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexGenCyanDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (request.adminNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Technical Team Notes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(request.adminNotes, style = MaterialTheme.typography.bodySmall, color = NexGenTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.navigateTo(Screen.SupportChat) },
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_contact_support_for_req")
        ) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Message Tech Support Regarding This Project")
        }
    }
}
