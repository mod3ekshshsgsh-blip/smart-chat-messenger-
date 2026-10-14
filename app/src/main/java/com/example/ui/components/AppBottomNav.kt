package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.navigation.Screen
import com.example.ui.theme.Primary

@Composable
fun AppBottomNav(
  currentRoute: String?,
  onNavigate: (String) -> Unit,
  userAvatar: String = ""
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .height(68.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      BottomNavItem(
        label = "Chats",
        icon = Icons.Default.ChatBubble,
        isSelected = currentRoute == Screen.Chats.route,
        onClick = { onNavigate(Screen.Chats.route) },
        testTag = "nav_chats"
      )

      BottomNavItem(
        label = "Contacts",
        icon = Icons.Default.People,
        isSelected = currentRoute == Screen.Contacts.route || currentRoute == "discover",
        onClick = { onNavigate(Screen.Contacts.route) },
        testTag = "nav_contacts"
      )

      BottomNavItem(
        label = "AI Assistant",
        icon = Icons.Default.AutoAwesome,
        isSelected = currentRoute == Screen.AiAssistant.route || currentRoute == "alerts",
        onClick = { onNavigate(Screen.AiAssistant.route) },
        testTag = "nav_ai_assistant"
      )

      // Profile item
      val isProfileSelected = currentRoute == Screen.Profile.route
      Column(
        modifier = Modifier
          .clip(CircleShape)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onNavigate(Screen.Profile.route) }
          .padding(vertical = 4.dp, horizontal = 12.dp)
          .testTag("nav_profile"),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.size(26.dp),
          contentAlignment = Alignment.Center
        ) {
          if (userAvatar.isNotBlank()) {
            AsyncImage(
              model = userAvatar,
              contentDescription = "Profile",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
            )
          } else {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Profile",
              modifier = Modifier.size(24.dp),
              tint = if (isProfileSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Text(
          text = "Profile",
          fontSize = 11.sp,
          fontWeight = if (isProfileSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isProfileSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }
  }
}

@Composable
private fun BottomNavItem(
  label: String,
  icon: ImageVector,
  badge: String? = null,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Column(
    modifier = Modifier
      .clip(CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(vertical = 4.dp, horizontal = 12.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(28.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        modifier = Modifier.size(24.dp),
        tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (badge != null) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 6.dp, y = (-2).dp)
            .background(Primary, CircleShape)
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = badge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}
