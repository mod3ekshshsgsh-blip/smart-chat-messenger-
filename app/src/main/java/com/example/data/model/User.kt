package com.example.data.model

data class User(
  val uid: String = "",
  val displayName: String = "",
  val username: String = "",
  val usernameNormalized: String = "",
  val email: String = "",
  val photoUrl: String = "",
  val bio: String = "",
  val statusText: String = "Available",
  val isOnline: Boolean = true,
  val lastSeen: Long = System.currentTimeMillis(),
  val profileCompleted: Boolean = true,
  val memberId: String = "#SCM-89241"
)
