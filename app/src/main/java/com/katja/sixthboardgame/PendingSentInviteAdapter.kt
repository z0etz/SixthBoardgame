package com.katja.sixthboardgame

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PendingSentInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<PendingSentInviteAdapter.InviteViewHolder>() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        filterSenderInvite()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        holder.bind(inviteList[position])
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.invitePlayerName)

        init {
            itemView.setOnClickListener {
                // Handle click if needed
            }

            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)
                true
            }
        }

        fun bind(playerName: String) {
            playerNameTextView.text = playerName
        }
    }

    fun updateInvitationsList(newInvites: List<String>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()
    }

    private fun filterSenderInvite() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            val inviteCollection = FirebaseFirestore
                .getInstance()
                .collection("game_invitations")

            inviteCollection
                .whereEqualTo("senderId", currentUserId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val filteredInvites = querySnapshot.documents.map { it.getString("receiverId") ?: "" }
                    updateInvitationsList(filteredInvites)
                }
                .addOnFailureListener { e ->
                    // Handle any errors that occur during the query
                    Log.e("PendingSentInviteAdapter", "Error fetching invites", e)
                }
        }
    }

    fun refreshList() {
        filterSenderInvite()
    }
}