package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ContactRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.chats.ChatsScreen
import com.example.ui.contacts.ContactsScreen
import com.example.ui.conversation.ConversationScreen
import com.example.ui.profile.ProfileScreen

@Composable
fun AppNavigation(
  authRepository: AuthRepository,
  chatRepository: ChatRepository,
  contactRepository: ContactRepository,
  aiAssistantRepository: AiAssistantRepository,
  settingsRepository: SettingsRepository
) {
  val navController = rememberNavController()
  val currentUser by authRepository.currentUser.collectAsState()

  // Real-time sync conversations and contacts for logged-in user
  LaunchedEffect(currentUser) {
    val uid = currentUser?.uid
    if (!uid.isNullOrBlank()) {
      chatRepository.attachUserConversations(uid)
      contactRepository.attachUserContacts(uid)
    }
  }

  val startDestination = if (currentUser != null) Screen.Chats.route else Screen.Auth.route

  NavHost(
    navController = navController,
    startDestination = startDestination
  ) {
    composable(Screen.Auth.route) {
      AuthScreen(
        authRepository = authRepository,
        onAuthSuccess = {
          navController.navigate(Screen.Chats.route) {
            popUpTo(Screen.Auth.route) { inclusive = true }
          }
        }
      )
    }

    composable(Screen.Chats.route) {
      ChatsScreen(
        chatRepository = chatRepository,
        authRepository = authRepository,
        onNavigateToConversation = { convId ->
          navController.navigate(Screen.Conversation.createRoute(convId))
        },
        onNavigateTab = { route ->
          navController.navigate(route) {
            popUpTo(Screen.Chats.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }

    composable(Screen.Contacts.route) {
      ContactsScreen(
        contactRepository = contactRepository,
        chatRepository = chatRepository,
        authRepository = authRepository,
        onNavigateToConversation = { convId ->
          navController.navigate(Screen.Conversation.createRoute(convId))
        },
        onNavigateTab = { route ->
          navController.navigate(route) {
            popUpTo(Screen.Chats.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }

    composable(Screen.AiAssistant.route) {
      AiAssistantScreen(
        aiRepository = aiAssistantRepository,
        onNavigateTab = { route ->
          navController.navigate(route) {
            popUpTo(Screen.Chats.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }

    composable(Screen.Profile.route) {
      ProfileScreen(
        authRepository = authRepository,
        settingsRepository = settingsRepository,
        onNavigateTab = { route ->
          navController.navigate(route) {
            popUpTo(Screen.Chats.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        },
        onLogout = {
          chatRepository.clearConversations()
          contactRepository.clearContacts()
          navController.navigate(Screen.Auth.route) {
            popUpTo(0) { inclusive = true }
          }
        }
      )
    }

    composable(
      route = Screen.Conversation.route,
      arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
    ) { backStackEntry ->
      val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
      ConversationScreen(
        conversationId = conversationId,
        chatRepository = chatRepository,
        currentUserId = currentUser?.uid ?: "me",
        currentUserName = currentUser?.displayName ?: "Me",
        onBack = { navController.popBackStack() }
      )
    }
  }
}
