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
import kotlinx.coroutines.withContext

class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val receiverId: String,
    private val onDeleteClickListener: (Int) -> Unit,
    private val inviteDao: InviteDao = InviteDao(),
    private val userDao: UserDao = UserDao()
) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {

    //fix
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val senderId = inviteList[position]
        // Fetch username corresponding to the sender ID
        userDao.fetchUsernameById(senderId) { username ->
            if (!username.isNullOrEmpty()) {
                holder.bind(username) // Bind username to the view holder
            } else {
                // if no playerName or null
                holder.bind("Unknown")
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
        }

        init {
            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)
                true
            }
        }

        fun bind(playerName: String) {
            // set the sender name from the playerName and display it as "Invite from [senderName]"
            val senderName = context.getString(R.string.invited_by, playerName)
            playerNameTextView.text = senderName
        }
    }

    fun updateInvitationsList(newInvites: List<String>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()
    }

    private fun openGame(gameId: String) {
        val intent = Intent(context, GameActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        context.startActivity(intent)
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
                // Create a new a new game and add it to Firestore
                GameDao().addGame(currentUserId, receiverId)
                // Close dialog
                dialog.dismiss()
                // Delete invite from Firebase (reversed order of id:s to make function work from receiver side)
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
                // Delete invite from Firebase (reversed order of id:s to make function work from receiver side)
                inviteDao.deleteInvitation(receiverId, currentUserId)
            }

            dialog.show()
        } else {

            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
        }
    }
}
