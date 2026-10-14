package com.example.data.repository

import com.example.data.model.Community
import com.example.data.model.FriendRequest
import com.example.data.model.RequestStatus
import com.example.data.model.SuggestedFriend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FriendRepository {

  private var firestore: FirebaseFirestore? = null

  init {
    try {
      firestore = FirebaseFirestore.getInstance()
    } catch (_: Exception) {}
  }

  private val _pendingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
  val pendingRequests: StateFlow<List<FriendRequest>> = _pendingRequests.asStateFlow()

  private val _suggestedFriends = MutableStateFlow<List<SuggestedFriend>>(emptyList())
  val suggestedFriends: StateFlow<List<SuggestedFriend>> = _suggestedFriends.asStateFlow()

  private val _communities = MutableStateFlow<List<Community>>(emptyList())
  val communities: StateFlow<List<Community>> = _communities.asStateFlow()

  suspend fun searchUsers(query: String, currentUid: String): List<SuggestedFriend> {
    val clean = query.trim().lowercase()
    if (clean.isBlank()) return emptyList()
    val db = firestore ?: return emptyList()

    return try {
      val snap = db.collection("users")
        .whereGreaterThanOrEqualTo("usernameNormalized", clean)
        .whereLessThanOrEqualTo("usernameNormalized", clean + "\uf8ff")
        .limit(10)
        .get()
        .await()

      snap.documents.mapNotNull { doc ->
        val uid = doc.getString("uid") ?: doc.id
        if (uid == currentUid) return@mapNotNull null

        val name = doc.getString("displayName") ?: "User"
        val username = "@" + (doc.getString("username") ?: "user")
        val photoUrl = doc.getString("photoUrl") ?: ""
        val statusText = doc.getString("statusText") ?: "Member"

        SuggestedFriend(
          id = uid,
          name = name,
          username = username,
          role = statusText,
          avatar = photoUrl,
          isVerified = false,
          isRequested = false
        )
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun acceptRequest(requestId: String) {
    _pendingRequests.value = _pendingRequests.value.filter { it.id != requestId }
  }

  fun declineRequest(requestId: String) {
    _pendingRequests.value = _pendingRequests.value.filter { it.id != requestId }
  }

  fun toggleAddFriend(friendId: String) {
    _suggestedFriends.value = _suggestedFriends.value.map { friend ->
      if (friend.id == friendId) {
        friend.copy(isAdded = !friend.isAdded)
      } else friend
    }
  }

  fun toggleCommunityJoin(communityId: String) {
    _communities.value = _communities.value.map { comm ->
      if (comm.id == communityId) {
        comm.copy(isJoined = !comm.isJoined)
      } else comm
    }
  }
}
