package com.katja.sixthboardgame

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SentInviteAdapter(
    private val context: Activity,
    private val inviteList: MutableList<Invite>,
    private val onClickListener: (Int) -> Unit
) : RecyclerView.Adapter<SentInviteAdapter.InviteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        holder.bind(inviteList[position])
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    fun updateInvitationsList(sentInvites: MutableList<Invite>) {
        inviteList.clear()
        inviteList.addAll(sentInvites)
        notifyDataSetChanged()
    }

    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.invitePlayerName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClickListener(position)
                }
            }
        }

        fun bind(invite: Invite) {
            val receiverId = invite.receiverId

            UserDao().fetchUsernameById(receiverId) { username ->
                val playerName = username ?: "Unknown"
                val text = context.getString(R.string.invited, playerName)
                playerNameTextView.text = text
            }
        }
    }
}