package com.katja.sixthboardgame

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import android.widget.TextView
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
    private var selectedUsersList = mutableListOf<Invite>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var pendingInviteAdapter: PendingInviteAdapter
    private lateinit var inviteDao: InviteDao
    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")
    private var receiverId: String? = null
    private var sentInvitesList = mutableListOf<Invite>()
    private val inviteMap = mutableMapOf<String, Invite>()


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
            val invite = selectedUsersList[position]
            val receiverId = invite.receiverId
            receiverId?.let {
                deleteInvite(firebaseAuth.currentUser?.uid!!, it)
            }
        })
        recyclerView.adapter = pendingInviteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //Set up sent invites list
        val sentRecyclerView = findViewById<RecyclerView>(R.id.sentInvitesRecyclerView)
        sentInviteAdapter = SentInviteAdapter(this, sentInvitesList) { position ->
            val invite = sentInvitesList[position]
            showSentInviteDialog(invite.inviteId)
        }
        sentRecyclerView.adapter = sentInviteAdapter
        sentRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            inviteDao.listenForSentInvitations(currentUser.uid) { sentInvitations ->
                processSentInvitations(sentInvitations)
            }
            inviteDao.listenForInvitations(currentUser.uid) { receivedInvitations ->
                val invites = receivedInvitations.map { invitation ->
                    val inviteId = invitation[inviteDao.INVITE_ID_KEY] as String
                    val senderId = invitation[inviteDao.SENDER_ID_KEY] as String
                    val receiverId = invitation[inviteDao.RECEIVER_ID_KEY] as String
                    val status = invitation[inviteDao.STATUS_KEY] as String
                    Invite(inviteId, senderId, receiverId, status)
                }
                processReceivedInvitations(invites)
            }
        }

        userDao.fetchUserNames { names ->
            runOnUiThread {
                userNameList = names?.distinct() // Remove duplicates
                Log.d("StartGameActivity", "Unique user names: $userNameList")
                adapter.clear() // Clear existing data
                userNameList?.let {
                    adapter.addAll(it)
                    Log.d("StartGameActivity", "Adapter populated with: $it")
                }
            }
        }

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String
            val receiverId = getReceiverId(selectedUser) // Update receiverId
            val senderId = firebaseAuth.currentUser?.uid

            if (senderId != null && receiverId != null && senderId != receiverId) {
                pendingInviteAdapter.showPopup(
                    this,
                    selectedUser,
                    userMap,
                    firebaseAuth,
                    invitationsCollection,
                    selectedUsersList,
                    pendingInviteAdapter
                )
            } else {
                Toast.makeText(this, "You cannot send an invitation to yourself.", Toast.LENGTH_SHORT).show()
            }
        }

        getAllUsers()
    }

    private fun getReceiverId(selectedUser: String): String? {
        return userMap[selectedUser]
    }

    private fun getAllUsers() {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                runOnUiThread {
                    val usersList = mutableListOf<String>()
                    for (document in querySnapshot.documents) {
                        val fullName = document.getString("UserName")
                        val user2Id = document.getString("id")
                        if (!userMap.containsKey(fullName)) {
                            fullName?.let { usersList.add(it) }
                            userMap[fullName] = user2Id
                        }
                    }
                    adapter.clear() // Clear existing data
                    adapter.addAll(usersList.distinct()) // Add distinct names only
                    adapter.notifyDataSetChanged() // Notify adapter for changes
                }
            }
            .addOnFailureListener { exception ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Failed to fetch users: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun processReceivedInvitations(invitations: List<Invite>) {
        val incomingInvites = mutableListOf<Invite>()
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        for (invitation in invitations) {
            val senderId = invitation.senderId
            val receiverId = invitation.receiverId

            if (currentUserId == receiverId) {
                incomingInvites.add(invitation)
            }
        }

        // Add all invites to the list, both sent and received
        pendingInviteAdapter.updateInvitationsList(incomingInvites)
    }

    private fun processSentInvitations(sentInvitations: List<Invite>) {
        val sentInvites = mutableListOf<Invite>()
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        inviteMap.clear()

        for (invite in sentInvitations) {
            inviteMap[invite.inviteId] = invite

            // Add logic here to ensure that the invitation was sent by the current user
            if (currentUserId == invite.senderId) {
                // Add the receiverId to the list of sent invites
                sentInvites.add(invite)
            }
        }
        // Logging: Print contents of inviteMap
        Log.d("InviteMap", "InviteMap: $inviteMap")
        // Update the UI with the sent invites
        sentInviteAdapter.updateInvitationsList(sentInvites)
    }

    private fun deleteInvite(senderId: String, receiverId: String) {
        // Delete invitation from Firestore
        inviteDao.deleteInvitation(senderId, receiverId)
            .addOnSuccessListener {
                runOnUiThread {
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
                    
                val position = selectedUsersList.indexOfFirst { it.receiverId == receiverId }
                if (position != -1) {
                    selectedUsersList.removeAt(position)
                    pendingInviteAdapter.notifyItemRemoved(position)
                }
            }
            .addOnFailureListener { exception ->
                runOnUiThread {
                    Log.e("DeleteInvite", "Failed to delete invitation: ${exception.message}", exception)
                    Toast.makeText(
                        this,
                        "Failed to delete invitation: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        autoCompleteTextView.setText("")
    }
}
