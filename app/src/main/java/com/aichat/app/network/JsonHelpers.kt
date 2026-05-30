package com.aichat.app.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal val NetworkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun JsonObject.stringAt(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

internal fun JsonObject.booleanAt(key: String): Boolean? =
    this[key]?.jsonPrimitive?.booleanOrNull

internal fun JsonObject.objectAt(key: String): JsonObject? =
    this[key] as? JsonObject

internal fun JsonObject.arrayAt(key: String): JsonArray? =
    this[key] as? JsonArray

internal fun JsonElement.asObjectOrNull(): JsonObject? =
    this as? JsonObject

internal fun JsonElement.asArrayOrNull(): JsonArray? =
    this as? JsonArray

@Suppress("UnusedReceiverParameter")
internal fun JsonPrimitive.safeContent(): String =
    contentOrNull.orEmpty()
