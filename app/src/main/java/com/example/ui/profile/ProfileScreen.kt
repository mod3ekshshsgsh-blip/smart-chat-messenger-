package com.example.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryContainer

@Composable
fun ProfileScreen(
  authRepository: AuthRepository,
  settingsRepository: SettingsRepository,
  onNavigateTab: (String) -> Unit,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val currentUser by authRepository.currentUser.collectAsState()
  val user = currentUser ?: User(displayName = "Profile", email = "", statusText = "Available")

  val isDarkTheme by settingsRepository.isDarkTheme.collectAsState()
  var showQrDialog by remember { mutableStateOf(false) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var editDisplayName by remember { mutableStateOf(user.displayName) }
  var editBio by remember { mutableStateOf(user.bio) }
  var isCopied by remember { mutableStateOf(false) }

  val statuses = listOf("Available", "In a Meeting", "Focus Mode ☕️", "Away")

  Scaffold(
    topBar = {
      AppTopHeader(
        subtitle = "Profile",
        onProfileClick = {}
      )
    },
    bottomBar = {
      AppBottomNav(
        currentRoute = Screen.Profile.route,
        onNavigate = onNavigateTab
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // 1. Profile Visual Banner & Glassmorphic Backdrop
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
              Brush.linearGradient(
                listOf(Primary, Secondary, TertiaryContainer)
              )
            )
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = Color.White.copy(alpha = 0.2f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF34D399), CircleShape)
                )
                Text(
                  text = "Active Now",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            IconButton(
              onClick = { showQrDialog = true },
              modifier = Modifier
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .testTag("share_qr_btn")
            ) {
              Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = "Share QR Code",
                tint = Color.White
              )
            }
          }
        }
      }

      // 2. Profile Card Float Over Banner
      item {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 3.dp,
          modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-45).dp)
            .padding(horizontal = 16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Avatar with camera button
            Box(
              modifier = Modifier
                .offset(y = (-36).dp)
                .size(90.dp)
            ) {
              AsyncImage(
                model = user.photoUrl,
                contentDescription = user.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
                  .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
              )

              // Camera button
              Box(
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .size(28.dp)
                  .background(Primary, CircleShape)
                  .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PhotoCamera,
                  contentDescription = "Change photo",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }

              // Green status pip
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(4.dp)
                  .size(12.dp)
                  .background(OnlineGreen, CircleShape)
                  .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
              )
            }

            Column(
              modifier = Modifier.offset(y = (-20).dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = user.displayName,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified",
                  tint = Secondary,
                  modifier = Modifier.size(18.dp)
                )
              }

              Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(6.dp))

              // Copyable Member ID Pill
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.clickable {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Member ID", user.memberId)
                  clipboard.setPrimaryClip(clip)
                  isCopied = true
                }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = if (isCopied) "Copied to Clipboard!" else "ID: ${user.memberId}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                  )
                  Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(13.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = user.bio,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp)
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Primary Profile Actions: Edit Profile & Set Status
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = {
                    editDisplayName = user.displayName
                    editBio = user.bio
                    showEditProfileDialog = true
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Primary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("edit_profile_btn")
                ) {
                  Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Edit Profile", fontWeight = FontWeight.Bold)
                }

                Button(
                  onClick = {
                    val currentIndex = statuses.indexOf(user.statusText)
                    val nextIndex = (currentIndex + 1) % statuses.size
                    authRepository.updateStatus(statuses[nextIndex])
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                  ),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("set_status_btn")
                ) {
                  Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(user.statusText, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
              }
            }
          }
        }
      }

      // Settings Categories
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-25).dp)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Card 1: Account & Security
          SettingsCategoryGroup(title = "ACCOUNT & SECURITY", trailingBadge = "Synced") {
            SettingsItemRow(
              icon = Icons.Default.Badge,
              title = "Account Information",
              subtitle = "${user.email} • +1 (555) 234-5678",
              badge = "Verified",
              badgeColor = Secondary
            )
            SettingsDivider()
            SettingsItemRow(
              icon = Icons.Default.Lock,
              title = "Privacy & Security",
              subtitle = "Last seen: My Contacts • Online: Everyone",
              badge = "E2EE On",
              badgeColor = OnlineGreen
            )
            SettingsDivider()
            // Active Devices Strip
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Devices,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Active on iPhone 15 Pro & MacBook Air",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Text(
                text = "2 Active",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
              )
            }
          }

          // Card 2: App Experience
          SettingsCategoryGroup(title = "APP EXPERIENCE") {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(text = "Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(text = "Theme & Chat accents", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Theme Selector Toggle Buttons
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  ThemeChoiceButton(
                    label = "Light Tone",
                    icon = Icons.Default.LightMode,
                    iconTint = Color(0xFFF59E0B),
                    isSelected = !isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { settingsRepository.setDarkTheme(false) }
                  )
                  ThemeChoiceButton(
                    label = "Dark Indigo",
                    icon = Icons.Default.DarkMode,
                    iconTint = Color(0xFF818CF8),
                    isSelected = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { settingsRepository.setDarkTheme(true) }
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Bubble Palette Selector
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Bubble Palette",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  BubblePaletteDot(AccentIndigo, onClick = { settingsRepository.setAccentColor(AccentIndigo) })
                  BubblePaletteDot(AccentBlue, onClick = { settingsRepository.setAccentColor(AccentBlue) })
                  BubblePaletteDot(AccentViolet, onClick = { settingsRepository.setAccentColor(AccentViolet) })
                  BubblePaletteDot(AccentEmerald, onClick = { settingsRepository.setAccentColor(AccentEmerald) })
                }
              }
            }

            SettingsDivider()

            SettingsItemRow(
              icon = Icons.Default.NotificationsActive,
              title = "Notifications & Sounds",
              subtitle = "DMs, Mentions, Haptic vibration",
              hasDot = true
            )

            SettingsDivider()

            // Data & Storage Item
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(text = "Data & Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(text = "1.2 GB", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }

              // Storage colored bar
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape)
              ) {
                Box(modifier = Modifier.weight(0.65f).background(Primary))
                Box(modifier = Modifier.weight(0.20f).background(SecondaryContainer))
                Box(modifier = Modifier.weight(0.15f).background(Tertiary))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Wi-Fi auto-download: Media & Docs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Card 3: Support & System
          SettingsCategoryGroup(title = "SUPPORT & SYSTEM") {
            SettingsItemRow(
              icon = Icons.Default.Help,
              title = "Help Center & FAQ",
              subtitle = "Guides, safety tips & live support"
            )
            SettingsDivider()
            SettingsItemRow(
              icon = Icons.Default.Info,
              title = "About Smart Chat",
              subtitle = "Version 3.2.0 • Build 842",
              badge = "Latest",
              badgeColor = MaterialTheme.colorScheme.outline
            )
            SettingsDivider()
            // Logout Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  authRepository.logout()
                  onLogout()
                }
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = ErrorRed, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Log Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ErrorRed)
                Text(text = "Disconnect this account on current device", style = MaterialTheme.typography.bodySmall, color = ErrorRed.copy(alpha = 0.8f))
              }
              Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ErrorRed.copy(alpha = 0.6f))
            }
          }

          // Micro privacy footnote
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("End-to-End Encrypted Messenger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Smart Chat Messenger Inc. • Terms & Privacy", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
          }
        }
      }
    }
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = editDisplayName,
            onValueChange = { editDisplayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            authRepository.updateProfile(editDisplayName, editBio)
            showEditProfileDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // QR Code Modal Dialog
  if (showQrDialog) {
    AlertDialog(
      onDismissRequest = { showQrDialog = false },
      title = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text("${user.displayName}'s QR Code", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Text("Scan to start a secure direct conversation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.size(180.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .background(Color.White, RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Box(modifier = Modifier.size(36.dp).background(Primary, RoundedCornerShape(4.dp)))
                  Box(modifier = Modifier.size(36.dp).background(Primary, RoundedCornerShape(4.dp)))
                }
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .background(SecondaryContainer, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Box(modifier = Modifier.size(36.dp).background(Primary, RoundedCornerShape(4.dp)))
                  Box(modifier = Modifier.size(24.dp).background(TertiaryContainer, RoundedCornerShape(4.dp)))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showQrDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = Primary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Done")
        }
      }
    )
  }
}

@Composable
fun SettingsCategoryGroup(
  title: String,
  trailingBadge: String? = null,
  content: @Composable () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp
      )
      if (trailingBadge != null) {
        Text(
          text = trailingBadge,
          style = MaterialTheme.typography.labelSmall,
          color = Secondary,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        content()
      }
    }
  }
}

@Composable
fun SettingsItemRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  badge: String? = null,
  badgeColor: Color = Primary,
  hasDot: Boolean = false,
  onClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (badge != null) {
          Surface(
            shape = CircleShape,
            color = badgeColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = badge,
              color = badgeColor,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
        if (hasDot) {
          Box(modifier = Modifier.size(8.dp).background(Primary, CircleShape))
        }
      }
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Spacer(modifier = Modifier.width(8.dp))
    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
  }
}

@Composable
fun SettingsDivider() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
  )
}

@Composable
fun ThemeChoiceButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
    shadowElevation = if (isSelected) 2.dp else 0.dp,
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun BubblePaletteDot(color: Color, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .size(22.dp)
      .clip(CircleShape)
      .background(color)
      .clickable(onClick = onClick)
  )
}
