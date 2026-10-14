package com.example.data.repository

import android.content.Context
import com.example.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T =
  suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it, null) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
  }

class AuthRepository(private val context: Context) {

  private val prefs = context.getSharedPreferences("smart_chat_auth_prefs", Context.MODE_PRIVATE)
  private var firebaseAuth: FirebaseAuth? = null
  private var firestore: FirebaseFirestore? = null

  init {
    try {
      firebaseAuth = FirebaseAuth.getInstance()
      firestore = FirebaseFirestore.getInstance()
    } catch (_: Exception) {}
  }

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  init {
    initAuthSession()
  }

  private fun initAuthSession() {
    val fbUser = firebaseAuth?.currentUser
    val cachedUid = prefs.getString("user_uid", null)

    if (fbUser != null) {
      val name = prefs.getString("user_name", fbUser.displayName ?: "User") ?: "User"
      val username = prefs.getString("user_username", fbUser.email?.substringBefore("@") ?: "user") ?: "user"
      val email = fbUser.email ?: ""
      val bio = prefs.getString("user_bio", "Hey there! I am using Smart Chat.") ?: "Hey there! I am using Smart Chat."
      val status = prefs.getString("user_status", "Available") ?: "Available"
      val memberId = prefs.getString("user_member_id", "#SCM-${(10000..99999).random()}") ?: "#SCM-89241"

      val localUser = User(
        uid = fbUser.uid,
        displayName = name,
        username = username,
        usernameNormalized = username.lowercase(),
        email = email,
        bio = bio,
        statusText = status,
        isOnline = true,
        memberId = memberId
      )
      _currentUser.value = localUser

      // Sync latest profile from Firestore
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val doc = firestore?.collection("users")?.document(fbUser.uid)?.get()?.await()
          if (doc != null && doc.exists()) {
            val updatedUser = localUser.copy(
              displayName = doc.getString("displayName") ?: localUser.displayName,
              username = doc.getString("username") ?: localUser.username,
              usernameNormalized = doc.getString("usernameNormalized") ?: localUser.usernameNormalized,
              bio = doc.getString("bio") ?: localUser.bio,
              statusText = doc.getString("statusText") ?: localUser.statusText,
              memberId = doc.getString("memberId") ?: localUser.memberId,
              photoUrl = doc.getString("photoUrl") ?: localUser.photoUrl
            )
            saveLocalUser(updatedUser)
          }
        } catch (_: Exception) {}
      }
    } else if (cachedUid != null) {
      // Restore from local cache if available
      val name = prefs.getString("user_name", "User") ?: "User"
      val username = prefs.getString("user_username", "user") ?: "user"
      val email = prefs.getString("user_email", "") ?: ""
      val bio = prefs.getString("user_bio", "Hey there! I am using Smart Chat.") ?: "Hey there! I am using Smart Chat."
      val status = prefs.getString("user_status", "Available") ?: "Available"
      val memberId = prefs.getString("user_member_id", "#SCM-89241") ?: "#SCM-89241"

      _currentUser.value = User(
        uid = cachedUid,
        displayName = name,
        username = username,
        usernameNormalized = username.lowercase(),
        email = email,
        bio = bio,
        statusText = status,
        isOnline = true,
        memberId = memberId
      )
    }
  }

  suspend fun login(email: String, password: String): Result<User> {
    val cleanEmail = email.trim().lowercase()
    val cleanPassword = password.trim()

    if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your email and password"))
    }

    val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase is not initialized."))

    return try {
      val authResult = auth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
      val fbUser = authResult.user ?: return Result.failure(IllegalStateException("Authentication succeeded but no user returned."))

      var displayName = fbUser.displayName ?: cleanEmail.substringBefore("@")
      var bio = "Hey there! I am using Smart Chat."
      var status = "Available"
      var memberId = "#SCM-${(10000..99999).random()}"
      var photoUrl = fbUser.photoUrl?.toString() ?: ""

      try {
        val doc = firestore?.collection("users")?.document(fbUser.uid)?.get()?.await()
        if (doc != null && doc.exists()) {
          displayName = doc.getString("displayName") ?: displayName
          bio = doc.getString("bio") ?: bio
          status = doc.getString("statusText") ?: status
          memberId = doc.getString("memberId") ?: memberId
          photoUrl = doc.getString("photoUrl") ?: photoUrl
          // Update online presence
          firestore?.collection("users")?.document(fbUser.uid)?.update("isOnline", true)
        }
      } catch (_: Exception) {}

      val user = User(
        uid = fbUser.uid,
        displayName = displayName,
        username = cleanEmail.substringBefore("@"),
        usernameNormalized = cleanEmail.substringBefore("@"),
        email = fbUser.email ?: cleanEmail,
        photoUrl = photoUrl,
        bio = bio,
        statusText = status,
        isOnline = true,
        memberId = memberId
      )
      saveLocalUser(user)
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun register(name: String, email: String, password: String): Result<User> {
    val cleanName = name.trim()
    val cleanEmail = email.trim().lowercase()
    val cleanPassword = password.trim()

    if (cleanName.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your full name"))
    }
    if (cleanEmail.isBlank()) {
      return Result.failure(IllegalArgumentException("Please enter your email address"))
    }
    if (cleanPassword.length < 6) {
      return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
    }

    val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase is not initialized."))

    return try {
      val authResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
      val fbUser = authResult.user ?: return Result.failure(IllegalStateException("Registration succeeded but no user returned."))

      val username = cleanEmail.substringBefore("@").lowercase()
      val memberId = "#SCM-${(10000..99999).random()}"
      val user = User(
        uid = fbUser.uid,
        displayName = cleanName,
        username = username,
        usernameNormalized = username,
        email = fbUser.email ?: cleanEmail,
        bio = "Hey there! I am using Smart Chat.",
        statusText = "Available",
        isOnline = true,
        memberId = memberId
      )

      // Write user profile to Firestore
      try {
        val userMap = hashMapOf(
          "uid" to fbUser.uid,
          "displayName" to cleanName,
          "username" to username,
          "usernameNormalized" to username,
          "email" to cleanEmail,
          "bio" to user.bio,
          "statusText" to user.statusText,
          "isOnline" to true,
          "memberId" to memberId,
          "createdAt" to System.currentTimeMillis()
        )
        firestore?.collection("users")?.document(fbUser.uid)?.set(userMap)?.await()
      } catch (_: Exception) {}

      saveLocalUser(user)
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun resetPassword(email: String): Result<Unit> {
    val cleanEmail = email.trim()
    if (cleanEmail.isBlank()) return Result.failure(IllegalArgumentException("Please provide a valid email"))
    val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase is not initialized."))

    return try {
      auth.sendPasswordResetEmail(cleanEmail).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun updateStatus(status: String) {
    val current = _currentUser.value ?: return
    val updated = current.copy(statusText = status)
    saveLocalUser(updated)
    try {
      firestore?.collection("users")?.document(current.uid)?.update("statusText", status)
    } catch (_: Exception) {}
  }

  fun updateProfile(displayName: String, bio: String) {
    val current = _currentUser.value ?: return
    val updated = current.copy(displayName = displayName, bio = bio)
    saveLocalUser(updated)
    try {
      firestore?.collection("users")?.document(current.uid)?.update(
        mapOf("displayName" to displayName, "bio" to bio)
      )
    } catch (_: Exception) {}
  }

  private fun saveLocalUser(user: User) {
    prefs.edit()
      .putString("user_uid", user.uid)
      .putString("user_name", user.displayName)
      .putString("user_username", user.username)
      .putString("user_email", user.email)
      .putString("user_bio", user.bio)
      .putString("user_status", user.statusText)
      .putString("user_member_id", user.memberId)
      .apply()
    _currentUser.value = user
  }

  fun logout() {
    val current = _currentUser.value
    if (current != null) {
      try {
        firestore?.collection("users")?.document(current.uid)?.update("isOnline", false)
      } catch (_: Exception) {}
    }
    try {
      firebaseAuth?.signOut()
    } catch (_: Exception) {}
    prefs.edit().clear().apply()
    _currentUser.value = null
  }

  fun parseAuthError(e: Throwable, isRegistering: Boolean): String {
    val msg = e.message ?: ""
    return when {
      e is FirebaseAuthInvalidUserException -> {
        "No account found with this email. Please switch to 'Sign Up' below to create your account first."
      }
      e is FirebaseAuthInvalidCredentialsException -> {
        if (isRegistering) {
          "The email format is invalid or Email/Password provider is not enabled in Firebase Console. Please verify."
        } else {
          "Account not found or password incorrect. If you haven't created an account yet, please tap 'Sign Up' below to register."
        }
      }
      e is FirebaseAuthUserCollisionException -> {
        "An account with this email already exists. Please tap 'Sign In' instead."
      }
      e is FirebaseAuthWeakPasswordException -> {
        "Password is too weak. Please use at least 6 characters."
      }
      msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
      msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) -> {
        "Email/Password sign-in is disabled in your Firebase Console. Please go to Firebase Console > Authentication > Sign-in method and enable 'Email/Password'."
      }
      msg.contains("network", ignoreCase = true) -> {
        "Network connection error. Please verify your internet connection and try again."
      }
      msg.contains("supplied auth credential", ignoreCase = true) ||
      msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
      msg.contains("invalid_credential", ignoreCase = true) ||
      msg.contains("malformed", ignoreCase = true) ||
      msg.contains("incorrect", ignoreCase = true) ||
      msg.contains("expired", ignoreCase = true) -> {
        if (isRegistering) {
          "Invalid registration details or Email/Password is not enabled in Firebase Console. Please check your credentials."
        } else {
          "Account not found or password incorrect. If this is your first time, please tap 'Sign Up' below to create your account first."
        }
      }
      else -> msg.ifBlank { "Authentication failed. Please verify your details." }
    }
  }
}
