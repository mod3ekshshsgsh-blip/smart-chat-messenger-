package com.example.data.model

enum class ConversationCategory {
  ALL,
  UNREAD,
  GROUPS,
  CHANNELS,
  ARCHIVED
}

data class Conversation(
  val id: String = "",
  val title: String = "",
  val isGroup: Boolean = false,
  val isPinned: Boolean = false,
  val participants: List<String> = emptyList(),
  val participantAvatars: List<String> = emptyList(),
  val lastMessage: String = "",
  val lastMessageSender: String = "",
  val lastMessageTimestamp: Long = System.currentTimeMillis(),
  val lastMessageType: MessageType = MessageType.TEXT,
  val unreadCount: Int = 0,
  val isVerified: Boolean = false,
  val isOnline: Boolean = false,
  val isTyping: Boolean = false,
  val category: ConversationCategory = ConversationCategory.ALL
)
