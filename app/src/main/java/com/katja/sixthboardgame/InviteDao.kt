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
    val STATUS_KEY = "status"

    fun sendInvitation(senderId: String, receiverId: String, inviteId: String,selectedTime: Int, callback: (Map<String, Any>) -> Unit) {
        val invitationData = hashMapOf(
            INVITE_ID_KEY to inviteId,
            SENDER_ID_KEY to senderId,
            RECEIVER_ID_KEY to receiverId,
            SELECTED_TIME_KEY to selectedTime, // Add turn time to the invitation data
            STATUS_KEY to "pending"
        )

        invitationsCollection.add(invitationData)
            .addOnSuccessListener { documentReference ->
                val inviteId = documentReference.id
                // Log the sent invitation details
                Log.d(TAG, "Invitation sent with id: $inviteId")

                // Pass invitationData to callback
                callback(invitationData)
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur while sending the invitation here
            }
    }

    // Metod för att lyssna på inkommande spelinbjudningar för en specifik användare
    fun listenForInvitations(receiverId: String, listener: (List<Map<String, Any>>) -> Unit) {
        invitationsCollection
            .whereEqualTo(RECEIVER_ID_KEY, receiverId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Hantera eventuellt fel vid lyssning på inbjudningar här
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


    // Metod för att uppdatera statusen för en spelinbjudning
    fun updateInvitationStatus(inviteId: String, newStatus: String) {
        invitationsCollection.document(inviteId)
            .update(STATUS_KEY, newStatus)
            .addOnSuccessListener {
                // Implementera eventuell logik för lyckad uppdatering av inbjudningsstatus här
            }
            .addOnFailureListener { e ->
                // Hantera eventuellt fel vid uppdatering av inbjudningsstatus här
            }
    }

    fun getInvite(inviteId: String, callback: (Map<String, Any>?) -> Unit) {
        invitationsCollection.document(inviteId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val inviteData = documentSnapshot.data
                callback(inviteData)
            }
            .addOnFailureListener { exception ->
                // Handle failure if necessary
                callback(null)
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



