package com.example.ui.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.repository.ChatRepository
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.Tertiary
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
  conversationId: String,
  chatRepository: ChatRepository,
  currentUserId: String = "me",
  currentUserName: String = "Me",
  onBack: () -> Unit
) {
  val conversations by chatRepository.conversations.collectAsState()
  val currentConv = conversations.firstOrNull { it.id == conversationId }
  val chatTitle = currentConv?.title ?: "Chat"
  val isVerified = currentConv?.isVerified ?: false
  val isGroup = currentConv?.isGroup ?: false
  val participantCount = currentConv?.participants?.size ?: 2
  val statusSubtitle = if (isGroup) "$participantCount members" else "Active Now"
  val avatarUrl = currentConv?.participantAvatars?.firstOrNull() ?: ""

  val messages by chatRepository.getMessages(conversationId).collectAsState(initial = emptyList())
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  var inputText by remember { mutableStateOf("") }
  var showAttachmentDrawer by remember { mutableStateOf(false) }
  var isAudioPlaying by remember { mutableStateOf(false) }
  var playbackSpeed by remember { mutableStateOf("1.0x") }

  Scaffold(
    topBar = {
      ConversationTopBar(
        title = chatTitle,
        subtitle = statusSubtitle,
        isVerified = isVerified,
        avatarUrl = avatarUrl,
        onBack = onBack,
        onCallClick = {},
        onVideoCallClick = {}
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .imePadding()
          .navigationBarsPadding()
      ) {
        // Contextual Attachment Drawer
        AnimatedVisibility(visible = showAttachmentDrawer) {
          AttachmentDrawer(
            onSelect = { showAttachmentDrawer = false }
          )
        }

        // Message Composer Bar
        ComposerBar(
          inputText = inputText,
          onInputChanged = { inputText = it },
          isAttachmentOpen = showAttachmentDrawer,
          onToggleAttachment = { showAttachmentDrawer = !showAttachmentDrawer },
          onSend = {
            if (inputText.isNotBlank()) {
              chatRepository.sendMessage(
                conversationId = conversationId,
                text = inputText,
                currentUserId = currentUserId,
                currentUserName = currentUserName
              )
              inputText = ""
              scope.launch {
                listState.animateScrollToItem((messages.size).coerceAtLeast(0))
              }
            }
          },
          onVoiceNote = {
            chatRepository.sendVoiceNote(
              conversationId = conversationId,
              duration = "0:30",
              currentUserId = currentUserId,
              currentUserName = currentUserName
            )
          }
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (messages.isEmpty()) {
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 48.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .background(Primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Chat,
                  contentDescription = null,
                  tint = Primary,
                  modifier = Modifier.size(36.dp)
                )
              }
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No messages yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Say hello to $chatTitle! Send your first message below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
              )
            }
          }
        } else {
          // Date Separator Badge
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 1.dp
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = "Conversation History",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          items(messages, key = { it.id }) { message ->
            ConversationMessageItem(
              message = message,
              isPlayingAudio = isAudioPlaying,
              onToggleAudio = { isAudioPlaying = !isAudioPlaying },
              playbackSpeed = playbackSpeed,
              onCycleSpeed = {
                playbackSpeed = when (playbackSpeed) {
                  "1.0x" -> "1.5x"
                  "1.5x" -> "2.0x"
                  else -> "1.0x"
                }
              },
              onAddReaction = { emoji ->
                chatRepository.addReaction(conversationId, message.id, emoji)
              }
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(30.dp))
        }
      }

      // Quick Scroll-to-Bottom Floating Pill
      if (messages.size > 8) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.95f),
          shadowElevation = 6.dp,
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-16).dp)
            .clickable {
              scope.launch {
                listState.animateScrollToItem(messages.size)
              }
            }
            .testTag("scroll_to_bottom_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = SecondaryFixed,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "New messages",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.inverseOnSurface
            )
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(OnlineGreen, CircleShape)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ConversationTopBar(
  title: String,
  subtitle: String,
  isVerified: Boolean,
  avatarUrl: String,
  onBack: () -> Unit,
  onCallClick: () -> Unit,
  onVideoCallClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding(),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag("conversation_back_btn")) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      Box(modifier = Modifier.size(40.dp)) {
        if (avatarUrl.isNotBlank()) {
          AsyncImage(
            model = avatarUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(Primary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title.take(1).uppercase(),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }
        }
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(10.dp)
            .background(OnlineGreen, CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (isVerified) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = Primary,
              modifier = Modifier.size(15.dp)
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(OnlineGreen, CircleShape)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
          )
        }
      }

      IconButton(onClick = onCallClick, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Call,
          contentDescription = "Audio call",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      IconButton(onClick = onVideoCallClick, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Videocam,
          contentDescription = "Video call",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun ConversationMessageItem(
  message: Message,
  isPlayingAudio: Boolean,
  onToggleAudio: () -> Unit,
  playbackSpeed: String,
  onCycleSpeed: () -> Unit,
  onAddReaction: (String) -> Unit
) {
  val isOutgoing = message.isOutgoing

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom
  ) {
    if (!isOutgoing) {
      AsyncImage(
        model = message.senderAvatar,
        contentDescription = message.senderName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .padding(bottom = 2.dp, end = 6.dp)
          .size(28.dp)
          .clip(CircleShape)
      )
    }

    Column(
      modifier = Modifier.fillMaxWidth(if (isOutgoing) 0.85f else 0.88f),
      horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
      Surface(
        shape = if (isOutgoing) {
          RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
        } else {
          RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
        },
        color = if (isOutgoing) Primary else MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          // Quoted snippet if replying
          if (message.replyToText != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLow,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ) {
              Row(modifier = Modifier.padding(8.dp)) {
                Box(
                  modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(Primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = message.replyToAuthor ?: "You",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = message.replyToText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          // Message Content by Type
          when (message.type) {
            MessageType.AUDIO -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = onToggleAudio,
                  modifier = Modifier
                    .size(40.dp)
                    .background(Primary, CircleShape)
                ) {
                  Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play voice note",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                  // Waveform visualizer bars
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                  ) {
                    val heights = listOf(8, 14, 20, 12, 24, 16, 20, 8, 14, 24, 18, 12, 18, 8, 14, 10)
                    heights.forEachIndexed { idx, h ->
                      val isPlayed = idx < 8
                      Box(
                        modifier = Modifier
                          .width(3.dp)
                          .height(h.dp)
                          .background(
                            if (isPlayed) Primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            CircleShape
                          )
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = message.audioDuration ?: "0:42 / 1:15",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.clickable { onCycleSpeed() }
                      ) {
                        Text(
                          text = playbackSpeed,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                      }
                      Text(
                        text = "10:16 AM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }
            }

            MessageType.IMAGE -> {
              Column {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                ) {
                  AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Image Mockup",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                  if (message.mediaLabel != null) {
                    Surface(
                      color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f),
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Palette,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(13.dp)
                        )
                        Text(
                          text = message.mediaLabel,
                          color = Color.White,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = message.text,
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                )

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "10:17 AM",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    tint = SecondaryFixed,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }

            else -> {
              Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isOutgoing) "10:15 AM" else "10:14 AM",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isOutgoing) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    tint = SecondaryFixed,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Reaction pills below bubble if any
      if (message.reactions.isNotEmpty()) {
        Row(
          modifier = Modifier
            .offset(y = (-6).dp)
            .padding(start = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          message.reactions.forEach { (emoji, count) ->
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 2.dp,
              modifier = Modifier.clickable { onAddReaction(emoji) }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Text(text = emoji, fontSize = 12.sp)
                Text(
                  text = "$count",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier
              .size(22.dp)
              .clickable { onAddReaction("🔥") }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = "+", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}

@Composable
fun TypingIndicatorBubble() {
  Row(
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.padding(top = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Typing",
        tint = Primary,
        modifier = Modifier.size(15.dp)
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 1.dp
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val transition = rememberInfiniteTransition()
        val anim1 by transition.animateFloat(
          initialValue = 0f, targetValue = 1f,
          animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse)
        )
        val anim2 by transition.animateFloat(
          initialValue = 0f, targetValue = 1f,
          animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse)
        )
        val anim3 by transition.animateFloat(
          initialValue = 0f, targetValue = 1f,
          animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Reverse)
        )

        Box(
          modifier = Modifier
            .offset(y = (-3 * anim1).dp)
            .size(6.dp)
            .background(Primary, CircleShape)
        )
        Box(
          modifier = Modifier
            .offset(y = (-3 * anim2).dp)
            .size(6.dp)
            .background(Primary, CircleShape)
        )
        Box(
          modifier = Modifier
            .offset(y = (-3 * anim3).dp)
            .size(6.dp)
            .background(Primary, CircleShape)
        )
      }
    }
  }
}

@Composable
fun AttachmentDrawer(onSelect: (String) -> Unit) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 6.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      AttachmentOption(
        icon = Icons.Default.Image,
        label = "Photos",
        bgColor = SecondaryFixed,
        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = { onSelect("Photos") }
      )
      AttachmentOption(
        icon = Icons.Default.Description,
        label = "Files",
        bgColor = Primary.copy(alpha = 0.2f),
        iconColor = Primary,
        onClick = { onSelect("Files") }
      )
      AttachmentOption(
        icon = Icons.Default.LocationOn,
        label = "Location",
        bgColor = MaterialTheme.colorScheme.surfaceVariant,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onSelect("Location") }
      )
      AttachmentOption(
        icon = Icons.Default.Poll,
        label = "Poll",
        bgColor = Tertiary.copy(alpha = 0.2f),
        iconColor = Tertiary,
        onClick = { onSelect("Poll") }
      )
    }
  }
}

@Composable
fun AttachmentOption(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  bgColor: Color,
  iconColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .background(bgColor, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, style = MaterialTheme.typography.labelSmall)
  }
}

@Composable
fun ComposerBar(
  inputText: String,
  onInputChanged: (String) -> Unit,
  isAttachmentOpen: Boolean,
  onToggleAttachment: () -> Unit,
  onSend: () -> Unit,
  onVoiceNote: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    shadowElevation = 6.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onToggleAttachment,
        modifier = Modifier.testTag("composer_attach_btn")
      ) {
        Icon(
          imageVector = if (isAttachmentOpen) Icons.Default.Close else Icons.Default.AttachFile,
          contentDescription = "Attachment",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        modifier = Modifier.weight(1f),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
            Icon(
              imageVector = Icons.Default.Mood,
              contentDescription = "Emoji",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          TextField(
            value = inputText,
            onValueChange = onInputChanged,
            placeholder = {
              Text(
                "Write a message...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
              )
            },
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("message_input_field"),
            maxLines = 4
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Dynamic Mic / Send Action Button
      FloatingActionButton(
        onClick = {
          if (inputText.isNotBlank()) onSend() else onVoiceNote()
        },
        containerColor = Primary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .testTag("composer_action_btn")
      ) {
        Icon(
          imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
          contentDescription = if (inputText.isNotBlank()) "Send message" else "Record voice note",
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
