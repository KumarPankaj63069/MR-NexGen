package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.PortfolioItem
import com.example.ui.components.EmptyState
import com.example.ui.components.PortfolioCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PortfolioScreen(viewModel: MainViewModel) {
    val portfolio by viewModel.publishedPortfolio.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Websites", "Android Apps", "Software", "UI/UX", "Digital Marketing")

    val filteredList = remember(portfolio, selectedCategory) {
        if (selectedCategory == "All") portfolio
        else portfolio.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    modifier = Modifier.testTag("portfolio_chip_${cat.lowercase()}")
                )
            }
        }

        if (filteredList.isEmpty()) {
            EmptyState(
                icon = Icons.Default.WorkOff,
                title = "No Projects Found",
                message = "There are no showcased portfolio items under $selectedCategory currently.",
                actionButtonText = "View All Projects",
                onActionClick = { selectedCategory = "All" }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    PortfolioCard(
                        item = item,
                        onItemClick = { viewModel.selectPortfolio(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioDetailScreen(viewModel: MainViewModel) {
    val item = viewModel.selectedPortfolio.collectAsState().value
    val context = LocalContext.current

    if (item == null) {
        EmptyState(
            icon = Icons.Default.Info,
            title = "No Project Selected",
            message = "Select a portfolio project to view technical specifications.",
            actionButtonText = "Back to Portfolio",
            onActionClick = { viewModel.navigateBack() }
        )
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
        Surface(
            color = NexGenCyan.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = item.category.uppercase(),
                color = NexGenNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Client: ${item.clientName}",
            style = MaterialTheme.typography.bodyMedium,
            color = NexGenCyanDark,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NexGenSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Project Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.fullDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexGenTextSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Technologies & Frameworks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            item.technologies.forEach { tech ->
                Surface(
                    color = NexGenNavy.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tech,
                        color = NexGenNavy,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (item.projectUrl.isNotBlank()) {
            OutlinedButton(
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.projectUrl))
                    context.startActivity(browserIntent)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit Live Application")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                viewModel.navigateTo(Screen.ServiceRequestForm)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NexGenNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_build_similar_project")
        ) {
            Text("Request Similar Project Development", fontWeight = FontWeight.Bold)
        }
    }
}
