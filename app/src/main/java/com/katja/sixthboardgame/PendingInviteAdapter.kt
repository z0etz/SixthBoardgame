package com.katja.sixthboardgame


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<Invite>,
    private val receiverId: String,
    private val onDeleteClickListener: (Int) -> Unit,
    private val inviteDao: InviteDao = InviteDao(),
    var selectedSliderTime: Int = 24 * 3600000,
    private val userDao: UserDao = UserDao(),
    private val gameDao: GameDao = GameDao() // Add GameDao dependency
) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        holder.bind(inviteList[position])
    }

    override fun getItemCount(): Int = inviteList.size

    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.invitePlayerName)
        private val selectedTimeTextView: TextView = itemView.findViewById(R.id.selectedTime)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d(
                        "PendingInviteAdapter",
                        "Item clicked at position $position: SenderId of invite = ${inviteList[position].senderId}"
                    )
                    showGameDialog(receiverId, inviteList[position].senderId)
                }
            }

            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)
                Log.d("PendingInviteAdapter", "Item long-clicked at position $adapterPosition")
                true
            }
        }

        fun bind(invite: Invite) {
            val senderId = invite.senderId
            val selectedTimeInMillis = invite.selectedTime

            // Convert milliseconds to hours
            val selectedTimeInHours = selectedTimeInMillis / (1000 * 60 * 60)
            val selectedTimeText = context.getString(R.string.selected_time, selectedTimeInHours)
            selectedTimeTextView.text = selectedTimeText

            userDao.fetchUsernameById(senderId) { username ->
                val playerName = username ?: "Unknown"
                val senderName = itemView.context.getString(R.string.invited_by, playerName)
                playerNameTextView.text = senderName
            }
        }
    }

    fun updateInvitationsList(newInvites: List<Invite>, selectedTimes: Map<String, Int>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()

        selectedTimes.forEach { (inviteId, selectedTime) ->
            val position = inviteList.indexOfFirst { it.inviteId == inviteId }
            if (position != -1) {
                inviteList[position].selectedTime = selectedTime
                notifyItemChanged(position)
            }
        }
        Log.d("PendingInviteAdapter", "Invitations list updated: $newInvites")
    }
    fun showPopup(
        context: Context,
        selectedUser: String,
        userMap: Map<String?, String?>,
        firebaseAuth: FirebaseAuth,
        invitationsCollection: CollectionReference,
        selectedUsersList: MutableList<Invite>,
        pendingInviteAdapter: PendingInviteAdapter
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_time_choice, null)
        val timeSlider = dialogView.findViewById<SeekBar>(R.id.timeSlider)
        val selectedTimeTextView = dialogView.findViewById<TextView>(R.id.selectedTimeTextView)

        // Convert seconds to hours for display
        selectedTimeTextView.text = "${selectedSliderTime / 3600000} hours"

        timeSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // Convert hours (progress) to seconds
                selectedSliderTime = progress * 3600000
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
                    InviteDao().sendInvitation(
                        senderId,
                        receiverId,
                        inviteId,
                        selectedSliderTime
                    ) { invitationData ->
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

    private fun showGameDialog(senderId: String, receiverId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId != null) {
            Log.d(
                "PendingInviteAdapter",
                "Fetching selected time from Firestore for senderId = $senderId, receiverId = $receiverId"
            )
            // Fetch selectedTime from Firestore
            fetchSelectedTimeFromFirebase(receiverId, currentUserId) { selectedTime ->
                if (selectedTime != null) {
                    Log.d("PendingInviteAdapter", "Fetched selected time: $selectedTime")
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.activity_game_dialog)

                    val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
                    val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

                    buttonContinue.setOnClickListener {
                        Log.d(
                            "PendingInviteAdapter",
                            "Continue button clicked: senderId = $senderId, receiverId = $receiverId"
                        )
                        // Create a new game and add it to Firestore (Parameters: senderId, receiverId, selectedTime)
                        gameDao.addGame(currentUserId, receiverId, selectedTime)
                        inviteDao.deleteInvitation(receiverId, currentUserId)
                        // Close dialog
                        dialog.dismiss()
                        // Delete invite from Firebase (Parameters: receiverId, senderId)

                    }

                    buttonCancel.setOnClickListener {
                        Log.d(
                            "PendingInviteAdapter",
                            "Cancel button clicked: senderId = $senderId, receiverId = $receiverId"
                        )
                        dialog.dismiss()
                        // Delete invite from Firebase (Parameters: receiverId, senderId)
                        inviteDao.deleteInvitation(receiverId, currentUserId)
                    }

                    val usernameText = dialog.findViewById<TextView>(R.id.usernameDialogTextView)
                    val opponentNameText = dialog.findViewById<TextView>(R.id.opponentsDialogTextView)

                    userDao.fetchUsernameById(receiverId) { username ->
                        if (username != null) {
                            usernameText.text = username
                        }
                    }
                    userDao.fetchUsernameById(currentUserId) { opponentName ->
                        if (opponentName != null) {
                            opponentNameText.text = opponentName
                        }
                    }

                    dialog.show()
                    Log.d(
                        "PendingInviteAdapter",
                        "Game dialog shown: senderId = $senderId, receiverId = $receiverId"
                    )
                } else {
                    Toast.makeText(context, "Failed to fetch selected time", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("PendingInviteAdapter", "Failed to fetch selected time")
                }
            }
        } else {
            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
            Log.e("PendingInviteAdapter", "Current user ID is null")
        }
    }

    fun fetchSelectedTimeFromFirebase(
        senderId: String,
        receiverId: String,
        callback: (Int?) -> Unit
    ) {
        val invitationsRef = FirebaseFirestore.getInstance().collection("game_invitations")
        Log.d(
            "PendingInviteAdapter",
            "Querying Firestore for senderId = $senderId and receiverId = $receiverId"
        )

        invitationsRef
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", receiverId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(
                    "PendingInviteAdapter",
                    "Query successful: ${querySnapshot.documents.size} documents found"
                )
                if (!querySnapshot.isEmpty) {
                    val document =
                        querySnapshot.documents[0] // Assuming one invite per sender-receiver pair
                    val selectedTime = document.getLong("selectedTime")?.toInt()
                    Log.d("PendingInviteAdapter", "Selected time fetched: $selectedTime")
                    callback(selectedTime)
                } else {
                    Log.d(
                        "PendingInviteAdapter",
                        "No documents found for the given senderId and receiverId"
                    )
                    callback(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("PendingInviteAdapter", "Error fetching documents: ${exception.message}")
                callback(null)
            }
    }
}

