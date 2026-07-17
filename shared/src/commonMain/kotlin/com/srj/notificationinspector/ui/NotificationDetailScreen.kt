package com.srj.notificationinspector.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.parser.JsonNode
import com.srj.notificationinspector.parser.JsonParser
import com.srj.notificationinspector.parser.JsonValueType
import com.srj.notificationinspector.theme.BackgroundDark
import com.srj.notificationinspector.theme.CodeBlockDark
import com.srj.notificationinspector.theme.CodeBlockLight
import com.srj.notificationinspector.theme.JsonBooleanLight
import com.srj.notificationinspector.theme.JsonKeyLight
import com.srj.notificationinspector.theme.JsonNullLight
import com.srj.notificationinspector.theme.JsonNumberLight
import com.srj.notificationinspector.theme.JsonStringLight
import com.srj.notificationinspector.util.Util.toSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    log: NotificationLog,
    onNavigateBack: () -> Unit,
    onReplay: ((NotificationLog) -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
    onEditPayload: ((Long) -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    var isAllExpanded by remember { mutableStateOf(true) }
    var showBottomSheet by remember { mutableStateOf(false) }

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
                title = { Text("Log Details", fontSize = 18.dp.toSp(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onShare != null) {
                        IconButton(
                            onClick = {
                                val sharedText = formatLogForSharing(log)
                                onShare.invoke(sharedText)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // General Details Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = log.title ?: "No Title",
                                fontSize = 18.dp.toSp(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.body ?: "No Body",
                                fontSize = 14.dp.toSp(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Timestamp: ${formatTimestamp(log.timestamp)}",
                                fontSize = 12.dp.toSp(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Payload Label
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PAYLOAD",
                            fontSize = 12.dp.toSp(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            isAllExpanded = !isAllExpanded
                            toggleAllExpansion(rootNode, isAllExpanded)
                            refreshTrigger++
                        },) {
                            Text(
                                if (isAllExpanded) "Collapse All" else "Expand All",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Interactive JSON AST Tree Renderer
                item {
                    val isDark = MaterialTheme.colorScheme.background == BackgroundDark
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) CodeBlockDark else CodeBlockLight, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        key(refreshTrigger) {
                            JsonTreeRenderer(node = rootNode)
                        }
                    }
                }
            }

            // Fixed Replay CTA at the bottom
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Replay Options",
                        fontSize = 16.dp.toSp(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(40.dp))
        
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Replay Notification",
                        fontSize = 18.dp.toSp(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Button(
                        onClick = {
                            showBottomSheet = false
                            onReplay?.invoke(log)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Replay with same payload", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            showBottomSheet = false
                            onEditPayload?.invoke(log.id)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Payload", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun formatLogForSharing(log: NotificationLog): String {
    return """
        🔔 Notification Inspector Log
        ------------------------------
        Title: ${log.title ?: "No Title"}
        Body: ${log.body ?: "No Body"}
        Timestamp: ${formatTimestamp(log.timestamp)}

        Raw Payload:
        ${log.rawPayload}
    """.trimIndent()
}

@Composable
fun JsonTreeRenderer(node: JsonNode) {
    val indentation = (12 * node.depth).dp
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    val keyColor = if (isDark) Color(0xFF9CDCFE) else JsonKeyLight
    val stringColor = if (isDark) Color(0xFFCE9178) else JsonStringLight
    val numberColor = if (isDark) Color(0xFFB5CEA8) else JsonNumberLight
    val booleanColor = if (isDark) Color(0xFF569CD6) else JsonBooleanLight
    val nullColor = if (isDark) Color(0xFF569CD6) else JsonNullLight

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
                    color = keyColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.dp.toSp(),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (node.valueType == JsonValueType.STRING) "\"${node.value}\"" else node.value,
                    color = when (node.valueType) {
                        JsonValueType.STRING -> stringColor
                        JsonValueType.NUMBER -> numberColor
                        JsonValueType.BOOLEAN -> booleanColor
                        JsonValueType.NULL -> nullColor
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.dp.toSp()
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.dp.toSp(),
                        modifier = Modifier.width(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "\"${node.key}\": $bracketOpen",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.dp.toSp(),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!localExpanded) {
                        Text(
                            text = " ... $bracketClose",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.dp.toSp()
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.dp.toSp(),
                            modifier = Modifier.padding(start = indentation + 16.dp)
                        )
                    }
                }
            }
        }
    }
}
