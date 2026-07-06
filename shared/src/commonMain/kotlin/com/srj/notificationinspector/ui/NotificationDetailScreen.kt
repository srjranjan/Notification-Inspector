package com.srj.notificationinspector.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.parser.JsonNode
import com.srj.notificationinspector.parser.JsonParser
import com.srj.notificationinspector.parser.JsonValueType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    log: NotificationLog,
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isAllExpanded by remember { mutableStateOf(true) }

    // Parse JSON payload to AST
    val rootNode = remember(log.rawPayload) {
        try {
            JsonParser(log.rawPayload).parse()
        } catch (e: Exception) {
            JsonNode.LeafNode("payload", 0, log.rawPayload, JsonValueType.STRING)
        }
    }

    var refreshTrigger by remember { mutableStateOf(0) }

    fun toggleAllExpansion(node: JsonNode, expand: Boolean) {
        if (node is JsonNode.CollapsibleNode) {
            node.isExpanded = expand
            node.children.forEach { toggleAllExpansion(it, expand) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("< Back", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        isAllExpanded = !isAllExpanded
                        toggleAllExpansion(rootNode, isAllExpanded)
                        refreshTrigger++
                    }) {
                        Text(if (isAllExpanded) "Collapse All" else "Expand All", color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(log.rawPayload))
                    }) {
                        Text("Copy Raw")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // General Details Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = log.title ?: "No Title",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.body ?: "No Body",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Timestamp: ${formatTimestamp(log.timestamp)}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Payload Label
            item {
                Text(
                    text = "PAYLOAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Interactive JSON AST Tree Renderer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    key(refreshTrigger) {
                        JsonTreeRenderer(node = rootNode)
                    }
                }
            }
        }
    }
}

@Composable
fun JsonTreeRenderer(node: JsonNode) {
    val indentation = (12 * node.depth).dp

    when (node) {
        is JsonNode.LeafNode -> {
            Row(
                modifier = Modifier
                    .padding(start = indentation)
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "  ", // Match indent of toggle arrow
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    text = "\"${node.key}\": ",
                    color = Color(0xFF9CDCFE),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (node.valueType == JsonValueType.STRING) "\"${node.value}\"" else node.value,
                    color = when (node.valueType) {
                        JsonValueType.STRING -> Color(0xFFCE9178)
                        JsonValueType.NUMBER -> Color(0xFFB5CEA8)
                        JsonValueType.BOOLEAN -> Color(0xFF569CD6)
                        JsonValueType.NULL -> Color(0xFF569CD6)
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
        is JsonNode.CollapsibleNode -> {
            var localExpanded by remember(node, node.isExpanded) { mutableStateOf(node.isExpanded) }
            val bracketOpen = if (node.isObject) "{" else "["
            val bracketClose = if (node.isObject) "}" else "]"

            Column {
                Row(
                    modifier = Modifier
                        .padding(start = indentation)
                        .clickable { localExpanded = !localExpanded }
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (localExpanded) "▼" else "▶",
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.width(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "\"${node.key}\": $bracketOpen",
                        color = Color(0xFFD4D4D4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!localExpanded) {
                        Text(
                            text = " ... $bracketClose",
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }

                AnimatedVisibility(visible = localExpanded) {
                    Column {
                        node.children.forEach { child ->
                            JsonTreeRenderer(child)
                        }
                        Text(
                            text = bracketClose,
                            color = Color(0xFFD4D4D4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = indentation + 16.dp)
                        )
                    }
                }
            }
        }
    }
}
