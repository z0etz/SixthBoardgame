package com.katja.sixthboardgame


import android.app.AlertDialog
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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withContext

class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val receiverId: String,
    private val onDeleteClickListener: (Int) -> Unit,
    private val inviteDao: InviteDao = InviteDao(),
    var selectedTime: Int = 24 * 3600,
    private val userDao: UserDao = UserDao(),
    private val gameDao: GameDao = GameDao() // Add GameDao dependency
) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {
// he heee
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val senderId = inviteList[position]
        Log.d("PendingInviteAdapter", "Binding invite at position $position: senderId = $senderId")

        // Fetch username corresponding to the sender ID
        userDao.fetchUsernameById(senderId) { username ->
            if (!username.isNullOrEmpty()) {
                Log.d("PendingInviteAdapter", "Fetched username for senderId $senderId: $username")
                // Assuming you have access to selectedTime here
                holder.bind(username) // Pass selectedTime when binding
            } else {
                Log.d("PendingInviteAdapter", "Username for senderId $senderId is null or empty")
                holder.bind("Unknown") // Pass selectedTime when binding
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
                    Log.d("PendingInviteAdapter", "Item clicked at position $position: invite = ${inviteList[position]}")
                    showGameDialog(receiverId, inviteList[position])
                }
            }

            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)
                Log.d("PendingInviteAdapter", "Item long-clicked at position $adapterPosition")
                true
            }
        }

        fun bind(playerName: String) {
            val senderName = itemView.context.getString(R.string.invited_by, playerName)
            val displayText = "$senderName "
            playerNameTextView.text = displayText
            Log.d("PendingInviteAdapter", "Binding player name: $playerName with selected time: $selectedTime")
        }
    }

    fun updateInvitationsList(newInvites: List<String>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()
        Log.d("PendingInviteAdapter", "Invitations list updated: $newInvites")
    }

    private fun showGameDialog(senderId: String, receiverId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId != null) {
            // Fetch selectedTime from Firestore
            fetchSelectedTimeFromFirebase(receiverId) { selectedTime ->
                if (selectedTime != null) {
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.activity_game_dialog)

                    val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
                    val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

                    buttonContinue.setOnClickListener {
                        Log.d("PendingInviteAdapter", "Continue button clicked: senderId = $senderId, receiverId = $receiverId")
                        // Create a new game and add it to Firestore (Parameters: senderId, receiverId, selectedTime)
                        gameDao.addGame(currentUserId, receiverId, selectedTime)
                        // Close dialog
                        dialog.dismiss()
                        // Delete invite from Firebase (Parameters: receiverId, senderId)
                        inviteDao.deleteInvitation(receiverId, currentUserId)
                    }

                    buttonCancel.setOnClickListener {
                        Log.d("PendingInviteAdapter", "Cancel button clicked: senderId = $senderId, receiverId = $receiverId")
                        dialog.dismiss()
                        // Delete invite from Firebase (Parameters: receiverId, senderId)
                        inviteDao.deleteInvitation(receiverId, currentUserId)
                    }

                    dialog.show()
                    Log.d("PendingInviteAdapter", "Game dialog shown: senderId = $senderId, receiverId = $receiverId")
                } else {
                    Toast.makeText(context, "Failed to fetch selected time", Toast.LENGTH_SHORT).show()
                    Log.e("PendingInviteAdapter", "Failed to fetch selected time")
                }
            }
        } else {
            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
            Log.e("PendingInviteAdapter", "Current user ID is null")
        }
    }

    private fun fetchSelectedTimeFromFirebase(receiverId: String, callback: (Int?) -> Unit) {
        val invitationsRef = FirebaseFirestore.getInstance().collection("game_invitations")
        invitationsRef.whereEqualTo("receiverId", receiverId).get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0] // Assuming one invite per receiver
                val selectedTime = document.getLong("selectedTime")?.toInt()
                callback(selectedTime)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun showPopup(
        context: Context,
        selectedUser: String,
        userMap: Map<String?, String?>,
        firebaseAuth: FirebaseAuth,
        invitationsCollection: CollectionReference,
        selectedUsersList: MutableList<String>,
        pendingInviteAdapter: PendingInviteAdapter
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_time_choice, null)
        val timeSlider = dialogView.findViewById<SeekBar>(R.id.timeSlider)
        val selectedTimeTextView = dialogView.findViewById<TextView>(R.id.selectedTimeTextView)

        // Convert seconds to hours for display
        selectedTimeTextView.text = "${selectedTime / 3600} hours"

        timeSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Convert hours (progress) to seconds
                selectedTime = progress * 3600
                selectedTimeTextView.text = "$progress hours"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val builder = AlertDialog.Builder(context)
        builder.setTitle("INVITE")
        builder.setMessage("Do you want to challenge $selectedUser?")
        builder.setView(dialogView)

        builder.setPositiveButton("Yes") { dialog, which ->
            val receiverId = userMap[selectedUser]
            if (receiverId != null) {
                val senderId = firebaseAuth.currentUser?.uid
                if (senderId != null) {
                    val inviteId = invitationsCollection.document().id
                    InviteDao().sendInvitation(senderId, receiverId, inviteId,
                        selectedTime) { invitationData ->
                        // Handle the callback here, for example, you might want to update UI or log information
                        pendingInviteAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(context, "Sender ID is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Receiver ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }
}

