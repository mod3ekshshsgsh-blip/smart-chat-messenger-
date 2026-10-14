package com.example.ui.contacts

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Contact
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ContactRepository
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
  contactRepository: ContactRepository,
  chatRepository: ChatRepository,
  authRepository: AuthRepository,
  onNavigateToConversation: (String) -> Unit,
  onNavigateTab: (String) -> Unit
) {
  val currentUser by authRepository.currentUser.collectAsState()
  val contacts by contactRepository.contacts.collectAsState()
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }
  var contactToEdit by remember { mutableStateOf<Contact?>(null) }
  var contactToDelete by remember { mutableStateOf<Contact?>(null) }
  var selectedContactProfile by remember { mutableStateOf<Contact?>(null) }
  var notRegisteredNoticeContact by remember { mutableStateOf<Contact?>(null) }

  val filteredContacts = contacts.filter { contact ->
    if (searchQuery.isBlank()) true else {
      contact.name.contains(searchQuery, ignoreCase = true) ||
          contact.identifier.contains(searchQuery, ignoreCase = true)
    }
  }

  Scaffold(
    topBar = {
      AppTopHeader(
        subtitle = "Contacts",
        onSearchClick = {},
        onProfileClick = { onNavigateTab(Screen.Profile.route) }
      )
    },
    bottomBar = {
      AppBottomNav(
        currentRoute = Screen.Contacts.route,
        onNavigate = onNavigateTab
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = Primary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .padding(bottom = 8.dp)
          .testTag("fab_add_contact")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Contact",
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
      verticalArrangement = Arrangement.spacedBy(12.dp)
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
                  "Search saved contacts...",
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
                .testTag("contact_search_input"),
              singleLine = true
            )
          }
        }
      }

      // 2. Summary Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SAVED CONTACTS (${filteredContacts.size})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
          )
          TextButton(onClick = { showAddDialog = true }) {
            Text("+ Add Contact", color = Primary, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // 3. Empty State if no contacts
      if (filteredContacts.isEmpty()) {
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
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(38.dp)
              )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
              text = "No contacts yet",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Add contacts using their email, phone number, or username to start chatting.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Primary),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Contact", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // 4. Contact List
      items(filteredContacts, key = { it.id }) { contact ->
        ContactCardItem(
          contact = contact,
          onCardClick = { selectedContactProfile = contact },
          onStartChat = {
            if (contact.isRegisteredUser && !contact.registeredUid.isNullOrBlank()) {
              scope.launch {
                val myUid = currentUser?.uid ?: "me"
                val myName = currentUser?.displayName ?: "Me"
                val convId = chatRepository.createNewConversation(
                  title = contact.name,
                  currentUserId = myUid,
                  currentUserName = myName,
                  recipientIdentifier = contact.registeredUid ?: contact.identifier
                )
                onNavigateToConversation(convId)
              }
            } else {
              notRegisteredNoticeContact = contact
            }
          },
          onEdit = { contactToEdit = contact },
          onDelete = { contactToDelete = contact }
        )
      }
    }
  }

  // Add Contact Dialog
  if (showAddDialog) {
    ContactFormDialog(
      title = "Add Contact",
      contact = null,
      onDismiss = { showAddDialog = false },
      onSave = { name, identifier, type, notes ->
        scope.launch {
          contactRepository.addContact(
            name = name,
            identifier = identifier,
            identifierType = type,
            notes = notes,
            currentUid = currentUser?.uid
          )
          showAddDialog = false
        }
      }
    )
  }

  // Edit Contact Dialog
  contactToEdit?.let { contact ->
    ContactFormDialog(
      title = "Edit Contact",
      contact = contact,
      onDismiss = { contactToEdit = null },
      onSave = { name, identifier, type, notes ->
        scope.launch {
          contactRepository.updateContact(
            contact = contact.copy(
              name = name,
              identifier = identifier,
              identifierType = type,
              notes = notes
            ),
            currentUid = currentUser?.uid
          )
          contactToEdit = null
        }
      }
    )
  }

  // Delete Contact Confirmation
  contactToDelete?.let { contact ->
    AlertDialog(
      onDismissRequest = { contactToDelete = null },
      title = { Text("Delete Contact") },
      text = { Text("Are you sure you want to remove ${contact.name} from your contacts? This cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              contactRepository.deleteContact(contact.id, currentUser?.uid)
              contactToDelete = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { contactToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // View Profile Dialog
  selectedContactProfile?.let { contact ->
    AlertDialog(
      onDismissRequest = { selectedContactProfile = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            if (!contact.photoUrl.isNullOrBlank()) {
              AsyncImage(
                model = contact.photoUrl,
                contentDescription = contact.name,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Text(
                text = contact.name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Primary
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
              text = if (contact.isRegisteredUser) "Verified Smart Chat User" else "Local Contact",
              color = if (contact.isRegisteredUser) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("${contact.identifierType}: ${contact.identifier}", fontSize = 14.sp)
          if (contact.notes.isNotBlank()) {
            Text("Notes: ${contact.notes}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          if (contact.isRegisteredUser) {
            Surface(
              color = OnlineGreen.copy(alpha = 0.1f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("This contact is registered on the messaging service.", fontSize = 12.sp, color = OnlineGreen)
              }
            }
          } else {
            Surface(
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Contact is saved locally. Not registered on Smart Chat yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      },
      confirmButton = {
        if (contact.isRegisteredUser) {
          Button(
            onClick = {
              val contactToChat = contact
              selectedContactProfile = null
              scope.launch {
                val myUid = currentUser?.uid ?: "me"
                val myName = currentUser?.displayName ?: "Me"
                val convId = chatRepository.createNewConversation(
                  title = contactToChat.name,
                  currentUserId = myUid,
                  currentUserName = myName,
                  recipientIdentifier = contactToChat.registeredUid ?: contactToChat.identifier
                )
                onNavigateToConversation(convId)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
          ) {
            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start Chat")
          }
        } else {
          TextButton(onClick = { selectedContactProfile = null }) {
            Text("Close")
          }
        }
      },
      dismissButton = {
        if (contact.isRegisteredUser) {
          TextButton(onClick = { selectedContactProfile = null }) {
            Text("Close")
          }
        }
      }
    )
  }

  // Not Registered Notice Dialog
  notRegisteredNoticeContact?.let { contact ->
    AlertDialog(
      onDismissRequest = { notRegisteredNoticeContact = null },
      title = { Text("Contact Not Registered") },
      text = {
        Text("${contact.name} (${contact.identifier}) has not registered an account on Smart Chat yet. Messages can only be delivered to registered users.")
      },
      confirmButton = {
        Button(
          onClick = { notRegisteredNoticeContact = null },
          colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
          Text("Got It")
        }
      }
    )
  }
}

@Composable
private fun ContactCardItem(
  contact: Contact,
  onCardClick: () -> Unit,
  onStartChat: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onCardClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        if (!contact.photoUrl.isNullOrBlank()) {
          AsyncImage(
            model = contact.photoUrl,
            contentDescription = contact.name,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Text(
            text = contact.name.take(1).uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Primary
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = contact.name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (contact.isRegisteredUser) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = OnlineGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "Registered",
                color = OnlineGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }
        Text(
          text = contact.identifier,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Actions
      IconButton(onClick = onStartChat, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "Chat",
          tint = if (contact.isRegisteredUser) Primary else MaterialTheme.colorScheme.outline,
          modifier = Modifier.size(20.dp)
        )
      }
      IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Edit",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun ContactFormDialog(
  title: String,
  contact: Contact?,
  onDismiss: () -> Unit,
  onSave: (name: String, identifier: String, type: String, notes: String) -> Unit
) {
  var name by remember { mutableStateOf(contact?.name ?: "") }
  var identifier by remember { mutableStateOf(contact?.identifier ?: "") }
  var identifierType by remember { mutableStateOf(contact?.identifierType ?: "EMAIL") }
  var notes by remember { mutableStateOf(contact?.notes ?: "") }
  var isSaving by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Contact Name *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text("Identifier Type:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf("EMAIL" to "Email", "USERNAME" to "Username", "PHONE" to "Phone").forEach { (type, label) ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { identifierType = type }
            ) {
              RadioButton(
                selected = identifierType == type,
                onClick = { identifierType = type }
              )
              Text(label, fontSize = 13.sp)
            }
          }
        }

        OutlinedTextField(
          value = identifier,
          onValueChange = { identifier = it },
          label = {
            Text(
              when (identifierType) {
                "EMAIL" -> "Email Address *"
                "PHONE" -> "Phone Number *"
                else -> "Username (e.g. @alex) *"
              }
            )
          },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (Optional)") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && identifier.isNotBlank()) {
            isSaving = true
            onSave(name, identifier, identifierType, notes)
          }
        },
        enabled = name.isNotBlank() && identifier.isNotBlank() && !isSaving,
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
      ) {
        if (isSaving) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
          Text("Save")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
