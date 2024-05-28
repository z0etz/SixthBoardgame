package com.katja.sixthboardgame

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.katja.sixthboardgame.databinding.ActivityHighscoreBinding


class HighscoreActivity : AppCompatActivity() {

    lateinit var binding: ActivityHighscoreBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ArrayAdapter<String>
    private val userDao = UserDao()
    private var receiverId: String? = null
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private val invitationsCollection =
        FirebaseFirestore.getInstance().collection("game_invitations")
    private var selectedUsersList = mutableListOf<String>()
    private lateinit var pendingInviteAdapter: PendingInviteAdapter
    private val userMap = mutableMapOf<String?, String?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighscoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        pendingInviteAdapter = PendingInviteAdapter(this, selectedUsersList, receiverId ?: "") { position ->
            val receiverName = selectedUsersList[position]
            val receiverId = userMap[receiverName]
            receiverId?.let {
            }
        }
        getAllUsers()

        leaderboardAdapter = LeaderboardAdapter(emptyList()) {selectedUser ->
            showPopup(selectedUser)

        }
        binding.recyclerViewHighscore.adapter = leaderboardAdapter
        binding.recyclerViewHighscore.layoutManager = LinearLayoutManager(this)

        fetchAndDisplayLeaderboard()

    }

    private var selectedTime: Int = 24

    private fun showPopup(selectedUser: String) {
        Log.d("ShowPopup", "Showing popup for $selectedUser")

        val dialogView = layoutInflater.inflate(R.layout.dialog_time_choice, null)
        val timeSlider = dialogView.findViewById<SeekBar>(R.id.timeSlider)
        val selectedTimeTextView = dialogView.findViewById<TextView>(R.id.selectedTimeTextView)
        selectedTimeTextView.text = "$selectedTime hours"

        timeSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedTime = progress
                selectedTimeTextView.text = "$selectedTime hours"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val builder = AlertDialog.Builder(this)
        builder.setTitle("INVITE")
        builder.setMessage("Do you want to challenge $selectedUser?")
        builder.setView(dialogView)

        builder.setPositiveButton("Yes") { dialog, which ->
            Log.d("ShowPopup", "User clicked Yes to send invitation.")
            Log.d("ShowPopup", "Selected user: $selectedUser")
            Log.d("ShowPopup", "UserMap: $userMap")
            val receiverId = Utils.getReceiverId(selectedUser, userMap)
            Log.d("ShowPopup", "Receiver ID: $receiverId")
            if (receiverId != null) {
                Log.d("ShowPopup", "Receiver ID found: $receiverId")
                val senderId = firebaseAuth.currentUser?.uid
                if (senderId != null) {
                    Log.d("ShowPopup", "Sender ID found: $senderId")
                    val inviteId = invitationsCollection.document().id
                    InviteDao().sendInvitation(senderId, receiverId, inviteId)
                    selectedUsersList.add(selectedUser)
                    pendingInviteAdapter.notifyDataSetChanged()
                } else {
                    Log.e("ShowPopup", "Sender ID is null")
                    Toast.makeText(this, "Sender ID is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("ShowPopup", "Receiver ID not found")
                Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show()
            }
        }




        builder.setNegativeButton("No") { dialog, which ->
            Log.d("ShowPopup", "User clicked No.")
            dialog.dismiss()
        }

        builder.show()
    }




    private fun getReceiverId(selectedUser: String): String? {
        return  userMap[selectedUser]

    }

    private fun fetchAndDisplayLeaderboard() {
        userDao.fetchLeaderboard { leaderboard ->
            leaderboardAdapter.updateHighscores(leaderboard)
        }
    }

    private fun getAllUsers() {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val fullName = document.getString("UserName")
                    val user2Id = document.getString("id")
                    fullName?.let { usersList.add(it) }
                    userMap[fullName] = user2Id
                }
                adapter.addAll(usersList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch users: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}