package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import com.example.data.model.BlogPost
import com.example.ui.components.BlogCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BlogScreen(viewModel: MainViewModel) {
    val blogs by viewModel.publishedBlogs.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }

    val filteredBlogs = remember(blogs, searchKeyword) {
        if (searchKeyword.isBlank()) blogs
        else blogs.filter {
            it.title.contains(searchKeyword, ignoreCase = true) ||
                    it.category.contains(searchKeyword, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(searchKeyword, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            placeholder = { Text("Search blogs by topic, tag, or title...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchKeyword.isNotEmpty()) {
                    IconButton(onClick = { searchKeyword = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("input_search_blogs")
        )

        if (filteredBlogs.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Article,
                title = "No Articles Found",
                message = "Try searching for a different keyword or topic.",
                actionButtonText = "Clear Search",
                onActionClick = { searchKeyword = "" }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBlogs, key = { it.id }) { blog ->
                    BlogCard(
                        blog = blog,
                        onBlogClick = { viewModel.selectBlog(blog) }
                    )
                }
            }
        }
    }
}

@Composable
fun BlogDetailScreen(viewModel: MainViewModel) {
    val blog = viewModel.selectedBlog.collectAsState().value
    val favorites by viewModel.userFavorites.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    if (blog == null) {
        EmptyState(
            icon = Icons.Default.Info,
            title = "No Article Selected",
            message = "Choose an article from the blog catalogue to read.",
            actionButtonText = "Back to Blogs",
            onActionClick = { viewModel.navigateBack() }
        )
        return
    }

    val isFav = favorites.any { it.itemId == blog.id }

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
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = blog.category,
                    color = Color(0xFF047857),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Row {
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, blog.title)
                            putExtra(Intent.EXTRA_TEXT, "${blog.title}\n\nRead more on MR NexGen IT Services: https://www.mrnexgen.com")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Blog Article"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NexGenTextSecondary)
                }
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(
                            itemType = "BLOG",
                            itemId = blog.id,
                            title = blog.title,
                            subtitle = "${blog.category} • ${blog.authorName}"
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isFav) NexGenNavy else NexGenTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = blog.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "By ${blog.authorName} • ${blog.readTimeMinutes} min read • ${dateFormat.format(Date(blog.publishedAt))}",
            style = MaterialTheme.typography.bodySmall,
            color = NexGenTextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(color = NexGenBorder)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = blog.excerpt,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = NexGenNavy,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = blog.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )

        if (blog.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                blog.tags.forEach { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexGenCyanDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
