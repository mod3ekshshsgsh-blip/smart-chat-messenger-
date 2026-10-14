package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class AiMessage(
  val id: String = UUID.randomUUID().toString(),
  val text: String,
  val isFromUser: Boolean,
  val timestamp: Long = System.currentTimeMillis(),
  val isError: Boolean = false
)

class AiAssistantRepository(context: Context? = null) {

  private val prefs = context?.getSharedPreferences("smart_chat_ai_assistant", Context.MODE_PRIVATE)
  private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
  val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()

  init {
    loadLocalMessages()
  }

  private fun loadLocalMessages() {
    if (prefs == null) return
    try {
      val jsonStr = prefs.getString("ai_chat_history", "[]") ?: "[]"
      val array = JSONArray(jsonStr)
      val list = mutableListOf<AiMessage>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          AiMessage(
            id = obj.optString("id", UUID.randomUUID().toString()),
            text = obj.optString("text"),
            isFromUser = obj.optBoolean("isFromUser"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            isError = obj.optBoolean("isError", false)
          )
        )
      }
      _messages.value = list
    } catch (_: Exception) {}
  }

  private fun saveLocalMessages(list: List<AiMessage>) {
    if (prefs == null) return
    try {
      val array = JSONArray()
      list.forEach { m ->
        val obj = JSONObject().apply {
          put("id", m.id)
          put("text", m.text)
          put("isFromUser", m.isFromUser)
          put("timestamp", m.timestamp)
          put("isError", m.isError)
        }
        array.put(obj)
      }
      prefs.edit().putString("ai_chat_history", array.toString()).apply()
    } catch (_: Exception) {}
  }

  suspend fun askAssistant(prompt: String): Result<String> = withContext(Dispatchers.IO) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Prompt cannot be empty"))
    }

    // 1. Add user message
    val userMsg = AiMessage(
      text = cleanPrompt,
      isFromUser = true
    )
    val updatedWithUser = _messages.value + userMsg
    _messages.value = updatedWithUser
    saveLocalMessages(updatedWithUser)

    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
      val errorMsg = AiMessage(
        text = "The AI Assistant requires a valid GEMINI_API_KEY configured in the AI Studio Secrets panel. Please configure your Gemini API Key to enable AI responses.",
        isFromUser = false,
        isError = true
      )
      val updatedWithError = _messages.value + errorMsg
      _messages.value = updatedWithError
      saveLocalMessages(updatedWithError)
      return@withContext Result.failure(IllegalStateException(errorMsg.text))
    }

    try {
      // Build request body for gemini-3.5-flash as mandated in gemini-api skill
      val contentsArray = JSONArray()
      // Send last 6 messages as context
      val history = updatedWithUser.takeLast(6)
      for (msg in history) {
        val role = if (msg.isFromUser) "user" else "model"
        val partsArray = JSONArray().put(JSONObject().put("text", msg.text))
        contentsArray.put(JSONObject().apply {
          put("role", role)
          put("parts", partsArray)
        })
      }

      val requestJson = JSONObject().apply {
        put("contents", contentsArray)
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().put("text", "You are the dedicated Smart Chat AI Assistant. You assist the user politely, accurately, and concisely. Clearly identify yourself as an AI assistant.")))
        })
      }

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = requestJson.toString().toRequestBody(mediaType)
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

      val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

      val response = httpClient.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        val errorText = try {
          val errorJson = JSONObject(responseBody)
          errorJson.optJSONObject("error")?.optString("message") ?: "HTTP error: ${response.code}"
        } catch (_: Exception) {
          "HTTP error: ${response.code}"
        }

        val errorMsg = AiMessage(
          text = "AI Service Error: $errorText",
          isFromUser = false,
          isError = true
        )
        val updatedWithError = _messages.value + errorMsg
        _messages.value = updatedWithError
        saveLocalMessages(updatedWithError)
        return@withContext Result.failure(Exception(errorText))
      }

      val json = JSONObject(responseBody)
      val candidates = json.optJSONArray("candidates")
      val firstCandidate = candidates?.optJSONObject(0)
      val content = firstCandidate?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      val aiReplyText = parts?.optJSONObject(0)?.optString("text") ?: "I'm here to help, but no response text was returned."

      val aiMsg = AiMessage(
        text = aiReplyText.trim(),
        isFromUser = false,
        isError = false
      )
      val updatedWithAi = _messages.value + aiMsg
      _messages.value = updatedWithAi
      saveLocalMessages(updatedWithAi)

      Result.success(aiReplyText)
    } catch (e: Exception) {
      val errorMsg = AiMessage(
        text = "Failed to communicate with AI service: ${e.localizedMessage ?: "Unknown error"}. Please check your internet connection.",
        isFromUser = false,
        isError = true
      )
      val updatedWithError = _messages.value + errorMsg
      _messages.value = updatedWithError
      saveLocalMessages(updatedWithError)
      Result.failure(e)
    }
  }

  fun clearChatHistory() {
    _messages.value = emptyList()
    saveLocalMessages(emptyList())
  }
}
