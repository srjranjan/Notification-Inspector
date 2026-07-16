package com.srj.notificationinspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.theme.*
import com.srj.notificationinspector.util.Util.toSp
import kotlinx.coroutines.launch

import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationEditPayloadScreen(
    log: NotificationLog,
    onNavigateBack: () -> Unit,
    onReplayWithPayload: (NotificationLog) -> Unit
) {
    var title by remember { mutableStateOf(log.title ?: "") }
    var body by remember { mutableStateOf(log.body ?: "") }

    // Auto-format by default
    var rawPayload by remember {
        mutableStateOf(try { formatJson(log.rawPayload) } catch(e: Exception) { log.rawPayload })
    }

    // Live validation
    val jsonError = remember(rawPayload) { validateJson(rawPayload) }
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    // Define syntax colors
    val syntaxColors = if (isDark) {
        JsonSyntaxColors(
            key = JsonKey,
            string = JsonString,
            number = JsonNumber,
            boolean = JsonBoolean,
            nullValue = JsonNull,
            punctuation = TextMutedDark
        )
    } else {
        JsonSyntaxColors(
            key = JsonKeyLight,
            string = JsonStringLight,
            number = JsonNumberLight,
            boolean = JsonBooleanLight,
            nullValue = JsonNullLight,
            punctuation = TextMutedLight
        )
    }

    val jsonTransformation = remember(syntaxColors) {
        JsonVisualTransformation(syntaxColors)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Notification", fontSize = 18.dp.toSp(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // General Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Metadata & Alert Values",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = body,
                            onValueChange = { body = it },
                            label = { Text("Body") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // JSON Payload Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) CodeBlockDark else CodeBlockLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Custom JSON Payload Data",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            TextButton(
                                onClick = {
                                    if (jsonError == null) {
                                        rawPayload = formatJson(rawPayload)
                                    }
                                },
                                enabled = jsonError == null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Format JSON",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Format JSON", fontSize = 12.dp.toSp())
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = rawPayload,
                                onValueChange = { rawPayload = it },
                                placeholder = { Text("{}") },
                                modifier = Modifier.width(2000.dp), // Set a large width to prevent wrapping and allow horizontal scroll
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.dp.toSp()
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Default
                                ),
                                isError = jsonError != null,
                                visualTransformation = jsonTransformation,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        if (jsonError != null) {
                            Text(
                                text = jsonError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.dp.toSp(),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Fixed Action Button at the bottom
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        if (jsonError == null) {
                            val updatedLog = log.copy(
                                title = title.takeIf { it.isNotBlank() },
                                body = body.takeIf { it.isNotBlank() },
                                rawPayload = rawPayload
                            )
                            onReplayWithPayload(updatedLog)
                        }
                    },
                    enabled = jsonError == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Replay as New Notification",
                        fontSize = 16.dp.toSp(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Live JSON Validation helper
private fun validateJson(json: String): String? {
    val trimmed = json.trim()
    if (trimmed.isEmpty()) return "JSON cannot be empty"
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
        return "JSON must start with '{' and end with '}'"
    }

    var inString = false
    var escape = false
    val stack = mutableListOf<Char>()
    for (i in trimmed.indices) {
        val c = trimmed[i]
        if (escape) {
            escape = false
            continue
        }
        if (c == '\\') {
            escape = true
            continue
        }
        if (c == '"') {
            inString = !inString
            continue
        }
        if (!inString) {
            if (c == '{' || c == '[') {
                stack.add(c)
            } else if (c == '}') {
                if (stack.isEmpty() || stack.last() != '{') {
                    return "Unexpected closing brace '}'"
                }
                stack.removeAt(stack.lastIndex)
            } else if (c == ']') {
                if (stack.isEmpty() || stack.last() != '[') {
                    return "Unexpected closing bracket ']'"
                }
                stack.removeAt(stack.lastIndex)
            }
        }
    }
    if (inString) return "Unclosed string literal"
    if (stack.isNotEmpty()) {
        val expected = if (stack.last() == '{') "'}'" else "']'"
        return "Missing closing bracket/brace. Expected $expected"
    }

    return null
}

// JSON Formatter helper
private fun formatJson(json: String): String {
    val result = StringBuilder()
    var indentLevel = 0
    val indent = "    "
    var inString = false
    var escape = false

    val trimmed = json.trim()
    for (i in trimmed.indices) {
        val c = trimmed[i]
        if (escape) {
            result.append(c)
            escape = false
            continue
        }
        if (c == '\\') {
            result.append(c)
            escape = true
            continue
        }
        if (c == '"') {
            result.append(c)
            inString = !inString
            continue
        }
        if (inString) {
            result.append(c)
            continue
        }

        when (c) {
            '{', '[' -> {
                result.append(c)
                result.append("\n")
                indentLevel++
                result.append(indent.repeat(indentLevel))
            }
            '}', ']' -> {
                result.append("\n")
                indentLevel--
                if (indentLevel < 0) indentLevel = 0
                result.append(indent.repeat(indentLevel))
                result.append(c)
            }
            ',' -> {
                result.append(c)
                result.append("\n")
                result.append(indent.repeat(indentLevel))
            }
            ':' -> {
                result.append(c)
                result.append(" ")
            }
            else -> {
                if (!c.isWhitespace()) {
                    result.append(c)
                }
            }
        }
    }
    return result.toString()
}

data class JsonSyntaxColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val boolean: Color,
    val nullValue: Color,
    val punctuation: Color
)

class JsonVisualTransformation(private val colors: JsonSyntaxColors) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = highlightJson(text.text, colors),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

private fun highlightJson(json: String, colors: JsonSyntaxColors): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0
    val length = json.length

    while (i < length) {
        val c = json[i]

        when {
            // Strings (Keys or Values)
            c == '"' -> {
                val start = i
                i++
                var escaped = false
                while (i < length) {
                    val current = json[i]
                    if (escaped) {
                        escaped = false
                    } else if (current == '\\') {
                        escaped = true
                    } else if (current == '"') {
                        break
                    }
                    i++
                }
                i++ // include closing quote
                val end = i

                // Peek ahead to see if it's a key
                var isKey = false
                var j = i
                while (j < length && json[j].isWhitespace()) j++
                if (j < length && json[j] == ':') {
                    isKey = true
                }

                builder.append(json.substring(start, end))
                builder.addStyle(
                    style = SpanStyle(color = if (isKey) colors.key else colors.string, fontWeight = if (isKey) FontWeight.Bold else FontWeight.Normal),
                    start = builder.length - (end - start),
                    end = builder.length
                )
            }
            // Brackets/Braces
            c == '{' || c == '}' || c == '[' || c == ']' -> {
                builder.append(c)
                builder.addStyle(
                    style = SpanStyle(color = colors.punctuation, fontWeight = FontWeight.Bold),
                    start = builder.length - 1,
                    end = builder.length
                )
                i++
            }
            // Colons/Commas
            c == ':' || c == ',' -> {
                builder.append(c)
                builder.addStyle(
                    style = SpanStyle(color = colors.punctuation),
                    start = builder.length - 1,
                    end = builder.length
                )
                i++
            }
            // Numbers, Booleans, Null
            c.isLetterOrDigit() || c == '-' -> {
                val start = i
                while (i < length && (json[i].isLetterOrDigit() || json[i] == '.' || json[i] == '-')) {
                    i++
                }
                val token = json.substring(start, i)
                builder.append(token)

                val style = when {
                    token == "true" || token == "false" -> SpanStyle(color = colors.boolean, fontWeight = FontWeight.SemiBold)
                    token == "null" -> SpanStyle(color = colors.nullValue, fontWeight = FontWeight.SemiBold)
                    token.any { it.isDigit() } -> SpanStyle(color = colors.number)
                    else -> SpanStyle(color = colors.string) // Fallback
                }

                builder.addStyle(
                    style = style,
                    start = builder.length - token.length,
                    end = builder.length
                )
            }
            else -> {
                builder.append(c)
                i++
            }
        }
    }
    return builder.toAnnotatedString()
}
