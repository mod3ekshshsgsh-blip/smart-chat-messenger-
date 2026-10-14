package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.Primary

@Composable
fun AppTopHeader(
  subtitle: String,
  userAvatar: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuC5JvrM9f_RYmyHoZ7Lop1kdG3bxlFkQCCJaVXslsC3__6q5MnMY48Q4zzY6pQEEypdaa_ta7pADTlWCXRB3VkiLrK-Js2LMA7b3DCx2qfhBNZkIEMjvHiOiU69r8xxz6PiMZ6lJ0ejzYrlx71hNeK88gP6694p46DcI3AnJBdRSUHS4Cx9CSFqCxXG9uEVBpAfuiaPqs7HyfqMp5RR0SToIUW-mbRp8XWmlk04BuSmJ_Dq4qO-kom0",
  onSearchClick: () -> Unit = {},
  onCameraClick: () -> Unit = {},
  onProfileClick: () -> Unit = {}
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .height(64.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // App Logo badge
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(Primary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "Smart Chat Logo",
          tint = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Smart Chat",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.testTag("header_search_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onCameraClick,
        modifier = Modifier.testTag("header_camera_btn")
      ) {
        Icon(
          imageVector = Icons.Default.PhotoCamera,
          contentDescription = "Camera",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .testTag("header_avatar_btn")
      ) {
        AsyncImage(
          model = userAvatar,
          contentDescription = "Profile",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(9.dp)
            .background(OnlineGreen, CircleShape)
        )
      }
    }
  }
}
