package com.katja.sixthboardgame
import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class InviteDao {
    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")
    val INVITE_ID_KEY = "inviteId"
    val SENDER_ID_KEY = "senderId"
    val RECEIVER_ID_KEY = "receiverId"
    val STATUS_KEY = "status"

    fun sendInvitation(senderId: String, receiverId: String, inviteId: String) {
        val invitationData = hashMapOf(
            INVITE_ID_KEY to inviteId,
            SENDER_ID_KEY to senderId,
            RECEIVER_ID_KEY to receiverId,
            STATUS_KEY to "pending"
            // Lägg till andra relevanta attribut för spelinbjudningar här
        )

        invitationsCollection.add(invitationData)
            .addOnSuccessListener { documentReference ->
                val inviteId = documentReference.id
                // Använd inviteId för att identifiera den nya spelinbjudningen
                Log.d(TAG, "Invitation sent with id: $inviteId")
                // Implementera eventuell logik för lyckad inbjudningsskickning här
            }
            .addOnFailureListener { e ->
                // Hantera eventuellt fel vid skickning av inbjudning här
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
