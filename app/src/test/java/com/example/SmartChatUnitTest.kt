package com.example

import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.data.model.FriendRequest
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.RequestStatus
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartChatUnitTest {

  @Test
  fun testUserModel_validationAndNormalization() {
    val user = User(
      uid = "usr_123",
      displayName = "Alex Rivera",
      username = "AlexRivera",
      usernameNormalized = "AlexRivera".lowercase(),
      email = "alex@smartchat.io"
    )

    assertEquals("usr_123", user.uid)
    assertEquals("Alex Rivera", user.displayName)
    assertEquals("alexrivera", user.usernameNormalized)
    assertTrue(user.isOnline)
  }

  @Test
  fun testMessageModel_reactionsAndAudio() {
    val message = Message(
      id = "msg_elena_audio",
      conversationId = "conv_elena",
      senderId = "elena",
      text = "Voice note",
      type = MessageType.AUDIO,
      audioDuration = "0:42 / 1:15",
      reactions = mapOf("👍" to 1, "❤️" to 1)
    )

    assertEquals(MessageType.AUDIO, message.type)
    assertEquals("0:42 / 1:15", message.audioDuration)
    assertEquals(1, message.reactions["👍"])
    assertEquals(1, message.reactions["❤️"])
    assertFalse(message.isOutgoing)
  }

  @Test
  fun testConversationFiltering_byCategory() {
    val conversations = listOf(
      Conversation(id = "1", title = "Design Core", isGroup = true, unreadCount = 3, category = ConversationCategory.GROUPS),
      Conversation(id = "2", title = "Elena", isGroup = false, unreadCount = 1, category = ConversationCategory.ALL),
      Conversation(id = "3", title = "Liam", isGroup = false, unreadCount = 0, category = ConversationCategory.ALL)
    )

    val unreadList = conversations.filter { it.unreadCount > 0 }
    val groupsList = conversations.filter { it.isGroup }

    assertEquals(2, unreadList.size)
    assertEquals(1, groupsList.size)
    assertEquals("Design Core", groupsList[0].title)
  }

  @Test
  fun testFriendRequest_statusTransitions() {
    val request = FriendRequest(
      id = "req_1",
      fromUid = "sarah_jenkins",
      fromName = "Sarah Jenkins",
      status = RequestStatus.PENDING
    )

    assertEquals(RequestStatus.PENDING, request.status)
    val accepted = request.copy(status = RequestStatus.ACCEPTED)
    assertEquals(RequestStatus.ACCEPTED, accepted.status)
  }

  @Test
  fun testContactModel_savedAndVerifiedStatus() {
    val contact = com.example.data.model.Contact(
      id = "contact_001",
      name = "Sarah Miller",
      identifier = "sarah@example.com",
      identifierType = "EMAIL",
      isRegisteredUser = true,
      registeredUid = "user_sarah_uid"
    )

    assertEquals("contact_001", contact.id)
    assertEquals("Sarah Miller", contact.name)
    assertEquals("sarah@example.com", contact.identifier)
    assertTrue(contact.isRegisteredUser)
    assertEquals("user_sarah_uid", contact.registeredUid)

    val localContact = contact.copy(
      isRegisteredUser = false,
      registeredUid = null
    )
    assertFalse(localContact.isRegisteredUser)
    assertEquals(null, localContact.registeredUid)
  }

  @Test
  fun testContactModel_searchFilter() {
    val contacts = listOf(
      com.example.data.model.Contact(id = "1", name = "David Lee", identifier = "david@company.com"),
      com.example.data.model.Contact(id = "2", name = "Emma Watson", identifier = "+1234567890"),
      com.example.data.model.Contact(id = "3", name = "Daniel Craig", identifier = "@danielc")
    )

    val query = "Dan"
    val filtered = contacts.filter {
      it.name.contains(query, ignoreCase = true) || it.identifier.contains(query, ignoreCase = true)
    }

    assertEquals(1, filtered.size)
    assertEquals("Daniel Craig", filtered.first().name)
  }
}

