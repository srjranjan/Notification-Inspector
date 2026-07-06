package com.srj.notificationinspector.parser

class JsonParser(private val json: String) {
    private var index = 0

    fun parse(rootKey: String = "payload"): JsonNode {
        skipWhitespace()
        if (index >= json.length) {
            return JsonNode.LeafNode(rootKey, 0, "{}", JsonValueType.STRING)
        }
        return parseValue(rootKey, 0)
    }

    private fun parseValue(key: String, depth: Int): JsonNode {
        skipWhitespace()
        if (index >= json.length) return JsonNode.LeafNode(key, depth, "null", JsonValueType.NULL)

        return when (val char = json[index]) {
            '{' -> parseObject(key, depth)
            '[' -> parseArray(key, depth)
            '"' -> {
                val value = parseString()
                JsonNode.LeafNode(key, depth, value, JsonValueType.STRING)
            }
            else -> {
                if (char.isDigit() || char == '-') {
                    val value = parseNumber()
                    JsonNode.LeafNode(key, depth, value, JsonValueType.NUMBER)
                } else if (json.startsWith("true", index)) {
                    index += 4
                    JsonNode.LeafNode(key, depth, "true", JsonValueType.BOOLEAN)
                } else if (json.startsWith("false", index)) {
                    index += 5
                    JsonNode.LeafNode(key, depth, "false", JsonValueType.BOOLEAN)
                } else if (json.startsWith("null", index)) {
                    index += 4
                    JsonNode.LeafNode(key, depth, "null", JsonValueType.NULL)
                } else {
                    // Fallback for malformed values
                    index++
                    JsonNode.LeafNode(key, depth, char.toString(), JsonValueType.STRING)
                }
            }
        }
    }

    private fun parseObject(key: String, depth: Int): JsonNode.CollapsibleNode {
        index++ // Skip '{'
        val children = mutableListOf<JsonNode>()

        while (index < json.length) {
            skipWhitespace()
            if (index >= json.length) break
            if (json[index] == '}') {
                index++
                break
            }

            if (json[index] == ',') {
                index++
                continue
            }

            skipWhitespace()
            if (json[index] != '"') {
                // Malformed JSON key
                index++
                continue
            }

            val childKey = parseString()
            skipWhitespace()

            if (index < json.length && json[index] == ':') {
                index++ // Skip ':'
            }

            val childNode = parseValue(childKey, depth + 1)
            children.add(childNode)
        }

        return JsonNode.CollapsibleNode(
            key = key,
            depth = depth,
            children = children,
            isObject = true
        )
    }

    private fun parseArray(key: String, depth: Int): JsonNode.CollapsibleNode {
        index++ // Skip '['
        val children = mutableListOf<JsonNode>()
        var itemIndex = 0

        while (index < json.length) {
            skipWhitespace()
            if (index >= json.length) break
            if (json[index] == ']') {
                index++
                break
            }

            if (json[index] == ',') {
                index++
                continue
            }

            val childNode = parseValue(itemIndex.toString(), depth + 1)
            children.add(childNode)
            itemIndex++
        }

        return JsonNode.CollapsibleNode(
            key = key,
            depth = depth,
            children = children,
            isObject = false
        )
    }

    private fun parseString(): String {
        index++ // Skip starting '"'
        val sb = StringBuilder()
        var escaped = false

        while (index < json.length) {
            val char = json[index]
            if (escaped) {
                when (char) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('')
                    else -> sb.append(char)
                }
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else if (char == '"') {
                index++ // Skip ending '"'
                break
            } else {
                sb.append(char)
            }
            index++
        }
        return sb.toString()
    }

    private fun parseNumber(): String {
        val sb = StringBuilder()
        while (index < json.length) {
            val char = json[index]
            if (char.isDigit() || char == '.' || char == '-' || char == '+' || char == 'e' || char == 'E') {
                sb.append(char)
                index++
            } else {
                break
            }
        }
        return sb.toString()
    }

    private fun skipWhitespace() {
        while (index < json.length && json[index].isWhitespace()) {
            index++
        }
    }
}
