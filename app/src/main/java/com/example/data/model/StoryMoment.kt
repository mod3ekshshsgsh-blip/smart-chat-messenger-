package com.example.data.model

data class StoryMoment(
  val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userAvatar: String = "",
  val isLive: Boolean = false,
  val isUserStory: Boolean = false,
  val hasUnseen: Boolean = false
)

data class Community(
  val id: String = "",
  val name: String = "",
  val description: String = "",
  val memberCount: String = "",
  val activityTag: String = "",
  val isVerified: Boolean = true,
  val isJoined: Boolean = false
)
