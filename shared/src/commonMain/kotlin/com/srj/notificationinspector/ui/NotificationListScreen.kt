package com.srj.notificationinspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.repository.NotificationRepository
import com.srj.notificationinspector.ui.formatTimestamp
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    repository: NotificationRepository,
    onNavigateToDetail: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    // Collect logs from database reactively
    val logsFlow = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            repository.getAllLogs()
        } else {
            repository.searchLogs("%$searchQuery%")
        }
    }
    val logs by logsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Inspector", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            repository.clearAllLogs()
                        }
                    }) {
                        Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input with Emoji leading icon
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                placeholder = { Text("Search logs...", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Text(
                        text = "🔍",
                        modifier = Modifier.padding(start = 12.dp),
                        fontSize = 16.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF334155),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            // Logs Listing
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔔",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "No notification logs intercepted yet." else "No results found.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogCard(log = log, onClick = { onNavigateToDetail(log.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun LogCard(
    log: NotificationLog,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = log.title ?: "No Title",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(log.timestamp).substringAfter(" "),
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = log.body ?: "No Body",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
