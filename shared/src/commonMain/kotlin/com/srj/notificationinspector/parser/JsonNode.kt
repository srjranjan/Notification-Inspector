package com.srj.notificationinspector.parser

sealed interface JsonNode {
    val key: String
    val depth: Int

    data class CollapsibleNode(
        override val key: String,
        override val depth: Int,
        val children: List<JsonNode>,
        val isObject: Boolean, // true = {}, false = []
        var isExpanded: Boolean = true
    ) : JsonNode

    data class LeafNode(
        override val key: String,
        override val depth: Int,
        val value: String,
        val valueType: JsonValueType
    ) : JsonNode
}

enum class JsonValueType { STRING, NUMBER, BOOLEAN, NULL }
