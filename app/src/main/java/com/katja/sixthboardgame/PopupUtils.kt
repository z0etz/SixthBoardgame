package com.katja.sixthboardgame

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.katja.sixthboardgame.InviteDao
import com.katja.sixthboardgame.R
import com.katja.sixthboardgame.StartGameActivity

object PopupUtils {

    var selectedTime: Int = 24 * 3600

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
                    InviteDao().sendInvitation(senderId, receiverId, inviteId) { invitationData ->
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

