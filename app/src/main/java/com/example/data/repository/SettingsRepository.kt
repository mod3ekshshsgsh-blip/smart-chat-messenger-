package com.example.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentIndigo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
  private val prefs = context.getSharedPreferences("smart_chat_settings_prefs", Context.MODE_PRIVATE)

  private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  private val _accentColor = MutableStateFlow(AccentIndigo)
  val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

  fun setDarkTheme(enabled: Boolean) {
    prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    _isDarkTheme.value = enabled
  }

  fun setAccentColor(color: Color) {
    _accentColor.value = color
  }
}
