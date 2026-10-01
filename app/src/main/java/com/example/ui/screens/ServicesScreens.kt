package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceItem
import com.example.ui.components.EmptyState
import com.example.ui.components.ServiceCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ServicesScreen(viewModel: MainViewModel) {
    val services by viewModel.publishedServices.collectAsState()
    val favorites by viewModel.userFavorites.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Development", "Mobile", "Enterprise", "Design", "Marketing", "Training")

    val filteredServices = remember(services, selectedCategory) {
        if (selectedCategory == "All") services
        else services.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Categories horizontal filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NexGenNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${cat.lowercase()}")
                )
            }
        }

        if (filteredServices.isEmpty()) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No Services Found",
                message = "There are no services in this category currently.",
                actionButtonText = "View All Services",
                onActionClick = { selectedCategory = "All" }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredServices, key = { it.id }) { service ->
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
                }
            }
        }
    }
}

@Composable
fun ServiceDetailScreen(viewModel: MainViewModel) {
    val service = viewModel.selectedService.collectAsState().value
    val favorites by viewModel.userFavorites.collectAsState()

    if (service == null) {
        EmptyState(
            icon = Icons.Default.Info,
            title = "No Service Selected",
            message = "Please select a service from the catalogue.",
            actionButtonText = "Back to Services",
            onActionClick = { viewModel.navigateBack() }
        )
        return
    }

    val isFav = favorites.any { it.itemId == service.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = NexGenCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = service.category.uppercase(),
                    color = NexGenNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            IconButton(
                onClick = {
                    viewModel.toggleFavorite(
                        itemType = "SERVICE",
                        itemId = service.id,
                        title = service.title,
                        subtitle = service.shortDesc
                    )
                }
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) Color(0xFFEF4444) else NexGenTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = service.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = service.shortDesc,
            style = MaterialTheme.typography.bodyLarge,
            color = NexGenTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Price & Timeline Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Starting Price", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                    Text(
                        text = if (service.startingPrice.isNotBlank()) service.startingPrice else "Custom Quote",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexGenNavy
                    )
                }
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(NexGenBorder)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Est. Timeline", style = MaterialTheme.typography.labelSmall, color = NexGenTextMuted)
                    Text(
                        text = if (service.deliveryTime.isNotBlank()) service.deliveryTime else "2-4 Weeks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexGenNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = service.fullDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = NexGenTextSecondary,
            lineHeight = 22.sp
        )

        if (service.features.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Key Deliverables & Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            service.features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = feat, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        if (service.technologies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Technology Stack",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                service.technologies.forEach { tech ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tech,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.ContactUs) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Enquire Now")
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.ServiceRequestForm) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("btn_request_from_detail")
            ) {
                Text("Request Service", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ServiceRequestFormScreen(viewModel: MainViewModel) {
    val service = viewModel.selectedService.collectAsState().value
    val currentUser = viewModel.currentUser.collectAsState().value

    var projectTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("₹25,000 - ₹50,000") }
    var deadline by remember { mutableStateOf("1 Month") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = NexGenNavy, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Login Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please login to create and track your service requests.", color = NexGenTextSecondary)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy)
            ) {
                Text("Sign In Now")
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
        Text(
            text = "Request IT Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Fill in your project requirements for an official quotation and roadmap.",
            style = MaterialTheme.typography.bodySmall,
            color = NexGenTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Service Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(NexGenNavy, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = service?.title ?: "Custom IT Development",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Category: ${service?.category ?: "Enterprise"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexGenTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Client Info Summary
        Text(
            text = "Client Details (Verified)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NexGenNavy
        )
        Text(
            text = "${currentUser.name} • ${currentUser.email} • ${currentUser.phone}",
            style = MaterialTheme.typography.bodySmall,
            color = NexGenTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = projectTitle,
            onValueChange = { projectTitle = it },
            label = { Text("Project Title *") },
            placeholder = { Text("e.g. Modern E-Commerce Web Portal") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_project_title")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Project Description & Requirements *") },
            placeholder = { Text("Describe target audience, expected features, integration needs...") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_project_desc")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Estimated Budget *") },
            placeholder = { Text("e.g. ₹30,000 - ₹50,000") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_project_budget")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Target Completion Timeline *") },
            placeholder = { Text("e.g. 4 Weeks / Immediate") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_project_deadline")
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (projectTitle.isBlank() || description.isBlank()) {
                    errorMessage = "Project title and description are required."
                    return@Button
                }
                isSubmitting = true
                errorMessage = null
                viewModel.submitServiceRequest(
                    serviceId = service?.id ?: "srv_custom",
                    serviceTitle = service?.title ?: "Custom Project",
                    projectTitle = projectTitle,
                    description = description,
                    budget = budget,
                    deadline = deadline
                ) { req ->
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_service_request")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Submit Service Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
