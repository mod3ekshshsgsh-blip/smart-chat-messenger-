package com.example.data.model

enum class RequestStatus {
  PENDING,
  ACCEPTED,
  DECLINED,
  CANCELED
}

data class FriendRequest(
  val id: String = "",
  val fromUid: String = "",
  val fromName: String = "",
  val fromUsername: String = "",
  val fromAvatar: String = "",
  val role: String = "",
  val mutualCount: Int = 0,
  val timestamp: String = "2h ago",
  val isVerified: Boolean = false,
  val status: RequestStatus = RequestStatus.PENDING
)

data class SuggestedFriend(
  val id: String = "",
  val name: String = "",
  val username: String = "",
  val role: String = "",
  val avatar: String = "",
  val mutualCount: Int = 0,
  val interests: List<String> = emptyList(),
  val isVerified: Boolean = false,
  val isRequested: Boolean = false,
  val isAdded: Boolean = false
)
