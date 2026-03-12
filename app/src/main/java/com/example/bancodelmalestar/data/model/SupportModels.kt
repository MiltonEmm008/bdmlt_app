package com.example.bancodelmalestar.data.model

import com.google.gson.annotations.SerializedName

data class SupportChatRequest(
    @SerializedName("session_id") val sessionId: String? = null,
    val message: String
)

data class SupportChatResponse(
    @SerializedName("session_id") val sessionId: String,
    val reply: String,
    @SerializedName("memory_messages") val memoryMessages: Int
)

data class SupportHealthResponse(
    val status: String,
    val model: String,
    @SerializedName("base_url") val baseUrl: String,
    @SerializedName("chats_en_ram") val chatsEnRam: Int,
    @SerializedName("max_chats_en_ram") val maxChatsEnRam: Int,
    @SerializedName("max_mensajes_por_chat") val maxMensajesPorChat: Int,
    @SerializedName("ttl_segundos") val ttlSegundos: Int
)

data class ChatMessage(
    val content: String,
    val isUser: Boolean
)
