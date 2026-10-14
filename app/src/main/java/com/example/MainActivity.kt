package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ContactRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.SmartChatTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val authRepository = remember { AuthRepository(applicationContext) }
      val chatRepository = remember { ChatRepository() }
      val contactRepository = remember { ContactRepository(applicationContext) }
      val aiAssistantRepository = remember { AiAssistantRepository(applicationContext) }
      val settingsRepository = remember { SettingsRepository(applicationContext) }

      val isDarkTheme by settingsRepository.isDarkTheme.collectAsState()

      SmartChatTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation(
            authRepository = authRepository,
            chatRepository = chatRepository,
            contactRepository = contactRepository,
            aiAssistantRepository = aiAssistantRepository,
            settingsRepository = settingsRepository
          )
        }
      }
    }
  }
}
