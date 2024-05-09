package com.katja.sixthboardgame;

import android.app.Activity;
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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
        pendingInviteAdapter = PendingInviteAdapter(selectedUsersList) { position ->
            deleteInvite(position)
        }
        recyclerView.adapter = pendingInviteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null){
            inviteDao.listenForInvitations(currentUser.uid) { invitations ->
                processInvitations(invitations)
            }
        }

        userDao.fetchUserNames { names ->
            userNameList = names
            adapter.addAll(names ?: emptyList())
        }

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String
            val receiverId: String? = userMap[selectedUser]

            receiverId?.let{
                val senderId = firebaseAuth.currentUser?.uid
                if (senderId != null){
                    InviteDao().sendInvitation(senderId, it)
                } else {
                    Toast.makeText(this, "Sender ID is null", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show()
            }

            selectedUsersList.add(selectedUser)
            pendingInviteAdapter.notifyDataSetChanged()
        }

        getAllUsers()
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

    private fun processInvitations(invitations: List<Map<String, Any>>) {
        val incomingInvites = mutableListOf<String>()
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        for (invitation in invitations) {
            val senderId = invitation[inviteDao.SENDER_ID_KEY] as String
            val receiverId = invitation[inviteDao.RECEIVER_ID_KEY] as String
            val status = invitation[inviteDao.STATUS_KEY] as String
            val inviteInfo = "Invitation from: $senderId - Status: $status"

            // Check if the current user is either the sender or receiver
            if (currentUserId == senderId || currentUserId == receiverId) {
                incomingInvites.add(inviteInfo)
            }
        }

        // Add all invites to the list, both sent and received
        pendingInviteAdapter.updateInvitationsList(incomingInvites)
    }


    private fun deleteInvite(position: Int) {
        selectedUsersList.removeAt(position)
        pendingInviteAdapter.notifyItemRemoved(position)

    }

    override fun onResume() {
        super.onResume()
        autoCompleteTextView.setText("")
    }
}
