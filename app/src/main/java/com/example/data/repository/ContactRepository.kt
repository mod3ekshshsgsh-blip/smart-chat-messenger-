package com.example.data.repository

import android.content.Context
import com.example.data.model.Contact
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ContactRepository(private val context: Context? = null) {

  private var firestore: FirebaseFirestore? = null
  private var contactsListener: ListenerRegistration? = null
  private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
  val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

  private val prefs = context?.getSharedPreferences("smart_chat_contacts", Context.MODE_PRIVATE)

  init {
    try {
      firestore = FirebaseFirestore.getInstance()
    } catch (_: Exception) {}
    loadLocalContacts()
  }

  private fun loadLocalContacts() {
    if (prefs == null) return
    try {
      val jsonStr = prefs.getString("saved_contacts", "[]") ?: "[]"
      val array = JSONArray(jsonStr)
      val list = mutableListOf<Contact>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          Contact(
            id = obj.optString("id"),
            name = obj.optString("name"),
            identifier = obj.optString("identifier"),
            identifierType = obj.optString("identifierType", "EMAIL"),
            notes = obj.optString("notes"),
            isRegisteredUser = obj.optBoolean("isRegisteredUser", false),
            registeredUid = obj.optString("registeredUid").takeIf { it.isNotEmpty() },
            photoUrl = obj.optString("photoUrl").takeIf { it.isNotEmpty() },
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      _contacts.value = list
    } catch (_: Exception) {}
  }

  private fun saveLocalContacts(list: List<Contact>) {
    if (prefs == null) return
    try {
      val array = JSONArray()
      list.forEach { c ->
        val obj = JSONObject().apply {
          put("id", c.id)
          put("name", c.name)
          put("identifier", c.identifier)
          put("identifierType", c.identifierType)
          put("notes", c.notes)
          put("isRegisteredUser", c.isRegisteredUser)
          put("registeredUid", c.registeredUid ?: "")
          put("photoUrl", c.photoUrl ?: "")
          put("createdAt", c.createdAt)
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_contacts", array.toString()).apply()
    } catch (_: Exception) {}
  }

  fun attachUserContacts(uid: String) {
    contactsListener?.remove()
    val db = firestore ?: return
    if (uid.isBlank()) return

    try {
      contactsListener = db.collection("users")
        .document(uid)
        .collection("contacts")
        .addSnapshotListener { snapshot, error ->
          if (error != null || snapshot == null) return@addSnapshotListener

          val remoteContacts = snapshot.documents.mapNotNull { doc ->
            try {
              Contact(
                id = doc.id,
                name = doc.getString("name") ?: "Contact",
                identifier = doc.getString("identifier") ?: "",
                identifierType = doc.getString("identifierType") ?: "EMAIL",
                notes = doc.getString("notes") ?: "",
                isRegisteredUser = doc.getBoolean("isRegisteredUser") ?: false,
                registeredUid = doc.getString("registeredUid"),
                photoUrl = doc.getString("photoUrl"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
              )
            } catch (_: Exception) {
              null
            }
          }

          if (remoteContacts.isNotEmpty()) {
            _contacts.value = remoteContacts
            saveLocalContacts(remoteContacts)
          }
        }
    } catch (_: Exception) {}
  }

  suspend fun checkRegisteredUser(identifier: String): Pair<Boolean, Pair<String?, String?>> =
    withContext(Dispatchers.IO) {
      val clean = identifier.trim().lowercase().removePrefix("@")
      val db = firestore ?: return@withContext Pair(false, Pair(null, null))

      try {
        // 1. Try search by email
        val emailSnap = db.collection("users")
          .whereEqualTo("email", clean)
          .limit(1)
          .get()
          .await()
        if (!emailSnap.isEmpty) {
          val doc = emailSnap.documents.first()
          val uid = doc.getString("uid") ?: doc.id
          val photo = doc.getString("photoUrl")
          return@withContext Pair(true, Pair(uid, photo))
        }

        // 2. Try search by username
        val userSnap = db.collection("users")
          .whereEqualTo("usernameNormalized", clean)
          .limit(1)
          .get()
          .await()
        if (!userSnap.isEmpty) {
          val doc = userSnap.documents.first()
          val uid = doc.getString("uid") ?: doc.id
          val photo = doc.getString("photoUrl")
          return@withContext Pair(true, Pair(uid, photo))
        }

        Pair(false, Pair(null, null))
      } catch (_: Exception) {
        Pair(false, Pair(null, null))
      }
    }

  suspend fun addContact(
    name: String,
    identifier: String,
    identifierType: String = "EMAIL",
    notes: String = "",
    currentUid: String? = null
  ): Result<Contact> = withContext(Dispatchers.IO) {
    val cleanName = name.trim()
    val cleanIdentifier = identifier.trim()
    if (cleanName.isBlank()) return@withContext Result.failure(IllegalArgumentException("Please enter a name"))
    if (cleanIdentifier.isBlank()) return@withContext Result.failure(IllegalArgumentException("Please enter an email, phone, or username"))

    val (isRegistered, regDetails) = checkRegisteredUser(cleanIdentifier)
    val contactId = UUID.randomUUID().toString()

    val newContact = Contact(
      id = contactId,
      name = cleanName,
      identifier = cleanIdentifier,
      identifierType = identifierType,
      notes = notes.trim(),
      isRegisteredUser = isRegistered,
      registeredUid = regDetails.first,
      photoUrl = regDetails.second,
      createdAt = System.currentTimeMillis()
    )

    val updated = _contacts.value.filter { it.id != contactId } + newContact
    _contacts.value = updated
    saveLocalContacts(updated)

    // Save to Firestore if user is authenticated
    if (!currentUid.isNullOrBlank()) {
      try {
        val map = hashMapOf(
          "name" to newContact.name,
          "identifier" to newContact.identifier,
          "identifierType" to newContact.identifierType,
          "notes" to newContact.notes,
          "isRegisteredUser" to newContact.isRegisteredUser,
          "registeredUid" to (newContact.registeredUid ?: ""),
          "photoUrl" to (newContact.photoUrl ?: ""),
          "createdAt" to newContact.createdAt
        )
        firestore?.collection("users")
          ?.document(currentUid)
          ?.collection("contacts")
          ?.document(contactId)
          ?.set(map)
          ?.await()
      } catch (_: Exception) {}
    }

    Result.success(newContact)
  }

  suspend fun updateContact(
    contact: Contact,
    currentUid: String? = null
  ): Result<Contact> = withContext(Dispatchers.IO) {
    val (isRegistered, regDetails) = checkRegisteredUser(contact.identifier)
    val updatedContact = contact.copy(
      isRegisteredUser = isRegistered,
      registeredUid = regDetails.first ?: contact.registeredUid,
      photoUrl = regDetails.second ?: contact.photoUrl
    )

    val list = _contacts.value.map { if (it.id == updatedContact.id) updatedContact else it }
    _contacts.value = list
    saveLocalContacts(list)

    if (!currentUid.isNullOrBlank()) {
      try {
        val map = hashMapOf(
          "name" to updatedContact.name,
          "identifier" to updatedContact.identifier,
          "identifierType" to updatedContact.identifierType,
          "notes" to updatedContact.notes,
          "isRegisteredUser" to updatedContact.isRegisteredUser,
          "registeredUid" to (updatedContact.registeredUid ?: ""),
          "photoUrl" to (updatedContact.photoUrl ?: ""),
          "createdAt" to updatedContact.createdAt
        )
        firestore?.collection("users")
          ?.document(currentUid)
          ?.collection("contacts")
          ?.document(updatedContact.id)
          ?.set(map)
          ?.await()
      } catch (_: Exception) {}
    }

    Result.success(updatedContact)
  }

  suspend fun deleteContact(
    contactId: String,
    currentUid: String? = null
  ): Result<Unit> = withContext(Dispatchers.IO) {
    val list = _contacts.value.filter { it.id != contactId }
    _contacts.value = list
    saveLocalContacts(list)

    if (!currentUid.isNullOrBlank()) {
      try {
        firestore?.collection("users")
          ?.document(currentUid)
          ?.collection("contacts")
          ?.document(contactId)
          ?.delete()
          ?.await()
      } catch (_: Exception) {}
    }

    Result.success(Unit)
  }

  fun clearContacts() {
    contactsListener?.remove()
    contactsListener = null
    _contacts.value = emptyList()
  }
}
