package com.example.data.model

data class Contact(
  val id: String = "",
  val name: String = "",
  val identifier: String = "",
  val identifierType: String = "EMAIL", // "EMAIL", "PHONE", "USERNAME"
  val notes: String = "",
  val isRegisteredUser: Boolean = false,
  val registeredUid: String? = null,
  val photoUrl: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
