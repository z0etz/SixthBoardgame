package com.katja.sixthboardgame;

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.katja.sixthboardgame.databinding.ActivityStartGameBinding


class StartGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartGameBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userDao: UserDao
    private var userNameList: List<String>? = null
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val userMap = mutableMapOf<String?, String?>()
    private lateinit var firestore: FirebaseFirestore
    private var selectedUsersList = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var pendingInviteAdapter: PendingInviteAdapter
    private lateinit var inviteDao: InviteDao
    private val invitationsCollection =
        FirebaseFirestore.getInstance().collection("game_invitations")
    private var receiverId: String? = null

    // push

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartGameBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = Firebase.auth
        userDao = UserDao()
        inviteDao = InviteDao()

        autoCompleteTextView = binding.autoTv
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        recyclerView = findViewById(R.id.invitesRecyclerView)
        pendingInviteAdapter = PendingInviteAdapter(this, selectedUsersList, receiverId ?: "", onDeleteClickListener = { position ->
            val receiverName = selectedUsersList[position]
            val receiverId = userMap[receiverName]
            receiverId?.let {
                deleteInvite(firebaseAuth.currentUser?.uid!!, it)
            }
        }
        )
        recyclerView.adapter = pendingInviteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
 popup-invite-newgame
        currentUser?.let {
            inviteDao.listenForInvitations(it.uid) { invitations ->

            }
        }


        userDao.fetchUserNames { names ->
            userNameList = names?.distinct() // Remove duplicates
            Log.d("StartGameActivity", "Unique user names: $userNameList")
            adapter.clear() // Clear existing data
            userNameList?.let {
                adapter.addAll(it)
                Log.d("StartGameActivity", "Adapter populated with: $it")
            }
        }



        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String
 popup-invite-newgame
            showPopup(selectedUser)

        }

        val timeSlider = findViewById<SeekBar>(R.id.timeSlider)
        val selectedTimeTextView = findViewById<TextView>(R.id.selectedTimeTextView)

        timeSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedTimeTextView.text = "$progress hours"
                selectedTime = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        getAllUsers()

    }

    private var selectedTime: Int = 24
    private fun showPopup(selectedUser: String) {
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
            val receiverId = getReceiverId(selectedUser)
            receiverId?.let {
                val senderId = firebaseAuth.currentUser?.uid
                if (senderId != null) {
                    val inviteId = invitationsCollection.document().id
                    InviteDao().sendInvitation(senderId, it.toString(), inviteId)
                    selectedUsersList.add(selectedUser)
                    pendingInviteAdapter.notifyDataSetChanged()

                } else {
                    receiverId?.let {
                        val inviteId = invitationsCollection.document().id
                        InviteDao().sendInvitation(senderId, it, inviteId)
                    } ?: run {
                        Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show()
                    }
                    // line below is the culprit to the infamous bugg of the showing sender.
                    // selectedUsersList.add(selectedUser)
                    pendingInviteAdapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(this, "Sender ID is null", Toast.LENGTH_SHORT).show()
            }
        }

 popup-invite-newgame
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }


        builder.show()
    }

 popup-invite-newgame
    private fun endGameDueToTimeout() {
        Toast.makeText(this, "The game has ended due to inactivity", Toast.LENGTH_SHORT).show()
        finish()

    }


 popup-invite-newgame
    private fun getReceiverId(selectedUser: String): String? {
            return  userMap[selectedUser]
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


                adapter.clear() // clear old data
                adapter.addAll(usersList.distinct()) // Add actual names with specific id
                adapter.notifyDataSetChanged() // Notify adapter for changes
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

        private fun processInvitations(invitations: List<Map<String, Any>>) {
            val incomingInvites = mutableListOf<String>()
            val currentUser = firebaseAuth.currentUser
            val currentUserId = currentUser?.uid

 popup-invite-newgame
            for (invitation in invitations) {
                val senderId = invitation[inviteDao.SENDER_ID_KEY] as String
                val receiverId = invitation[inviteDao.RECEIVER_ID_KEY] as String
                val status = invitation[inviteDao.STATUS_KEY] as String
                // val inviteInfo = "Invitation from: $senderId - Status: $status"

                // Check if the current user is either the sender or receiver
                if (currentUserId == senderId || currentUserId == receiverId) {
                    incomingInvites.add(senderId)

                }
            }

 popup-invite-newgame
            // Add all invites to the list, both sent and received
            pendingInviteAdapter.updateInvitationsList(incomingInvites)
        }


        private fun deleteInvite(senderId: String, receiverId: String) {
            // Delete invitation from Firestore
            inviteDao.deleteInvitation(senderId, receiverId)
                .addOnSuccessListener {
                    val position = selectedUsersList.indexOf(receiverId)
                    if (position != -1) {
                        selectedUsersList.removeAt(position)
                        pendingInviteAdapter.notifyItemRemoved(position)
                    }
                    Toast.makeText(
                        this,
                        "Invitation deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "DeleteInvite",
                        "Failed to delete invitation: ${exception.message}",
                        exception
                    )
                    Toast.makeText(
                        this,
                        "Failed to delete invitation: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


        override fun onResume() {
            super.onResume()
            autoCompleteTextView.setText("")
        }

    }






