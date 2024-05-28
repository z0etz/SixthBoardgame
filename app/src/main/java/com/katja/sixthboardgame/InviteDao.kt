package com.katja.sixthboardgame
import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.katja.sixthboardgame.PopupUtils.selectedTime

class InviteDao {

    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")

    val SENDER_ID_KEY = "senderId"
    val RECEIVER_ID_KEY = "receiverId"
    val STATUS_KEY = "status"
    val TIME_KEY = "selectedTime"

    fun sendInvitation(senderId: String, receiverId: String, inviteId: String, selectedTime: Int, callback: (Map<String, Any>) -> Unit) {
        val invitation = hashMapOf(
            SENDER_ID_KEY to senderId,
            RECEIVER_ID_KEY to receiverId,
            STATUS_KEY to "pending",
            TIME_KEY to selectedTime
        )

        FirebaseFirestore.getInstance().collection("game_invitations")
            .document(inviteId)
            .set(invitation)
            .addOnSuccessListener {
                callback(invitation)
            }
            .addOnFailureListener { exception ->
                Log.e("InviteDao", "Failed to send invitation: ${exception.message}", exception)
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
    fun listenForSentInvitations(senderId: String, listener: (List<Map<String, Any>>) -> Unit) {
        invitationsCollection
            .whereEqualTo(SENDER_ID_KEY, senderId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    // Handle any errors that occur during the listening process
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


}
