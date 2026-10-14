package com.example.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.AiMessage
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Primary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiAssistantScreen(
  aiRepository: AiAssistantRepository,
  onNavigateTab: (String) -> Unit
) {
  val messages by aiRepository.messages.collectAsState()
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current

  var inputText by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }

  // Auto scroll to bottom on new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val starterPrompts = listOf(
    "How can I organize team communication?",
    "Summarize key messaging best practices",
    "Help me draft a polite project kickoff note",
    "Explain end-to-end encryption concepts"
  )

  Scaffold(
    topBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(
                  Brush.linearGradient(listOf(Color(0xFF8E24AA), Primary)),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "AI Assistant",
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = Color(0xFF8E24AA).copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "Gemini",
                    color = Color(0xFF8E24AA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
              }
              Text(
                text = "Dedicated Smart Chat Assistant",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (messages.isNotEmpty()) {
            IconButton(
              onClick = { aiRepository.clearChatHistory() },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ClearAll,
                contentDescription = "Clear History",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    },
    bottomBar = {
      AppBottomNav(
        currentRoute = Screen.AiAssistant.route,
        onNavigate = onNavigateTab
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .imePadding()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Message list or empty state
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (messages.isEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .background(
                  Brush.linearGradient(listOf(Color(0xFF8E24AA).copy(alpha = 0.15f), Primary.copy(alpha = 0.15f))),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF8E24AA),
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Dedicated AI Assistant",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Ask questions, brainstorm ideas, draft messages, or solve problems. Your AI Assistant is completely separate from human conversations.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
              text = "Try asking:",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              starterPrompts.forEach { prompt ->
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      inputText = prompt
                    },
                  color = MaterialTheme.colorScheme.surfaceContainerLow,
                  shape = RoundedCornerShape(12.dp),
                  tonalElevation = 1.dp
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = Color(0xFF8E24AA),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = prompt,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            items(messages, key = { it.id }) { message ->
              AiMessageBubble(
                message = message,
                onCopy = { clipboardManager.setText(AnnotatedString(message.text)) }
              )
            }

            if (isLoading) {
              item {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(8.dp)
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF8E24AA)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    "AI Assistant is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Input Bar
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                "Ask AI Assistant anything...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
              )
            },
            colors = TextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("ai_input_field"),
            maxLines = 4
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              val prompt = inputText.trim()
              if (prompt.isNotBlank() && !isLoading) {
                inputText = ""
                isLoading = true
                scope.launch {
                  aiRepository.askAssistant(prompt)
                  isLoading = false
                }
              }
            },
            enabled = inputText.isNotBlank() && !isLoading,
            modifier = Modifier
              .size(44.dp)
              .background(
                if (inputText.isNotBlank() && !isLoading) Brush.linearGradient(listOf(Color(0xFF8E24AA), Primary))
                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHigh)),
                CircleShape
              )
              .testTag("ai_send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (inputText.isNotBlank() && !isLoading) Color.White else MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AiMessageBubble(
  message: AiMessage,
  onCopy: () -> Unit
) {
  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  val timeStr = timeFormat.format(Date(message.timestamp))

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
  ) {
    // Header for AI message
    if (!message.isFromUser) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = if (message.isError) ErrorRed else Color(0xFF8E24AA),
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (message.isError) "AI Assistant (Error)" else "AI Assistant",
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = if (message.isError) ErrorRed else Color(0xFF8E24AA)
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
      ),
      color = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        message.isFromUser -> Primary
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
      },
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(0.85f)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        if (message.isError) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Status Notice",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error,
              fontSize = 13.sp
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium,
          color = when {
            message.isError -> MaterialTheme.colorScheme.onErrorContainer
            message.isFromUser -> Color.White
            else -> MaterialTheme.colorScheme.onSurface
          }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!message.isFromUser && !message.isError) {
            IconButton(onClick = onCopy, modifier = Modifier.size(20.dp)) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy text",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(13.dp)
              )
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = when {
              message.isError -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
              message.isFromUser -> Color.White.copy(alpha = 0.8f)
              else -> MaterialTheme.colorScheme.outline
            },
            fontSize = 10.sp
          )
        }
      }
    }
  }
}
