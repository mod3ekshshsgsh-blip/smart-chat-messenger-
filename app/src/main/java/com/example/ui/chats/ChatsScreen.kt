package com.example.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.data.model.MessageType
import com.example.data.model.StoryMoment
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.Tertiary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(
  chatRepository: ChatRepository,
  authRepository: AuthRepository? = null,
  onNavigateToConversation: (String) -> Unit,
  onNavigateTab: (String) -> Unit
) {
  val currentUser by (authRepository?.currentUser ?: remember { MutableStateFlow(null) }).collectAsState()
  val conversations by chatRepository.conversations.collectAsState()
  val stories by chatRepository.stories.collectAsState()
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ConversationCategory.ALL) }
  var showNewChatDialog by remember { mutableStateOf(false) }

  val filteredConversations = conversations.filter { conv ->
    val matchesCategory = when (selectedCategory) {
      ConversationCategory.ALL -> true
      ConversationCategory.UNREAD -> conv.unreadCount > 0
      ConversationCategory.GROUPS -> conv.isGroup
      ConversationCategory.CHANNELS -> conv.category == ConversationCategory.CHANNELS
      ConversationCategory.ARCHIVED -> conv.category == ConversationCategory.ARCHIVED
    }
    val matchesSearch = if (searchQuery.isBlank()) true else {
      conv.title.contains(searchQuery, ignoreCase = true) ||
          conv.lastMessage.contains(searchQuery, ignoreCase = true)
    }
    matchesCategory && matchesSearch
  }

  val pinnedConversations = filteredConversations.filter { it.isPinned }
  val regularConversations = filteredConversations.filter { !it.isPinned }

  Scaffold(
    topBar = {
      AppTopHeader(
        subtitle = "Chats",
        onSearchClick = {},
        onProfileClick = { onNavigateTab(Screen.Profile.route) }
      )
    },
    bottomBar = {
      AppBottomNav(
        currentRoute = Screen.Chats.route,
        onNavigate = onNavigateTab
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showNewChatDialog = true },
        containerColor = Primary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .padding(bottom = 8.dp)
          .testTag("fab_new_chat")
      ) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "New Chat",
          modifier = Modifier.size(24.dp)
        )
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Search Bar
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(20.dp)
            )
            TextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = {
                Text(
                  "Search messages, people, groups...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.outline
                )
              },
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("chat_search_input"),
              singleLine = true
            )
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Search",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
              )
            }
            IconButton(
              onClick = {},
              modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // 2. Stories Carousel (only if stories exist)
      if (stories.isNotEmpty()) {
        item {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "ACTIVE MOMENTS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
              )
              TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                Text(
                  text = "View All",
                  style = MaterialTheme.typography.labelSmall,
                  color = Primary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              contentPadding = PaddingValues(vertical = 4.dp)
            ) {
              items(stories) { story ->
                StoryItemView(
                  story = story,
                  onClick = {}
                )
              }
            }
          }
        }
      }

      // 3. Category Filter Chips (Dynamic counts)
      item {
        val totalCount = conversations.size
        val unreadCount = conversations.count { it.unreadCount > 0 }
        val groupCount = conversations.count { it.isGroup }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 2.dp)
        ) {
          item {
            FilterChipItem(
              title = "All",
              count = if (totalCount > 0) "$totalCount" else null,
              isSelected = selectedCategory == ConversationCategory.ALL,
              onClick = { selectedCategory = ConversationCategory.ALL }
            )
          }
          item {
            FilterChipItem(
              title = "Unread",
              count = if (unreadCount > 0) "$unreadCount" else null,
              isSelected = selectedCategory == ConversationCategory.UNREAD,
              onClick = { selectedCategory = ConversationCategory.UNREAD }
            )
          }
          item {
            FilterChipItem(
              title = "Groups",
              count = if (groupCount > 0) "$groupCount" else null,
              isSelected = selectedCategory == ConversationCategory.GROUPS,
              onClick = { selectedCategory = ConversationCategory.GROUPS }
            )
          }
          item {
            FilterChipItem(
              title = "Channels",
              count = null,
              isSelected = selectedCategory == ConversationCategory.CHANNELS,
              onClick = { selectedCategory = ConversationCategory.CHANNELS }
            )
          }
          item {
            FilterChipItem(
              title = "Archived",
              count = null,
              isSelected = selectedCategory == ConversationCategory.ARCHIVED,
              onClick = { selectedCategory = ConversationCategory.ARCHIVED }
            )
          }
        }
      }

      // 4. Empty State if no conversations exist
      if (filteredConversations.isEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(76.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
              text = "No conversations yet",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Add a contact or start a new conversation to get started.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { onNavigateTab(Screen.Contacts.route) },
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Contact", fontWeight = FontWeight.SemiBold)
              }
              Button(
                onClick = { showNewChatDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start New Chat", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 5. Pinned Conversations
      if (pinnedConversations.isNotEmpty()) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Pinned",
              tint = Primary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PINNED CONVERSATIONS",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.8.sp
            )
          }
        }

        items(pinnedConversations, key = { it.id }) { conv ->
          ConversationRowItem(
            conversation = conv,
            onClick = { onNavigateToConversation(conv.id) }
          )
        }
      }

      // 6. All Conversations
      if (regularConversations.isNotEmpty()) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "ALL CONVERSATIONS",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.8.sp
            )
            Text(
              text = "Real-time Encrypted",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        items(regularConversations, key = { it.id }) { conv ->
          ConversationRowItem(
            conversation = conv,
            onClick = { onNavigateToConversation(conv.id) }
          )
        }
      }

      // End of Chats Delight Note
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Encrypted",
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Your personal communications are end-to-end encrypted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    }
  }

  // Dialog to Start New Conversation
  if (showNewChatDialog) {
    var recipientInput by remember { mutableStateOf("") }
    var chatTitleInput by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showNewChatDialog = false },
      title = {
        Text("Start New Conversation", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Enter the name, email, or username of the person you want to chat with.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = recipientInput,
            onValueChange = { recipientInput = it },
            label = { Text("Recipient (Name or Email)") },
            placeholder = { Text("e.g. friend@example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = chatTitleInput,
            onValueChange = { chatTitleInput = it },
            label = { Text("Chat Title (Optional)") },
            placeholder = { Text("e.g. Project Sync") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (recipientInput.isNotBlank()) {
              scope.launch {
                val title = chatTitleInput.ifBlank { recipientInput.trim() }
                val myUid = currentUser?.uid ?: "me"
                val myName = currentUser?.displayName ?: "Me"
                val newId = chatRepository.createNewConversation(
                  title = title,
                  currentUserId = myUid,
                  currentUserName = myName,
                  recipientIdentifier = recipientInput.trim()
                )
                showNewChatDialog = false
                onNavigateToConversation(newId)
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Primary),
          enabled = recipientInput.isNotBlank()
        ) {
          Text("Start Chat")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewChatDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun StoryItemView(story: StoryMoment, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag("story_${story.userName}")
  ) {
    Box(
      modifier = Modifier
        .size(60.dp),
      contentAlignment = Alignment.Center
    ) {
      if (story.isLive) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
              Brush.sweepGradient(
                listOf(Primary, Secondary, ErrorRed, Primary)
              )
            )
            .padding(2.dp)
        ) {
          AsyncImage(
            model = story.userAvatar,
            contentDescription = story.userName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
        }
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .background(ErrorRed, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = "LIVE",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      } else if (story.isUserStory) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(2.dp)
        ) {
          AsyncImage(
            model = story.userAvatar,
            contentDescription = story.userName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(18.dp)
              .background(Primary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add Story",
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .padding(2.dp)
        ) {
          AsyncImage(
            model = story.userAvatar,
            contentDescription = story.userName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(11.dp)
              .background(SecondaryContainer, CircleShape)
              .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = story.userName,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun FilterChipItem(
  title: String,
  count: String?,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = CircleShape,
    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainer,
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag("filter_chip_$title")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (count != null) {
        Box(
          modifier = Modifier
            .background(
              if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHigh,
              CircleShape
            )
            .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
          Text(
            text = count,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Composable
fun ConversationRowItem(
  conversation: Conversation,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("conversation_${conversation.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (conversation.participantAvatars.isNotEmpty()) {
          AsyncImage(
            model = conversation.participantAvatars[0],
            contentDescription = conversation.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
        } else {
          val icon = if (conversation.isGroup) Icons.Default.Terminal else Icons.Default.Chat
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary)
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = conversation.title,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (conversation.isVerified) {
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = Primary,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            when (conversation.lastMessageType) {
              MessageType.AUDIO -> {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Audio",
                  tint = Primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = conversation.lastMessage,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium,
                  color = Primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              MessageType.IMAGE -> {
                Icon(
                  imageVector = Icons.Default.PhotoCamera,
                  contentDescription = "Photo",
                  tint = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = conversation.lastMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              else -> {
                if (conversation.lastMessageSender.isNotBlank()) {
                  Text(
                    text = "${conversation.lastMessageSender}: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Text(
                  text = conversation.lastMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          if (conversation.unreadCount > 0) {
            Box(
              modifier = Modifier
                .background(Primary, CircleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${conversation.unreadCount}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
