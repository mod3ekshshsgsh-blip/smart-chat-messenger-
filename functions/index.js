const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Sends a push notification when a new message is posted in a conversation.
 */
exports.sendNewMessageNotification = functions.firestore
  .document("conversations/{conversationId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const conversationId = context.params.conversationId;

    const convDoc = await admin.firestore().collection("conversations").doc(conversationId).get();
    if (!convDoc.exists) return null;

    const convData = convDoc.data();
    const recipientUids = (convData.participants || []).filter(uid => uid !== message.senderId);

    const tokens = [];
    for (const uid of recipientUids) {
      const userDoc = await admin.firestore().collection("users").doc(uid).get();
      if (userDoc.exists && userDoc.data().fcmToken) {
        tokens.push(userDoc.data().fcmToken);
      }
    }

    if (tokens.length === 0) return null;

    const payload = {
      notification: {
        title: message.senderName || "New message",
        body: message.type === "AUDIO" ? "🎤 Voice message" : message.text,
      },
      data: {
        conversationId: conversationId,
        click_action: "OPEN_CONVERSATION",
      },
    };

    return admin.messaging().sendEachForMulticast({
      tokens: tokens,
      notification: payload.notification,
      data: payload.data,
    });
  });

/**
 * Sends a push notification when a friend request is created.
 */
exports.sendFriendRequestNotification = functions.firestore
  .document("friendRequests/{requestId}")
  .onCreate(async (snap, context) => {
    const request = snap.data();
    const toUid = request.toUid;
    if (!toUid) return null;

    const userDoc = await admin.firestore().collection("users").doc(toUid).get();
    if (!userDoc.exists || !userDoc.data().fcmToken) return null;

    const token = userDoc.data().fcmToken;
    const payload = {
      notification: {
        title: "New Friend Request",
        body: `${request.fromName || "Someone"} sent you a connection request.`,
      },
      data: {
        requestId: context.params.requestId,
        click_action: "OPEN_DISCOVER",
      },
    };

    return admin.messaging().send({
      token: token,
      notification: payload.notification,
      data: payload.data,
    });
  });
