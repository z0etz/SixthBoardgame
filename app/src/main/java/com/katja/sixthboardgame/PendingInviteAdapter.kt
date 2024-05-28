package com.katja.sixthboardgame


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.inflate
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.PopupUtils.selectedTime
import kotlinx.coroutines.withContext

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
        // Fetch username corresponding to the sender ID
        userDao.fetchUsernameById(senderId) { username ->
            if (!username.isNullOrEmpty()) {
                // Assuming you have access to selectedTime here
                holder.bind(username, selectedTime) // Pass selectedTime when binding
            } else {
                // if no playerName or null
                holder.bind("Unknown", selectedTime) // Pass selectedTime when binding
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

        fun bind(playerName: String, selectedTime: Int) {
            // set the sender name from the playerName and display it as "Invite from [senderName] [selectedTime]"
            val senderName = itemView.context.getString(R.string.invited_by, playerName)
            val displayText = "$senderName $selectedTime"
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
                // Create a new game and add it to Firestore (Parameters: senderId, receiverId)
                gameDao.addGame(currentUserId, receiverId)
                // Close dialog
                dialog.dismiss()
                // Delete invite from Firebase (Parameters: receiverId, senderId)
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
                // Delete invite from Firebase (Parameters: receiverId, senderId)
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            dialog.show()
        } else {
            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
        }
    }
}

