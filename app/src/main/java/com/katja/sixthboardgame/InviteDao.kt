package com.katja.sixthboardgame
import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class InviteDao {

    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")

    val INVITE_ID_KEY = "inviteId"
    val SENDER_ID_KEY = "senderId"
    val RECEIVER_ID_KEY = "receiverId"
    val SELECTED_TIME_KEY = "selectedTime"

    fun sendInvitation(senderId: String, receiverId: String, inviteId: String,selectedTime: Int, callback: (Map<String, Any>) -> Unit) {
        val invitationData = hashMapOf(
            INVITE_ID_KEY to inviteId,
            SENDER_ID_KEY to senderId,
            RECEIVER_ID_KEY to receiverId,
            SELECTED_TIME_KEY to selectedTime
        )

        invitationsCollection.add(invitationData)
            .addOnSuccessListener { documentReference ->
                val inviteId = documentReference.id
                Log.d(TAG, "Invitation sent with id: $inviteId")

                callback(invitationData)
            }
            .addOnFailureListener { e ->
            }
    }

    fun listenForInvitations(receiverId: String, listener: (List<Map<String, Any>>) -> Unit) {
        invitationsCollection
            .whereEqualTo(RECEIVER_ID_KEY, receiverId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                val invitations = mutableListOf<Map<String, Any>>()
                snapshots?.documents?.forEach { document ->
                    val invitation = document.data ?: mapOf()
                    invitations.add(invitation)
                }

                listener(invitations)
            }
    }

    // Method to listen for sent game invitations for a specific user
    fun listenForSentInvitations(senderId: String, listener: (List<Invite>) -> Unit) {
        invitationsCollection
            .whereEqualTo(SENDER_ID_KEY, senderId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening for invitations: ", exception)
                    return@addSnapshotListener
                }

                val invitations = mutableListOf<Invite>()
                snapshots?.documents?.forEach { document ->
                    val invitation = document.toObject<Invite>()
                    if (invitation != null) {
                        invitations.add(invitation)
                    }
                }

                listener(invitations)
            }
    }

    fun deleteInvitation(senderId: String, receiverId: String): Task<Void> {
        return invitationsCollection
            .whereEqualTo(SENDER_ID_KEY, senderId)
            .whereEqualTo(RECEIVER_ID_KEY, receiverId)
            .get()
            .continueWith { querySnapshot ->
                val batch = FirebaseFirestore.getInstance().batch()
                querySnapshot.result?.forEach { documentSnapshot ->
                    batch.delete(documentSnapshot.reference)
                }
                batch.commit()
                null
            }
    }

    // Currently unused function, save as it my be needed when implementing future functionality
    fun fetchInvitationById(inviteId: String, callback: (Invite?) -> Unit) {
        invitationsCollection.document(inviteId).get()
            .addOnSuccessListener { documentSnapshot ->
                val invite = documentSnapshot.toObject<Invite>()
                callback(invite)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching invitation: ", e)
                callback(null)
            }
    }
}



