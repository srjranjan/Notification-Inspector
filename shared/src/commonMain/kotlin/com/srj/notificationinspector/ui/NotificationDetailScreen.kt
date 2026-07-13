package com.srj.notificationinspector.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
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
import com.srj.notificationinspector.theme.*
import com.srj.notificationinspector.util.Util.toSp
import org.jetbrains.compose.resources.vectorResource
import java.awt.SystemColor.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    log: NotificationLog,
    onNavigateBack: () -> Unit,
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
                title = { Text("Log Details", fontSize = 18.dp.toSp(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(log.rawPayload)) }
                    ) {
                        Icon(imageVector = Icons.Default.CopyAll, contentDescription = "Copy")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
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
        Spacer(Modifier.height(40.dp))
    }
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
