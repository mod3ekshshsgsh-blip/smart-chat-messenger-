package com.example.data.repository

import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.StoryMoment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ChatRepository {

  private var firestore: FirebaseFirestore? = null
  private var conversationsListener: ListenerRegistration? = null
  private val messageListeners = mutableMapOf<String, ListenerRegistration>()

  init {
    try {
      firestore = FirebaseFirestore.getInstance()
    } catch (_: Exception) {}
  }

  private val _stories = MutableStateFlow<List<StoryMoment>>(emptyList())
  val stories = _stories.asStateFlow()

  private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
  val conversations = _conversations.asStateFlow()

  private val _messagesMap = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

  private var currentUserId: String = ""

  fun attachUserConversations(uid: String) {
    currentUserId = uid
    conversationsListener?.remove()
    val db = firestore ?: return

    try {
      conversationsListener = db.collection("conversations")
        .whereArrayContains("participants", uid)
        .addSnapshotListener { snapshot, error ->
          if (error != null || snapshot == null) return@addSnapshotListener

          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val participants = doc.get("participants") as? List<String> ?: emptyList()
              val participantAvatars = doc.get("participantAvatars") as? List<String> ?: emptyList()
              val title = doc.getString("title") ?: "Chat"
              val lastMessage = doc.getString("lastMessage") ?: ""
              val lastMessageSender = doc.getString("lastMessageSender") ?: ""
              val lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: System.currentTimeMillis()
              val unreadCount = doc.getLong("unreadCount_${uid}")?.toInt() ?: 0
              val isVerified = doc.getBoolean("isVerified") ?: false

              Conversation(
                id = doc.id,
                title = title,
                isGroup = participants.size > 2,
                participants = participants,
                participantAvatars = participantAvatars,
                lastMessage = lastMessage,
                lastMessageSender = lastMessageSender,
                lastMessageTimestamp = lastMessageTimestamp,
                unreadCount = unreadCount,
                isVerified = isVerified,
                category = ConversationCategory.ALL
              )
            } catch (_: Exception) {
              null
            }
          }.sortedByDescending { it.lastMessageTimestamp }

          _conversations.value = list
        }
    } catch (_: Exception) {}
  }

  fun getMessages(conversationId: String): Flow<List<Message>> {
    attachMessagesListener(conversationId)
    return _messagesMap.map { map ->
      map[conversationId] ?: emptyList()
    }
  }

  private fun attachMessagesListener(conversationId: String) {
    if (messageListeners.containsKey(conversationId)) return
    val db = firestore ?: return

    try {
      val reg = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null || snapshot == null) return@addSnapshotListener

          val messages = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.id
              val senderId = doc.getString("senderId") ?: ""
              val senderName = doc.getString("senderName") ?: ""
              val senderAvatar = doc.getString("senderAvatar") ?: ""
              val text = doc.getString("text") ?: ""
              val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
              val typeStr = doc.getString("type") ?: "TEXT"
              val type = try { MessageType.valueOf(typeStr) } catch (_: Exception) { MessageType.TEXT }
              val audioDuration = doc.getString("audioDuration")
              val mediaUrl = doc.getString("mediaUrl")
              val mediaLabel = doc.getString("mediaLabel")
              val replyToText = doc.getString("replyToText")
              val replyToAuthor = doc.getString("replyToAuthor")
              val isOutgoing = (senderId.isNotBlank() && senderId == currentUserId) || (doc.getBoolean("isOutgoing") ?: false)

              Message(
                id = id,
                conversationId = conversationId,
                senderId = senderId,
                senderName = senderName,
                senderAvatar = senderAvatar,
                text = text,
                timestamp = timestamp,
                type = type,
                audioDuration = audioDuration,
                mediaUrl = mediaUrl,
                mediaLabel = mediaLabel,
                replyToText = replyToText,
                replyToAuthor = replyToAuthor,
                isOutgoing = isOutgoing
              )
            } catch (_: Exception) {
              null
            }
          }

          val currentMap = _messagesMap.value.toMutableMap()
          currentMap[conversationId] = messages
          _messagesMap.value = currentMap
        }

      messageListeners[conversationId] = reg
    } catch (_: Exception) {}
  }

  suspend fun createNewConversation(
    title: String,
    currentUserId: String,
    currentUserName: String,
    recipientIdentifier: String
  ): String {
    val cleanTitle = title.ifBlank { recipientIdentifier.substringBefore("@") }
    val newConvId = "conv_${System.currentTimeMillis()}"

    val newConv = Conversation(
      id = newConvId,
      title = cleanTitle,
      isGroup = false,
      participants = listOf(currentUserId, recipientIdentifier),
      lastMessage = "Started a new conversation",
      lastMessageSender = currentUserName,
      lastMessageTimestamp = System.currentTimeMillis(),
      unreadCount = 0
    )

    // Add locally immediately
    val current = _conversations.value.toMutableList()
    current.add(0, newConv)
    _conversations.value = current

    // Persist to Cloud Firestore if connected
    try {
      val data = hashMapOf(
        "title" to cleanTitle,
        "participants" to listOf(currentUserId, recipientIdentifier),
        "lastMessage" to "Started a new conversation",
        "lastMessageSender" to currentUserName,
        "lastMessageTimestamp" to System.currentTimeMillis(),
        "createdAt" to System.currentTimeMillis()
      )
      firestore?.collection("conversations")?.document(newConvId)?.set(data)
    } catch (_: Exception) {}

    return newConvId
  }

  fun sendMessage(
    conversationId: String,
    text: String,
    currentUserId: String = "me",
    currentUserName: String = "Me",
    replyToId: String? = null,
    replyToText: String? = null,
    replyToAuthor: String? = null
  ) {
    if (text.isBlank()) return
    val msgId = "msg_${System.currentTimeMillis()}"
    val newMsg = Message(
      id = msgId,
      conversationId = conversationId,
      senderId = currentUserId,
      senderName = currentUserName,
      text = text.trim(),
      timestamp = System.currentTimeMillis(),
      type = MessageType.TEXT,
      replyToId = replyToId,
      replyToText = replyToText,
      replyToAuthor = replyToAuthor,
      isOutgoing = true,
      isDelivered = true,
      isRead = false
    )

    // Local update
    val currentMap = _messagesMap.value.toMutableMap()
    val list = currentMap[conversationId]?.toMutableList() ?: mutableListOf()
    list.add(newMsg)
    currentMap[conversationId] = list
    _messagesMap.value = currentMap

    // Update conversation preview
    val convs = _conversations.value.map { conv ->
      if (conv.id == conversationId) {
        conv.copy(
          lastMessage = text.trim(),
          lastMessageSender = "You",
          lastMessageTimestamp = System.currentTimeMillis()
        )
      } else conv
    }
    _conversations.value = convs

    // Push to Firestore
    try {
      val msgData = hashMapOf(
        "senderId" to currentUserId,
        "senderName" to currentUserName,
        "text" to text.trim(),
        "timestamp" to System.currentTimeMillis(),
        "type" to "TEXT",
        "replyToText" to replyToText,
        "replyToAuthor" to replyToAuthor,
        "isOutgoing" to true
      )
      firestore?.collection("conversations")
        ?.document(conversationId)
        ?.collection("messages")
        ?.document(msgId)
        ?.set(msgData)

      firestore?.collection("conversations")?.document(conversationId)?.update(
        mapOf(
          "lastMessage" to text.trim(),
          "lastMessageSender" to currentUserName,
          "lastMessageTimestamp" to System.currentTimeMillis()
        )
      )
    } catch (_: Exception) {}
  }

  fun sendVoiceNote(
    conversationId: String,
    duration: String = "0:30",
    currentUserId: String = "me",
    currentUserName: String = "Me"
  ) {
    val msgId = "msg_${System.currentTimeMillis()}"
    val newMsg = Message(
      id = msgId,
      conversationId = conversationId,
      senderId = currentUserId,
      senderName = currentUserName,
      text = "Voice message",
      timestamp = System.currentTimeMillis(),
      type = MessageType.AUDIO,
      audioDuration = duration,
      isOutgoing = true,
      isDelivered = true,
      isRead = false
    )

    val currentMap = _messagesMap.value.toMutableMap()
    val list = currentMap[conversationId]?.toMutableList() ?: mutableListOf()
    list.add(newMsg)
    currentMap[conversationId] = list
    _messagesMap.value = currentMap
  }

  fun addReaction(conversationId: String, messageId: String, emoji: String) {
    val currentMap = _messagesMap.value.toMutableMap()
    val list = currentMap[conversationId]?.toMutableList() ?: return
    val index = list.indexOfFirst { it.id == messageId }
    if (index >= 0) {
      val msg = list[index]
      val currentReactions = msg.reactions.toMutableMap()
      val currentCount = currentReactions[emoji] ?: 0
      currentReactions[emoji] = currentCount + 1
      list[index] = msg.copy(reactions = currentReactions)
      currentMap[conversationId] = list
      _messagesMap.value = currentMap
    }
  }

  fun markConversationRead(conversationId: String) {
    val convs = _conversations.value.map { conv ->
      if (conv.id == conversationId) conv.copy(unreadCount = 0) else conv
    }
    _conversations.value = convs
  }

  fun clearConversations() {
    conversationsListener?.remove()
    conversationsListener = null
    messageListeners.values.forEach { it.remove() }
    messageListeners.clear()
    _conversations.value = emptyList()
    _messagesMap.value = emptyMap()
    _stories.value = emptyList()
    currentUserId = ""
  }
}
