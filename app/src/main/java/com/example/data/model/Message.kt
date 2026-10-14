package com.example.data.model

enum class MessageType {
  TEXT,
  AUDIO,
  IMAGE,
  FILE
}

data class Message(
  val id: String = "",
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val senderAvatar: String = "",
  val text: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val type: MessageType = MessageType.TEXT,
  val mediaUrl: String? = null,
  val mediaLabel: String? = null,
  val audioDuration: String? = null,
  val replyToId: String? = null,
  val replyToText: String? = null,
  val replyToAuthor: String? = null,
  val reactions: Map<String, Int> = emptyMap(),
  val isRead: Boolean = true,
  val isDelivered: Boolean = true,
  val isOutgoing: Boolean = false
)
