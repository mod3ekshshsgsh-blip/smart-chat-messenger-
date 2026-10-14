package com.example.ui.navigation

sealed class Screen(val route: String) {
  data object Auth : Screen("auth")
  data object Chats : Screen("chats")
  data object Contacts : Screen("contacts")
  data object AiAssistant : Screen("ai_assistant")
  data object Profile : Screen("profile")

  // Backward compatibility aliases
  data object Discover : Screen("contacts")
  data object Alerts : Screen("ai_assistant")

  data object Conversation : Screen("conversation/{conversationId}") {
    fun createRoute(conversationId: String) = "conversation/$conversationId"
  }
}
