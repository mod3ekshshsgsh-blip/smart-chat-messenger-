package com.example.ui.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Community
import com.example.data.model.FriendRequest
import com.example.data.model.SuggestedFriend
import com.example.data.repository.FriendRepository
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.TertiaryContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
  friendRepository: FriendRepository,
  onNavigateTab: (String) -> Unit
) {
  val requests by friendRepository.pendingRequests.collectAsState()
  val communities by friendRepository.communities.collectAsState()

  var selectedTab by remember { mutableStateOf("discover") }
  var searchQuery by remember { mutableStateOf("") }
  var searchResults by remember { mutableStateOf<List<SuggestedFriend>>(emptyList()) }
  var isSearching by remember { mutableStateOf(false) }
  var toastMessage by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()

  fun triggerToast(msg: String) {
    scope.launch {
      toastMessage = msg
      delay(2400)
      toastMessage = null
    }
  }

  LaunchedEffect(searchQuery) {
    if (searchQuery.trim().length >= 2) {
      isSearching = true
      searchResults = friendRepository.searchUsers(searchQuery, currentUid = "")
      isSearching = false
    } else {
      searchResults = emptyList()
    }
  }

  Scaffold(
    topBar = {
      AppTopHeader(
        subtitle = "Discover",
        onProfileClick = { onNavigateTab(Screen.Profile.route) }
      )
    },
    bottomBar = {
      AppBottomNav(
        currentRoute = Screen.Discover.route,
        onNavigate = onNavigateTab
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Hero Highlight: Network Pulse Banner
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 3.dp
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    listOf(Primary, SecondaryContainer, TertiaryContainer)
                  )
                )
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = SecondaryFixed,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "NETWORK PULSE",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = SecondaryFixed,
                      letterSpacing = 1.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Expand your circle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = if (requests.isNotEmpty()) "${requests.size} invitations awaiting your response" else "Find friends and connect with collaborators",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                  )
                }

                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                  )
                }
              }
            }
          }
        }

        // 2. Segmented Sub-navigation Tabs
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              SubNavTabItem(
                label = "Discover",
                badge = null,
                isSelected = selectedTab == "discover",
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = "discover" }
              )
              SubNavTabItem(
                label = "Requests",
                badge = if (requests.isNotEmpty()) "${requests.size}" else null,
                isSelected = selectedTab == "requests",
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = "requests" }
              )
              SubNavTabItem(
                label = "Mutual",
                badge = null,
                isSelected = selectedTab == "mutual",
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = "mutual" }
              )
            }
          }
        }

        // 3. Search Bar
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
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
                    "Search by @username or name...",
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
                  .testTag("user_search_input"),
                singleLine = true
              )
              if (searchQuery.isNotBlank()) {
                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.outline
                  )
                }
              }
            }
          }
        }

        // Live Search Results
        if (searchQuery.isNotBlank()) {
          item {
            Text(
              text = "SEARCH RESULTS",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.8.sp
            )
          }

          if (isSearching) {
            item {
              Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
              }
            }
          } else if (searchResults.isEmpty()) {
            item {
              Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(imageVector = Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No users found matching \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          } else {
            items(searchResults, key = { it.id }) { friend ->
              SuggestedFriendCardItem(
                friend = friend,
                onToggleAdd = {
                  friendRepository.toggleAddFriend(friend.id)
                  triggerToast("Friend request sent to ${friend.name}")
                }
              )
            }
          }
        }

        // 4. Pending Requests Section
        if (selectedTab == "requests" || (selectedTab == "discover" && searchQuery.isBlank())) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Pending Requests",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
              if (requests.isNotEmpty()) {
                Box(
                  modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${requests.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                  )
                }
              }
            }
          }

          if (requests.isEmpty()) {
            item {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "No pending requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "When someone sends you a friend request, you'll see it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          } else {
            items(requests, key = { it.id }) { req ->
              RequestCardItem(
                request = req,
                onAccept = {
                  friendRepository.acceptRequest(req.id)
                  triggerToast("Connected with ${req.fromName}! 🎉")
                },
                onDecline = {
                  friendRepository.declineRequest(req.id)
                  triggerToast("Request from ${req.fromName} declined")
                }
              )
            }
          }
        }

        // 5. Suggested Friends / Find Users Section
        if (searchQuery.isBlank() && (selectedTab == "discover" || selectedTab == "mutual")) {
          item {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 1.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier.size(56.dp).background(Primary.copy(alpha = 0.12f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.PersonSearch, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                  text = "Find People to Chat With",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Use the search bar above to search for people by @username or email address and connect directly.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        // 6. Explore Communities
        if (selectedTab == "discover" && searchQuery.isBlank() && communities.isNotEmpty()) {
          item {
            Text(
              text = "Communities",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )
          }

          items(communities, key = { it.id }) { community ->
            CommunityCardItem(
              community = community,
              onToggleJoin = {
                friendRepository.toggleCommunityJoin(community.id)
                triggerToast(if (!community.isJoined) "Joined ${community.name}" else "Left ${community.name}")
              }
            )
          }
        }
      }

      // Toast feedback pill
      AnimatedVisibility(
        visible = toastMessage != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 16.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.inverseSurface,
          shadowElevation = 6.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = SecondaryFixed,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = toastMessage ?: "",
              color = MaterialTheme.colorScheme.inverseOnSurface,
              style = MaterialTheme.typography.labelMedium
            )
          }
        }
      }
    }
  }
}

@Composable
fun SubNavTabItem(
  label: String,
  badge: String?,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
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
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (badge != null) {
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .background(Primary, CircleShape)
            .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
          Text(
            text = badge,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun RequestCardItem(
  request: FriendRequest,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth().testTag("request_card_${request.fromUsername}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(modifier = Modifier.weight(1f)) {
          Box(modifier = Modifier.size(48.dp)) {
            if (request.fromAvatar.isNotBlank()) {
              AsyncImage(
                model = request.fromAvatar,
                contentDescription = request.fromName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
              )
            } else {
              Box(
                modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Primary)
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = request.fromName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = request.fromUsername,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onAccept,
          colors = ButtonDefaults.buttonColors(containerColor = Primary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).height(40.dp)
        ) {
          Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Accept", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onDecline,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          ),
          modifier = Modifier.weight(1f).height(40.dp)
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Decline", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

@Composable
fun SuggestedFriendCardItem(
  friend: SuggestedFriend,
  onToggleAdd: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth().testTag("suggested_${friend.username}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(48.dp)) {
            if (friend.avatar.isNotBlank()) {
              AsyncImage(
                model = friend.avatar,
                contentDescription = friend.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
              )
            } else {
              Box(
                modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = Primary)
              }
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = friend.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${friend.username} • ${friend.role}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        Button(
          onClick = onToggleAdd,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (friend.isAdded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (friend.isAdded) MaterialTheme.colorScheme.onSurfaceVariant else Primary
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Icon(
            imageVector = if (friend.isAdded) Icons.Default.Check else Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (friend.isAdded) "Sent" else "Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun CommunityCardItem(
  community: Community,
  onToggleJoin: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth().testTag("community_${community.name}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Primary),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Default.Devices, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = community.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = community.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Button(
        onClick = onToggleJoin,
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(if (community.isJoined) "Joined" else "Join", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}
