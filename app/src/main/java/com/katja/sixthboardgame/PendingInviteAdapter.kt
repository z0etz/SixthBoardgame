package com.katja.sixthboardgame

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.PopupUtils.selectedTime
class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val receiverId: String,
    private val onDeleteClickListener: (Int) -> Unit,
    private val inviteDao: InviteDao = InviteDao(),
    private val userDao: UserDao = UserDao(),
    private val gameDao: GameDao = GameDao() // Add GameDao dependency
) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val senderId = inviteList[position]
        userDao.fetchUsernameById(senderId) { username ->
            if (!username.isNullOrEmpty()) {
                runOnUiThread {
                    Log.d("PendingInviteAdapter", "Binding username: $username with selectedTime: ${PopupUtils.selectedTime / 3600} hours")
                    holder.bind(username, PopupUtils.selectedTime / 3600) // Convert seconds to hours
                }
            } else {
                runOnUiThread {
                    Log.d("PendingInviteAdapter", "Binding username: Unknown with selectedTime: ${PopupUtils.selectedTime / 3600} hours")
                    holder.bind("Unknown", PopupUtils.selectedTime / 3600) // Convert seconds to hours
                }
            }
}
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.invitePlayerName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showGameDialog(receiverId, inviteList[position])
                }
            }

            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)
                true
            }
        }

        fun bind(playerName: String, selectedTimeHours: Int) {
            val senderName = itemView.context.getString(R.string.invited_by, playerName)
            val displayText = "$senderName for $selectedTimeHours hours"
            playerNameTextView.text = displayText
        }
    }

    fun updateInvitationsList(newInvites: List<String>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()
    }

    private fun showGameDialog(senderId: String, receiverId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId != null) {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.activity_game_dialog)

            val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
            val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

            buttonContinue.setOnClickListener {
                gameDao.addGame(currentUserId, receiverId)
                dialog.dismiss()
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            dialog.show()
        } else {
            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runOnUiThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            Handler(Looper.getMainLooper()).post { action() }
        }
    }
}
